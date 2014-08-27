package white.box.reins

import groovy.sql.Sql
import twitter4j.Paging
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User
import twitter4j.UserList;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import white.box.reins.dao.ListDataDao
import white.box.reins.dao.ListMstDao
import white.box.reins.model.ListData

/**
 * Twitterアカウントのリストから定期的に画像のURLを取得する。<br>
 *
 * @author seri
 */
class TwitterWatcher extends Thread {

	def config = null

	private boolean loop = true

	TwitterWatcher(def config) {
		this.config = config
	}

	/**
	 * スレッド停止用メソッド<br>
	 * スレッド作成元のスレッドで呼ぶように作ること。
	 */
	public void stopRunning(){
		loop = false;
	}

	@Override
	public void run() {

		Twitter twitter = TwitterFactory.getSingleton()

		// 先にユーザ情報を取り、これを使いまわす
		def userinfo = twitter.verifyCredentials()

		Sql db = Sql.newInstance(ReinsConstants.JDBC_MAP)
		def listMstDao = new ListMstDao(db)
		def listDataDao = new ListDataDao(db)

		final int tweet_max_count = config.reins.tweet.maxcount

		// リストごとにスリープ、リスト終わって長めのスリープ
		int waittime = config.reins.loop.waittime

//		int counter = 0		// TODO:テスト用
//		while(loop && counter++ < 3) {
		while(loop) {

			// 認証ユーザが持つリストを取得
			ResponseList<UserList> lists = twitter.getUserLists(userinfo.getScreenName())

			// TODO:list_mstが持つリスト名をどこかのタイミングで更新するようにすること
			// TODO:リストのブラック、ホワイトリストを持つようにした場合、そこのチェックタイミングで更新すること


			// リストごとに情報を取得
			Paging paging = new Paging(1, tweet_max_count)
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
					paging = new Paging(1, tweet_max_count)
				}

				println "$listname current since_id:" + currentSinceId

				// 最大200×10ツイートの取得
				for (int i=1; i <= paging_max_count; i++) {
					paging.page = i
					ResponseList<Status> statuses = twitter.getUserListStatuses(listId, paging)

					if (statuses == null || statuses.size() == 0) {
						break
					}

					for (Status status : statuses) {
//					statuses.each { status ->

						def screenName = status.getUser().getScreenName()
						// RTの場合はRT元のユーザー名を格納する
						if (status.getRetweetedStatus() != null) {
							screenName = status.getRetweetedStatus().getUser().getScreenName()
						}
						// 共通的な値はあらかじめ入れる
						ListData listdata = new ListData(
										screenName : screenName,
										counterStatus : 0,
										statusId : status.getId(),
										tweetDate : status.getCreatedAt())

						// media_urlならTwitter公式、それ以外は別形式で保存
						def mediaList = status.getMediaEntities()
						mediaList.each {
							listdata.url = it.getMediaURL()
							listdata.attribute = "twitter"

							listDataDao.insert(listId, listdata)
						}
						// ----------■公式以外のリンクを取得する場合はここに書く--------------
						def urlsList = status.getURLEntities()
						urlsList.each {
							// pixivリンクの保存
							if(it.getExpandedURL() =~ """www.pixiv.net/member_illust.php""") {
								listdata.url = it.getExpandedURL()
								listdata.attribute = "pixiv"
								listDataDao.insert(listId, listdata)
							}
							// gifの保存
							else if (it.getExpandedURL() =~ /.gif$/) {
								listdata.url = it.getExpandedURL()
								listdata.attribute = "gif"
								listDataDao.insert(listId, listdata)
							}
						}
						// ---------------------------------------------------------------------
					}

					if (i==1) {
						// since_idの保持
						listMstDao.updateSinceId(listId, statuses.get(0).getId())
					}
				}

				// リストごとにちょっと待つ
				Thread.sleep(waittime * 10)
			}

			// 1周したら結構待つ
			Thread.sleep(waittime * 600)
		}
	}
}
