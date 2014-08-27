package reins

import groovy.util.logging.Slf4j
import java.awt.Desktop
import spock.lang.Ignore
import spock.lang.Specification
import white.box.reins.BootStrap

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

	def 文字入力確認() {
		setup:

		println "実施直前"
		Scanner scan = new Scanner(System.in)
		println "実施直後"

		println "メソッド実行：" + scan.next()

	}
}
