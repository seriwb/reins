package reins.dao

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import groovy.sql.Sql;
import white.box.reins.dao.ListDataDao
import white.box.reins.model.ListData;

class ListDataDaoSpec extends Specification {


	Map<String, Object> jdbcMap = [
		url:'jdbc:h2:./db/test.db',		// DB接続文字列（永続化）TODO:フォルダ指定
		user:'sa',						// ユーザ名
		password:'',					// パスワード
		driver:'org.h2.Driver'			// H2 JDBCドライバ
	]
	def db = Sql.newInstance(jdbcMap)
	ListDataDao dao = new ListDataDao(db)
	// テスト用
	long listId = 9000999

	@Ignore('一度テストすれば以降は基本不要')
	def testCreateAndDrop() {

		setup:
		dao.create(listId)
		dao.drop(listId)
		dao.create(listId)

		cleanup:
		dao.drop(listId)
	}

	def testInsert() {

		setup:
		dao.create(listId)

		ListData listData = new ListData(
						screenName:"name",
						counterStatus:0,
						url : "http://hoge",
						attribute : "twitter",
						tweetUrl:"https://twitter.com/",
						tweetDate:new Date())

		//		println listData.getProperties().findAll{
		//			!(it.key in ['id', 'url', 'class'])
		//		}.collect { "${it.key} = \"${it.value}\""}.join(", ")

		//		String setstr = listData.getProperties().findAll{
		//			!(it.key in ['id', 'url', 'class'])
		//		}.collect { "${it.key} = \"${it.value}\""}.join(", ")
		//
		//		String sql = """insert into list_${listId} set ${setstr}"""
		//
		//		db.firstRow(sql)

		dao.insert(listId, listData)

		String tablename = "list_$listId"
		def result = db.firstRow("""select * from list_$listId where id = 1""".toString())

		expect:
		1                      == result.id
		listData.url           == result.url
		listData.screenName    == result.screenName
		listData.counterStatus == result.counterStatus
		listData.attribute     == result.attribute
		listData.tweetUrl      == result.tweetUrl
		listData.tweetDate.format("yyyy-MM-dd HH:mm:ss.SSSZ") ==
			result.tweetDate.format("yyyy-MM-dd HH:mm:ss.SSSZ")

		cleanup:
		dao.drop(listId)

	}
}
