package white.box.reins.component

import static white.box.reins.util.StringUtil.*
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import white.box.reins.ReinsConstants
import white.box.reins.dao.ReinsMstDao
import white.box.reins.util.WebUtil

@Slf4j
class OAuthComponent {

	private def db = null
	private ReinsMstDao reinsmst = null

	private static final String ACCESS_TOKEN = "accessToken"
	private static final String ACCESS_TOKEN_SECRET = "accessTokenSecret"


	OAuthComponent() {
		db = Sql.newInstance(ReinsConstants.JDBC_MAP)
		reinsmst = new ReinsMstDao(db)
	}

	/**
	 * 認証済みかどうかを確認する。<br>
	 * Twitterの連携アプリ認証が取り消されていた場合、falseを返す。
	 *
	 * @return 認証済みの場合、true
	 */
	public boolean isAuthorized() {

		String access_token = reinsmst.getValue(ACCESS_TOKEN)
		String access_token_secret = reinsmst.getValue(ACCESS_TOKEN_SECRET)
		log.debug "access token: " + access_token
		log.debug "access token secret: " + access_token_secret
		if (isBlank(access_token) || isBlank(access_token_secret)) {
			log.debug "access token not exist."
			return false
		}
		else {
			// 認証可能なAccessTokenを保持しているかを確認
			TwitterFactory factory = new TwitterFactory()
			AccessToken accessToken = new AccessToken(access_token, access_token_secret)
			Twitter twitter = factory.getInstance()
			twitter.setOAuthAccessToken(accessToken)

			try {
				twitter.verifyCredentials()
			} catch (TwitterException te) {
				if (te.getStatusCode() == 401) {
					log.debug "access token not enabled."
					return false
				}
			}
		}
		return true
	}

	/**
	 * OAuth認証を行う
	 */
	public void authorize() {

		Twitter twitter = TwitterFactory.getSingleton()
		log.debug "OAuth authentication start."

		RequestToken requestToken = twitter.getOAuthRequestToken()
		log.debug "request token: " + requestToken

		AccessToken accessToken = null
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in))

		while (null == accessToken) {
			// ブラウザで認証ページを表示
			WebUtil.viewUrlPage(requestToken.getAuthorizationURL())

			print "Enter the PIN(if aviailable) or just hit enter.[PIN]:"
			String pin = br.readLine()

			try {
				if (pin.length() > 0) {
					accessToken = twitter.getOAuthAccessToken(requestToken, pin)
				}
				else {
					accessToken = twitter.getOAuthAccessToken()
				}
			}
			catch (TwitterException te) {
				if (te.getStatusCode() == 401) {
					println "Unable to get the access token."
				}
				else {
					te.printStackTrace()
					log.error "OAuth Error:0001"
					throw new RuntimeException("OAuth authentication failed. please retry it.")
				}
			}
		}

		// 取得したAccess Tokenで認証チェック
		twitter.setOAuthAccessToken(accessToken)
		try {
			twitter.verifyCredentials()
		} catch (TwitterException te) {
			if (te.getStatusCode() == 401) {
				log.error "OAuth Error:0002"
				throw new RuntimeException("access token not enabled. please retry it.")
			}
		}

		// accessTokenを永続化
		def db = Sql.newInstance(ReinsConstants.JDBC_MAP)
		def reinsMstDao = new ReinsMstDao(db)
		log.debug "accessToken : " + accessToken.getToken()
		log.debug "accessTokenSecret : " + accessToken.getTokenSecret()
		checkKeyAndInsertUpdate(reinsMstDao, ACCESS_TOKEN, accessToken.getToken())
		checkKeyAndInsertUpdate(reinsMstDao, ACCESS_TOKEN_SECRET, accessToken.getTokenSecret())
	}

	/**
	 * keyがレコードにあれば更新、なければ挿入
	 *
	 * @param dao
	 * @param key
	 * @param value
	 * @return
	 */
	def checkKeyAndInsertUpdate(def dao, String key, String value) {

		if(dao.findKey(key)) {
			dao.updateValue(key, value)
		}
		else {
			dao.insert(key, value)
		}
	}


}
