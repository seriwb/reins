package white.box.reins

import static groovy.io.FileType.FILES
import groovy.sql.Sql

class ImageGetter extends Thread {

	def config = null			// 設定値
	String dirpath = null		// フォルダ作成先パス
	def db = null				// DBコネクション


	ImageGetter() {
		config = new ConfigSlurper().parse(
			new File('./src/main/resources/Config.groovy').toURI().toURL())

		dirpath = config.reins.image.dir
		if (dirpath == null || dirpath == "") {
			dirpath = "./dir"
		}

		db = Sql.newInstance(
			'jdbc:h2:./db/h2.db',		// DB接続文字列（永続化）TODO:フォルダ指定
			'sa',						// ユーザ名
			'',							// パスワード
			'org.h2.Driver'				// H2 JDBCドライバ
			)
	}

	@Override
	public void run() {

		// TODO:ディレクトリを作る処理はループの最初だけで呼ぶようにする
		// TODO:リストでフォルダを作成する（やるのは画像取得ロジック
		// TODO:画像があればユーザーでフォルダを作成する。


	}


	def mkdir(String list_name, String screen_name) {

//		def path = /C:\work/
//		new File("${path}/${new Date().format('yyyyMMdd')}").mkdir()

		def targetDir = null

		if (list_name == null || list_name.empty) {
			targetDir = new File("${dirpath}/${screen_name}")
		} else {
			targetDir = new File("${dirpath}/${list_name}/${screen_name}")
		}

		if (!targetDir.exists()) {
//			println targetDir.toURI().toString()
			targetDir.mkdirs()
		}
	}
}
