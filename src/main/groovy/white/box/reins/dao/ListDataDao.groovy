package white.box.reins.dao

import groovy.sql.GroovyRowResult
import groovy.sql.Sql;
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
					| id bigint unsigned auto_increment not null primary key,
					| imageUrl varchar2(300) unique,
					| imageName varchar2(75),
					| screenName varchar2(50),
					| retweetUser varchar2(50),
					| counterStatus integer,
					| attribute varchar2(10),
					| statusId bigint unsigned,
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
			println "一意制約なので特に何もしない"
		}
	}

	/**
	 * ダウンロード画像データ取得
	 * TODO:do test
	 * @return
	 */
	def getImageInfo(long listId, String attribute, int max) {
		db.rows("""select id, imageUrl, screenName, retweetUser, counterStatus, statusId, tweetDate
				| from list_${listId}
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
				"update list_${listId} set counterStatus = ${imageInfo.counterStatus} where id = ${imageInfo.id}".toString())
	}

	def updateImageName(long listId, long id, String imageName) {
		db.execute(
				"update list_${listId} set imageName = '$imageName' where id = $id".toString())
	}

	/**
	 * @param listId リストのID
	 * @param id リストテーブルのID
	 * @return データがあればtrueを返す
	 */
	def find(long listId, long id) {
		println db.firstRow("""select count(*) from list_${listId} where id = $id""".toString()).get("count(*)")
	}

	def findTwitterUrl(String imageName) {

		def listMstDao = new ListMstDao(db)
		List<GroovyRowResult> results = null
		def allList = listMstDao.getListAll()
		for (def list: allList) {
			long listId = list.get("listId")
			results = db.rows(
					"""select screenName, statusId from list_${listId} where imageName = '$imageName'""".toString())
			if (results != null && results.size() > 0) {
				return results
			}
		}
		results
	}

	def delete() {
		// TODO:later
	}

	def drop(long listId) {
		db.execute("""drop table list_${listId}""".toString())
	}
}
