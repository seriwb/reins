package white.box.reins.dao

import groovy.sql.Sql

/**
 * list_mstを操作するDAO
 *
 * @author seri
 */
class ListMstDao {

	Sql db = null

	ListMstDao(Sql db) {
		this.db = db
	}

	def create() {
		db.execute("""create table if not exists list_mst(
					| id bigint auto_increment not null primary key,
					| listId bigint unique,
					| listName varchar2(200),
					| sinceId bigint)""".stripMargin())
	}

	def insert(long listId, String listname) {

		def listSet = db.dataSet("list_mst")

		listSet.add([listId : listId, listName : listname])
	}

	def selectAll() {
		db.rows("""select count(*) from list_mst""")
	}

	def getSinceId(long listId) {
		def res = db.firstRow("""select sinceId from list_mst where listId = $listId""")
		if (res == null) {
			return ""
		}
		else {
			return res.get("sinceId")
		}
	}

	/**
	 * 全リスト情報の取得
	 *
	 * @return listIdとlistNameのrows
	 */
	def getListAll() {
		db.rows("""select listId, listName from list_mst""")
	}

	def updateSinceId(long listId, long sinceId) {
		db.execute("""update list_mst set sinceId = $sinceId where listId = $listId""")
	}

	/**
	 * @param id
	 * @return データがあればtrueを返す
	 */
	def find(long listId) {
		db.firstRow("""select count(*) from list_mst where listId = $listId""").get("count(*)") > 0
	}

	def delete() {
	}

	def drop() {
		db.execute("""drop table list_mst""")
	}
}
