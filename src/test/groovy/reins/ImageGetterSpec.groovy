package reins

import spock.lang.Specification;
import white.box.reins.ImageGetter

class ImageGetterSpec extends Specification {

	def 起動テスト() {

		setup:
		ImageGetter imageGetter = new ImageGetter()
//		imageGetter.start()

		imageGetter.mkdir("h2", "hoge2")
	}
}