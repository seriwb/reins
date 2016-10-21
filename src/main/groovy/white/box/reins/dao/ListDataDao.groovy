package white.box.reins.dao

import groovy.sql.Sql
import white.box.reins.model.ListData

/**
 * list_<リストID>のテーブルを操作するDAO
 *
 * @author seri
 */
class ListDataDao {

	Sql db = null

	ListDataDao(Sql db) {
		this.db = db
	}

	def create(long listId) {

		db.execute("""create table if not exists list_${listId} (
					| id bigint auto_increment not null primary key,
					| url varchar2(300) unique,
					| screenName varchar2(50),
					| counterStatus integer,
					| attribute varchar2(10),
					| statusId bigint,
					| tweetDate datetime)""".stripMargin())
	}

	def insert(long listId, ListData listData) {

		String tablename = "list_${listId}"
		def listSet = db.dataSet(tablename)

		try {
			listSet.add(listData.getProperties().findAll {
				!(it.key in ['id', 'class'])
			})
		} catch (e) {
			// TODO:標準出力を辞める
			println e
			println "一意制約なので特に何もしない"
		}
	}

	/**
	 * ダウンロード画像データ取得
	 * TODO:do test
	 * @return
	 */
	def getImageInfo(long listId, String attribute, int max) {
		db.rows("""select id, url, screenName, counterStatus, statusId, tweetDate
				 | from list_$listId
				 | where (counterStatus between 0 and 5)
				 | and attribute = '$attribute'
				 | limit $max""".stripMargin())
	}

	/**
	 * ダウンロード後のステータス更新
	 *
	 * @param listId
	 * @param imageInfo
	 * @return
	 */
	def updateStatus(long listId, Map imageInfo) {
		db.execute(
			"update list_$listId".toString() +
			" set counterStatus = ${imageInfo.counterStatus} where id = ${imageInfo.id}")
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
		// TODO:later
	}

	def drop(long listId) {
		db.execute("""drop table list_${listId}""".toString())
	}
}
