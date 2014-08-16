package white.box.reins

import groovy.sql.Sql

/**
 * 実行クラス
 *
 * @author seri
 */
class Main {

	static void main(String[] args) {

		def config = new BootStrap().init()

//		new BootStrap().destroy(config)

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