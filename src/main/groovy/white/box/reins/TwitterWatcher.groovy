package white.box.reins

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.UserList
import twitter4j.conf.ConfigurationBuilder
import white.box.reins.component.OAuthComponent
import white.box.reins.dao.ListDataDao
import white.box.reins.dao.ListMstDao
import white.box.reins.model.ListData

/**
 * Twitterアカウントのリストから定期的に画像のURLを取得する。<br>
 *
 * @author seri
 */
@Slf4j
class TwitterWatcher extends Thread {

	def config = null
	Twitter twitter = null

	/** 1度に取得要求するTweet数 */
	final int TWEET_MAX_COUNT

	/** sleepのベース時間：リスト毎は短め、チェック後は長め */
	final int WAIT_TIME

	private boolean loop = true

	/** リストマスタ参照用のDAO */
	ListMstDao listMstDao = null

	/** リストデータの作成に利用するDAO */
	ListDataDao listDataDao = null

	/**
	 * コンストラクタ<br>
	 * Config値の設定を行う。
	 *
	 * @param config Config値
	 * @param twitter Twitterインスタンス
	 */
	TwitterWatcher(config, Twitter twitter) {
		this.config = config
		this.twitter = twitter

		TWEET_MAX_COUNT = config.reins.tweet.maxcount
		WAIT_TIME = config.reins.loop.waittime
	}


	/**
	 * スレッド停止用メソッド<br>
	 * スレッド作成元のスレッドで終了時に呼ぶこと。
	 */
	void stopRunning(){
		loop = false
	}

	@Override
	void run() {

		// 先にユーザ情報を取り、これを使いまわす
		def userinfo = twitter.verifyCredentials()

		Sql db = Sql.newInstance(ReinsConstants.JDBC_MAP)
		listMstDao = new ListMstDao(db)
		listDataDao = new ListDataDao(db)

		while(loop) {
			try {
				// 画像URLの取得処理
				loopImageGetTask(userinfo)
			}
			catch (TwitterException te) {
				log.error("Twitter service or network is unavailable.", te)
				log.info "Twitter service or network is unavailable. wait ${15} minutes until next search."
				sleep(15 * 60 * 1000)
			}
		}
	}


	/**
	 * 指定されたTwitterアカウントのリストから画像のURLを取得し、DBに保存する
	 *
	 * @param userinfo Twitterのユーザ情報。再認証後は再取得の必要がある。
	 */
	protected void loopImageGetTask(userinfo)
	throws TwitterException {

		// 認証ユーザが持つリストを取得
		ResponseList<UserList> lists = null
		try {
			lists = twitter.getUserLists(userinfo.getScreenName())
		}
		catch (TwitterException te) {

			// リスト取得で401が返ってきた場合は、再認証処理を行う必要がある
			def oauth = new OAuthComponent()
			if (!oauth.isAuthorized(twitter)) {
				ConfigurationBuilder cb = new ConfigurationBuilder()
				String consumerKey = config.get("oauth.consumerKey")
				String consumerSecret = config.get("oauth.consumerSecret")
				cb.setDebugEnabled(true)
					.setOAuthConsumerKey(consumerKey)
					.setOAuthConsumerSecret(consumerSecret)

				TwitterFactory factory = new TwitterFactory(cb.build())
				twitter = factory.getInstance()

				// 再認証
				oauth.authorize(twitter)

				// 必要なTwitter情報と取り直す
				userinfo = twitter.verifyCredentials()
				lists = twitter.getUserLists(userinfo.getScreenName())
			}
		}

		// TODO:list_mstが持つリスト名をどこかのタイミングで更新するようにすること
		// TODO:リストのブラック、ホワイトリストを持つようにした場合、そこのチェックタイミングで更新すること


		// リストごとに情報を取得
		Paging paging = new Paging(1, TWEET_MAX_COUNT)
		lists.each { list ->

			// list_idでマスタを探し、存在しなければリスト用のテーブルを作成する。
			long listId = list.getId()
			String listname = list.getName()

			if (!listMstDao.find(listId)) {
				listMstDao.insert(listId, listname)
				listDataDao.create(listId)
			}

			// 現在チェックしているところまでのsince_idを設定
			// マスタのsinceIdがnullでなければpagingする
			long currentSinceId = listMstDao.getSinceId(listId) ?: -1

			// --------------- ツイート取得して解析 -----------------
			int paging_max_count = 1
			if (currentSinceId != -1) {
				paging.sinceId = currentSinceId
				paging_max_count = 10
			} else {
				paging = new Paging(1, TWEET_MAX_COUNT)
			}

			log.info("[check]$listname current since_id:" + currentSinceId)

			// 最大200(TWEET_MAX_COUNT)×10(paging_max_count)ツイートを取得し、チェックする
			for (int i=1; i <= paging_max_count; i++) {
				paging.page = i
				ResponseList<Status> statuses = twitter.getUserListStatuses(listId, paging)

				if (statuses == null || statuses.size() == 0) {
					break
				}

				for (Status status : statuses) {
					registerImageUrl(listId, status)
				}

				if (i==1) {
					// since_idの保持
					listMstDao.updateSinceId(listId, statuses.get(0).getId())
				}
			}

			// リストごとにちょっと待つ
			sleep(WAIT_TIME * 10)
		}

		// 1周したら結構待つ
		log.info "list check completed. wait ${WAIT_TIME * 600 / 1000}s until next search."
		sleep(WAIT_TIME * 600)
	}



	/**
	 * Tweetに画像関係のURLが含まれていれば、
	 * そのURLをlist_${listId}テーブルに登録する。<br>
	 *
	 * @param listId チェック対象のリストのID
	 * @param status Tweet情報
	 */
	protected void registerImageUrl(long listId, Status status) {

		// Tweetしたユーザー名
		String screenName = null
		// Retweetしたユーザ名
		String retweetUser = null

		if (status.getRetweetedStatus() != null && config.reins.retweet.target) {
			// RTの場合はRT元のユーザー名を格納する
			screenName = status.getRetweetedStatus().getUser().getScreenName()
			retweetUser = status.getUser().getScreenName()
		}
		else if (status.getRetweetedStatus() == null) {
			screenName = status.getUser().getScreenName()
		} else {
			return
		}

		// DB登録時の共通値設定
		ListData listData = new ListData(
				statusId : status.getId(),
				tweetDate : status.getCreatedAt(),
				screenName : screenName,
				retweetUser : retweetUser,
				counterStatus : 0)

		// media_urlならTwitter公式、それ以外は別形式で保存
		status.getExtendedMediaEntities().each {
			listData.imageUrl = it.getMediaURL()
			listData.attribute = "twitter"
			listDataDao.insert(listId, listData)
		}
		// ----------■公式以外のリンクを取得する場合はここに書く--------------
		status.getURLEntities().each {
			// pixivリンクの保存
			if(it.getExpandedURL() =~ """www.pixiv.net/member_illust.php""") {
				listData.attribute = "pixiv"
			}
			// gifの保存
			else if (it.getExpandedURL() =~ /.gif$/) {
				listData.attribute = "gif"
			}
			listData.imageUrl = it.getExpandedURL()
			listDataDao.insert(listId, listData)
		}
		// ---------------------------------------------------------------------
	}
}
