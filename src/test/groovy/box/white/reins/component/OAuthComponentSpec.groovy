package box.white.reins.component

import box.white.reins.ReinsConstants
import box.white.reins.dao.ReinsMstDao
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import spock.lang.Ignore
import spock.lang.Specification

import java.awt.Desktop

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

    @Ignore
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
