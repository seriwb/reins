package white.box.reins.dao

import groovy.sql.Sql;
import white.box.reins.model.ListData

class ListDataDao {

	Sql db = null

	ListDataDao(Sql db) {
		this.db = db
	}

	def create(long listId) {

		db.execute("""
create table list_${listId} (
		id bigint auto_increment not null primary key,
		url varchar2(300),
		screenName varchar2(50),
		counterStatus integer,
		attribute varchar2(10),
		tweetUrl varchar2(300),
		tweetDate datetime)""".toString())
	}

	def insert(long listId, ListData listData) {

		String tablename = "list_${listId}"
		def listSet = db.dataSet(tablename)

		listSet.add(listData.getProperties().findAll {
			!(it.key in ['id', 'class'])
		})
	}

	/**
	 * @param listId リストのID
	 * @param id リストテーブルのID
	 * @return データがあればtrueを返す
	 */
	def find(long listId, long id) {
		println db.firstRow("""select count(*) from list_${listId} where id = $id""".toString()).get("count(*)")
	}

	def delete() {
	}

	def drop(long listId) {
		db.execute("""drop table list_${listId}""".toString())
	}
}
