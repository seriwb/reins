package white.box.reins.dao

import groovy.sql.Sql

class ListMstDao {

	Sql db = null

	ListMstDao(Sql db) {
		this.db = db
	}

	def create() {
		db.execute("""
create table list_mst(
		id bigint auto_increment not null primary key,
		listId bigint,
		listName varchar2(200))""")
	}

	def insert(long listId, String listname) {

		def listSet = db.dataSet("list_mst")

		listSet.add([listId : listId, listName : listname])
	}

	def selectAll() {
		db.rows("""select count(*) from list_mst""")
	}

	/**
	 * @param id
	 * @return データがあればtrueを返す
	 */
	def find(long listId) {
		def val = db.firstRow("""select count(*) from list_mst where listId = $listId""").get("count(*)") > 0
	}

	def delete() {
	}

	def drop() {
		db.execute("""drop table list_mst""")
	}
}
