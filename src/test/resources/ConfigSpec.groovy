import spock.lang.Specification;

class ImageGetterSpeck extends Specification {

	def 設定値の読み込み確認() {

		setup:
		// 設定値
		def config = new ConfigSlurper().parse(
			new File('./conf/config.txt').toURI().toURL())

		assert config != null
		assert config.reins.image.dir == "./dir"

	}
}
