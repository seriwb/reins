package white.box.reins

import groovy.sql.Sql
import white.box.reins.dao.ListMstDao
import white.box.reins.model.ListMst

/**
 * 初期化用クラス
 *
 * @author seri
 */
class BootStrap {

	def init = { config ->
		config = new ConfigSlurper().parse(
						new File('./conf/Config.groovy').toURI().toURL())

		Map<String, Object> jdbcMap = [
			url:'jdbc:h2:./db/h2.db',		// DB接続文字列（永続化）TODO:フォルダ指定
			user:'sa',						// ユーザ名
			password:'',					// パスワード
			driver:'org.h2.Driver'			// H2 JDBCドライバ
		]
		config.put('jdbcMap', jdbcMap)

		def db = Sql.newInstance(jdbcMap)

		// マスタ系テーブルの作成
		def listMstDao = new ListMstDao(db)
		try {
			listMstDao.create()
		} catch (e) {
			println "list_mst already exist."
		}

		db.close()

		config
	}

	/**
	 * 環境初期化用
	 */
	def destroy = { config ->

		def db = Sql.newInstance(config.jdbcMap)

		try {
			println "search tables"
			def rows = db.rows("select listId from list_mst")

			rows.each {
				String tablename = """list_${it.get("listId")}"""
				String sql = "drop table $tablename"
				println sql
				db.execute(sql)
			}

			println "drop table list_mst"
			db.execute('drop table list_mst')
		} catch (e) {
			println "end"
		}

		db.close()
	}
}
