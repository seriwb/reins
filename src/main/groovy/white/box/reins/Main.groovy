package white.box.reins

import groovy.sql.Sql

class Main {

	static void main(String[] args) {

		def config = new BootStrap().init()

//		new BootStrap().destroy(config)

		final TwitterWatcher tw = new TwitterWatcher(config)

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run () {
				tw.stopRunning()
			}
		});

		try {
			tw.start()
		} catch (e) {
			println e
		}
	}
}