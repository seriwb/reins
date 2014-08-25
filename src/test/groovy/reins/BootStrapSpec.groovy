package reins

import groovy.util.logging.Slf4j
import spock.lang.Specification
import white.box.reins.BootStrap

class BootStrapSpec extends Specification {

	def ConsumerKeysが正常に取得できているかを確認() {
		setup:
		def hoge = new BootStrap()
		hoge.oauth()
		println "hoge"
	}
}
