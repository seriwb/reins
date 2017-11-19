package box.white.reins

import java.awt.Desktop

import spock.lang.Ignore
import spock.lang.Specification

class BootStrapSpec extends Specification {

    @Ignore
    def ConsumerKeysが正常に取得できているかを確認() {
        setup:
        def hoge = new BootStrap()
        hoge.oauth()
        println "hoge"
    }

    @Ignore
    def ブラウザ表示確認() {
        setup:
        Desktop desktop = Desktop.getDesktop()
        desktop.browse(new URI('https://github.com/seriwb'))
    }

    def keydataの取得() {

        setup:
        File keydata = new File("src/main/resources/box/white/reins/key.data")
        if (keydata.exists()) {
            println "あるよ！"
        } else {
            println new File('.').toURI().toString()
        }
    }

//	def 文字入力確認() {
//		setup:
//
//		println "実施直前"
//		Scanner scan = new Scanner(System.in)
//		println "実施直後"
//
//		println "メソッド実行：" + scan.next()
//
//	}
}
