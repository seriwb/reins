package box.white.reins.dao

import groovy.sql.Sql

/**
 * reins_mstを操作するDAO<br>
 * reins_mst：reins共通マスタ<br>
 * key/valueでシステム共通のデータを保持する。<br>
 *
 * @author seri
 */
class ReinsMstDao {

	Sql db = null

	ReinsMstDao(Sql db) {
		this.db = db
	}

	/**
	 *
	 * @return
	 */
	def create() {
		db.execute("""create table if not exists reins_mst(
					| id bigint auto_increment not null primary key,
					| key varchar2(200),
					| value varchar2(200))""".stripMargin())
	}

	def insert(String key, String value) {

		def listSet = db.dataSet("reins_mst")
		listSet.add([key : key, value : value])
	}

	def countRows() {
		db.rows("""select count(*) from reins_mst""")
	}

	String getValue(String key) {
		def res = db.firstRow("""select value from reins_mst where key = $key""")
		if (res == null) {
			return ""
		}
		else {
			return res.get("value")
		}
	}

	/**
	 * 全情報の取得
	 *
	 * @return keyとvalueのrows
	 */
	def findAll() {
		db.rows("""select key, value from reins_mst""")
	}

	def updateValue(String key, String value) {
		db.execute("""update reins_mst set value = $value where key = $key""")
	}

	/**
	 * @param key キー
	 * @return データがあればtrueを返す
	 */
	def findKey(String key) {
		db.firstRow("""select count(*) from reins_mst where key = $key""").get("count(*)") > 0
	}

	def delete(String key) {
		db.execute("""delete reins_mst where key = $key""")
	}

	def drop() {
		db.execute("""drop table reins_mst""")
	}
}
