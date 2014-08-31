package reins.component

import groovy.sql.Sql
import groovy.util.logging.Slf4j

import java.awt.Desktop

import spock.lang.Ignore
import spock.lang.Specification
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import white.box.reins.ReinsConstants
import white.box.reins.component.OAuthComponent
import white.box.reins.dao.ReinsMstDao

@Slf4j
class OAuthComponentSpec extends Specification {

	private def db = Sql.newInstance(ReinsConstants.JDBC_MAP)
	private ReinsMstDao reinsmst = new ReinsMstDao(db)
	private static final String ACCESS_TOKEN = "accessToken"
	private static final String ACCESS_TOKEN_SECRET = "accessTokenSecret"

	@Ignore
	def ブラウザ表示確認() {
		setup:
		Desktop desktop = Desktop.getDesktop()
		desktop.browse(new URI('https://github.com/seriwb'))
	}

	@Ignore
	def 文字入力確認() {
		setup:

		println "実施直前"
		Scanner scan = new Scanner(System.in)
		println "実施直後"
		println "メソッド実行：" + scan.next()
	}

	def isAuthorized_AccessTokenなし() {

		setup:

		String access_token = reinsmst.getValue(ACCESS_TOKEN)
		String access_token_secret = reinsmst.getValue(ACCESS_TOKEN_SECRET)

		reinsmst.updateValue(ACCESS_TOKEN, "")
		reinsmst.updateValue(ACCESS_TOKEN_SECRET, "")

		def component = new OAuthComponent()

		expect:
		component.isAuthorized() == false

		cleanup:
		reinsmst.updateValue(ACCESS_TOKEN, access_token)
		reinsmst.updateValue(ACCESS_TOKEN_SECRET, access_token_secret)
	}
}
