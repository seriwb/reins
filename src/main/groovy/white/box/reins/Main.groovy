package white.box.reins

import groovy.sql.Sql

class Main {

	static void main(String[] args) {

		def config = new BootStrap().init()

		TwitterWatcher tw = new TwitterWatcher(config)

		try {
			tw.start()
		} catch (e) {
			println "exception"
		} finally {

			Sql db = Sql.newInstance(config.jdbcMap)
			db.close()
		}
	}
}