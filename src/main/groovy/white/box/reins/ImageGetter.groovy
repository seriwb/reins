package white.box.reins

import groovy.sql.Sql
import white.box.reins.dao.ListDataDao
import white.box.reins.dao.ListMstDao
import white.box.reins.model.ListMst
import white.box.reins.util.WebUtil

/**
 * DB内のデータから画像をダウンロードするクラス
 *
 * 課題：
 * ・テーブル複数あるがどう取得する？テーブル分スレッドを起こす？
 *
 * @author seri
 */
class ImageGetter extends Thread {

	def config = null			// 設定値
	String dirpath = null		// フォルダ作成先パス

	private boolean loop = true

	ImageGetter(def config) {
		this.config = config

		dirpath = this.config.reins.image.dir
		if (dirpath == null || dirpath == "") {
			dirpath = "./dir"
		}
	}

	/**
	 * スレッド停止用メソッド<br>
	 * スレッド作成元のスレッドで呼ぶように作ること。
	 */
	public void stopRunning(){
		loop = false;
	}


	@Override
	public void run() {

		Sql db = Sql.newInstance(ReinsConstants.JDBC_MAP)
		def listMstDao = new ListMstDao(db)
		def listDataDao = new ListDataDao(db)

		// スリープ
		int waittime = config.reins.loop.waittime

//		int counter = 0		// TODO:テスト用
//		while(loop && counter++ < 3) {
		while(loop) {

			// リスト一覧の取得
			def listInfos = listMstDao.getListAll()

			// リストが全然ないなら動きようがないですな
			if (listInfos == null || listInfos.size() == 0) {
				// リストができるまでしばらく待つ
				sleep(30000)
				continue
			}
			else {

				def attributes = ["twitter", "gif"]		// TODO:取得対象
				attributes.each { attribute ->

					listInfos.collect {
						[ listId : it.get("listId"), listName : it.get("listName") ]
					}.each { listInfo ->

						println "listInfo:$listInfo"

						int max_getimage = 200	// 1テーブルが1回の動作で取得する回数はMAXを定めておく
						def imageInfos = listDataDao.getImageInfo(listInfo.listId, attribute, max_getimage)

						if (imageInfos == null || imageInfos.size() == 0) {
							// TODO:debugに直す
							println "no image url : ${listInfo.listName}"
						}
						else {
							imageInfos.collect {
								[
									id : it.get("id"),
									url : it.get("url"),
									screenName : it.get("screenName"),
									counterStatus : it.get("counterStatus"),
									tweetDate : it.get("tweetDate")
								]
							}.each { imageInfo ->

								println "imageInfo:$imageInfo"

								// ユーザー単位の画像フォルダを作成する
								File dirpath = mkdir(listInfo.listName, imageInfo.screenName)
								File filepath = createFileName(dirpath, imageInfo)

								if (attribute != "pixiv") {
									try {
										WebUtil.download(imageInfo.url, filepath)
										// 終了を設定
										imageInfo.counterStatus = -1
									} catch (e) {
										println "don't save : ${imageInfo.url}"
										// 施行回数を上げる
										imageInfo.counterStatus += 1
									}
								}

								listDataDao.updateStatus(listInfo.listId, imageInfo)
							}
						}
					}
				}
			}

			// 1周したら結構待つ
			Thread.sleep(waittime * 300)
		}
	}

	/**
	 * 画像の保存先ディレクトリを作成する
	 *
	 * @param list_name リスト名
	 * @param screen_name Twitterユーザ名
	 * @retrun ディレクトリパス
	 */
	File mkdir(String list_name, String screen_name) {

		File targetDir = null

		if (list_name == null || list_name.empty) {
			targetDir = new File("${dirpath}/${screen_name}")
		} else {
			targetDir = new File("${dirpath}/${list_name}/${screen_name}")
		}

		if (!targetDir.exists()) {
			// println targetDir.toURI().toString()
			targetDir.mkdirs()
		}
		targetDir
	}

	/**
	 * Tweet情報から画像ファイル名を作成<br>
	 * ファイル名は「yyyymmdd-HHmmss(-2)_screenName.identifer」<br>
	 * 括弧部分は同一ツイートに複数の画像があった場合に利用する。<br>
	 *
	 * 例：20140726-002935-2_screen_name.jpg
	 *
	 * @return 画像ファイル名
	 */
	File createFileName(File dirpath, Map imageInfo) {

		String datestr = imageInfo.tweetDate.format("yyyyMMdd-HHmmss")
		String[] strings = (imageInfo.url).split('\\.')
		String identifer = strings[strings.length - 1]
		String namestr = "_${imageInfo.screenName}.$identifer"

		File filepath = new File(dirpath, datestr.concat(namestr))

		// 重複してたら_9までファイル名を別のものを作成する（TODO:それ以上は無視）
		if (filepath.exists()) {
			for (int i=2; i<10; i++) {
				String tempname = "-${i}_${imageInfo.screenName}.$identifer"

				File temppath = new File(dirpath, datestr.concat(tempname))
				if (!temppath.exists()) {
					filepath = temppath
					break
				}
			}
		}

		filepath
	}
}
