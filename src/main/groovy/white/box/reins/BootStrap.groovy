package white.box.reins

import groovy.sql.Sql
import white.box.reins.dao.ListMstDao
import white.box.reins.model.ListMst

class BootStrap {

	def init = { config ->
		config = new ConfigSlurper().parse(
						new File('./src/main/resources/Config.groovy').toURI().toURL())

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

		//		ListMst[] insertListMstDatas = [
		//			new ListMst()
		//			]

		db.close()

		config
	}

	def destroy = { config ->

		def db = Sql.newInstance(config.jdbcMap)

//		def listMstDao = new ListMstDao(db)
//		listMstDao.drop()

		db.execute('DROP DATABASE h2.db')
		db.close()
	}
}