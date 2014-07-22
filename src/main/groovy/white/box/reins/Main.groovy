package white.box.reins

//import groovyx.net.http.HttpURLClient
import twitter4j.Paging
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UserList;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

class Main {

	static void main(String[] args) {

		Twitter twitter = TwitterFactory.getSingleton()

//		List<Status> statuses = twitter.getHomeTimeline()
//
//		statuses.each {
//			status ->
//			println(status.getUser().getName() + ":" + status.getText());
//		}

		// 先にユーザ情報を取り、これを使いまわす
		def userinfo = twitter.verifyCredentials()


		
		// TODO:ここにループを作成する
		// 認証ユーザが持つリストを取得
		ResponseList<UserList> lists = twitter.getUserLists(userinfo.getScreenName())

		// TODO:リストごとに情報を取得
		Paging paging = new Paging(1, 30)
		lists.each { list ->
			long id = list.getId()

			String listname = list.getName()
			// TODO:リストでフォルダを作成する（やるのは画像取得ロジック
			
			
			println listname

			int counter = 0
			ResponseList<Status> statuses = twitter.getUserListStatuses(id, paging)
			statuses.each { status ->

				// TODO:画像があればユーザーでフォルダを作成する。
				println status.getUser().getScreenName()

//				String text = status.getText()
				def mediaList = status.getMediaEntities()
				mediaList.each {
					println it.getMediaURL()

				}
				def urlsList = status.getURLEntities()
				urlsList.each {
					// pixivリンクの保存
					if(it.getExpandedURL() =~ """www.pixiv.net/member_illust.php""") {
						println it.getExpandedURL()
					}
					// gifの保存
					else if (it.getExpandedURL() =~ """.gif""") {
						println it.getExpandedURL()
					}
				}
				println "---------------${counter++}"


			}
		}


		// TODO:リンクをリストに取得（URL、ユーザ名、日時

		// TODO:body直下がimgタグだったらsrcを取ればいい
		// TODO:pixivのまとまったやつはimgタグのdata-srcを取ればよい

//		.each {
//			it.each {
//				println "id:" + it.getId() + ", name:" + it.getName() + ", description:" + it.getDescription() + ", slug:" + it.getSlug() + " /end"
//			}
//		}
	}


	def getList(UserList list) {
	}


//	public String expandQueryURL(String urlStr, String queryStr) {
//		HttpURLClient http = new HttpURLClient(followRedirects:false)
//
//		def params = [ url:urlStr,
//				query:[e:queryStr],
//				headers:['User-Agent':'Mozilla/5.0'] ]
//		def resp = http.request( params )
//
//		return resp.headers.'Location'
//	  }
}