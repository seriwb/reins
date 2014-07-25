package white.box.reins

import groovy.sql.Sql
import twitter4j.Paging
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UserList;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import white.box.reins.dao.ListDataDao
import white.box.reins.dao.ListMstDao
import white.box.reins.model.ListData

class TwitterWatcher extends Thread {

	def config = null

	TwitterWatcher(def config) {
		this.config = config
	}

	@Override
	public void run() {

		Twitter twitter = TwitterFactory.getSingleton()

		// 先にユーザ情報を取り、これを使いまわす
		def userinfo = twitter.verifyCredentials()

		Sql db = Sql.newInstance(config.jdbcMap)
		def listMstDao = new ListMstDao(db)
		def listDataDao = new ListDataDao(db)

		final int tweet_max_count = config.reins.tweet.maxcount

		// リストごとにスリープ、リスト終わって長めのスリープ
		boolean loop = true
		int counter = 0
		while(loop && counter++ < 4) {

			// 認証ユーザが持つリストを取得
			ResponseList<UserList> lists = twitter.getUserLists(userinfo.getScreenName())

			// リストごとに情報を取得
			Paging paging = new Paging(1, tweet_max_count)
			lists.each { list ->

				// list_idでマスタを探し、存在していればテーブルを作成しない。存在しなければ作成する。
				long listId = list.getId()
				String listname = list.getName()

				if (!listMstDao.find(listId)) {
					listMstDao.insert(listId, listname)
					listDataDao.create(listId)
				}

				// --------------- ツイート取得して解析 -----------------
				ResponseList<Status> statuses = twitter.getUserListStatuses(listId, paging)
				statuses.each { status ->

					def screenName = status.getUser().getScreenName()
					// 共通的な値はあらかじめ入れる
					ListData listdata = new ListData(
									screenName:screenName,
									counterStatus:0,
									tweetUrl:"https://twitter.com/${screenName}/status/${status.getId()}",
									tweetDate:status.getCreatedAt())

					//					String text = status.getText()

					// media_urlならTwitter公式、それ以外は別形式で保存
					def mediaList = status.getMediaEntities()
					mediaList.each {
						listdata.url = it.getMediaURL()
						listdata.attribute = "twitter"
						listDataDao.insert(listId, listdata)
					}
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
				}

				// リストごとにちょっと待つ
				Thread.sleep(1000)
			}

			// 1周したら結構待つ
			Thread.sleep(3000)

			// TODO:エンド処理
//			loop = false
		}
	}
















	//		List<Status> statuses = twitter.getHomeTimeline()
	//
	//		statuses.each {
	//			status ->
	//			println(status.getUser().getName() + ":" + status.getText());
	//		}


	// TODO:リンクをリストに取得（URL、ユーザ名、日時

	// TODO:body直下がimgタグだったらsrcを取ればいい
	// TODO:pixivのまとまったやつはimgタグのdata-srcを取ればよい

	//		.each {
	//			it.each {
	//				println "id:" + it.getId() + ", name:" + it.getName() + ", description:" + it.getDescription() + ", slug:" + it.getSlug() + " /end"
	//			}
	//		}
	def getList(UserList list) {
	}


}
