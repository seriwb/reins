package white.box.reins

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import white.box.reins.dao.ListMstDao
import white.box.reins.dao.ReinsMstDao
import static white.box.reins.util.StringUtil.*
import white.box.reins.model.ListMst

/**
 * 初期化用クラス<br>
 * initで以下の処理を行う。
 * <ul>
 * <li>conf/Config.groovyに記載された設定値の読み込み</li>
 * <li>DBの初期データ作成</li>
 * </ul>
 * 
 * 各クロージャはconfigを受け取り、configを返す。
 * 
 * @author seri
 */
@Slf4j
public class BootStrap {

	def init = { config ->
		// 1---------- 設定値の初期化 ---------------
		config = new ConfigSlurper().parse(
						new File('./conf/Config.groovy').toURI().toURL())

		Map<String, Object> jdbcMap = [
			url:'jdbc:h2:./db/h2.db',		// DB接続文字列（永続化）TODO:フォルダ指定
			user:'sa',						// ユーザ名
			password:'',					// パスワード
			driver:'org.h2.Driver'			// H2 JDBCドライバ
		]
		config.put('jdbcMap', jdbcMap)
		
		// 2---------- DBの初期化 ---------------
		def db = Sql.newInstance(jdbcMap)

		// マスタ系テーブルの作成
		def reinsMstDao = new ReinsMstDao(db)
		try {
			reinsMstDao.create()
		} catch (e) {
			log.info "reins_mst already exist."
		}
		
		def listMstDao = new ListMstDao(db)
		try {
			listMstDao.create()
		} catch (e) {
			log.info "list_mst already exist."
		}

		db.close()

		config
	}

	/**
	 * OAuth認証の処理を行う
	 */
	def oauth = { config ->

		Twitter twitter = TwitterFactory.getSingleton()
		log.debug "OAuth start."
		
		RequestToken requestToken = twitter.getOAuthRequestToken()
		log.debug "request token: " + requestToken
		
		AccessToken accessToken = null
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
		
		while (null == accessToken) {
			
		}
		println "prin"

		
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

		config
	}
}
