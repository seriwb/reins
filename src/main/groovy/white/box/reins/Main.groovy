package white.box.reins

import groovy.sql.Sql
import white.box.reins.component.OAuthComponent

/**
 * 実行クラス
 *
 * @author seri
 */
class Main {

	static void main(String[] args) {

		def bs = new BootStrap()
		def config = bs.init()

//		bs.destroy()

		def oauth = new OAuthComponent()
		if (!oauth.isAuthorized()) {
			oauth.authorize()
		}

		TwitterWatcher tw = new TwitterWatcher(config)
		ImageGetter ig = new ImageGetter(config)

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run () {
				tw.stopRunning()
				ig.stopRunning()

				println "exit."
			}
		});

		try {
			tw.start()
			ig.start()
		} catch (e) {
			println e
		}
	}
}