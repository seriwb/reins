package box.white.reins

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import box.white.reins.dao.ListDataDao
import box.white.reins.dao.ListMstDao
import box.white.reins.util.StringUtil
import box.white.reins.util.WebUtil

/**
 * DB内のデータから画像をダウンロードする。<br>
 *
 * 複数のテーブルを逐次的に走査し、未ダウンロードの画像があれば取得する。
 *
 * @author seri
 */
@Slf4j
class ImageGetter extends Thread {

	def config = null			// 設定値
	String dirpath = null		// フォルダ作成先パス

	private volatile boolean loop = true

	/** リストマスタ参照用のDAO */
	ListMstDao listMstDao = null

	/** リストデータの作成に利用するDAO */
	ListDataDao listDataDao = null

	ImageGetter(config) {
		this.config = config

		dirpath = this.config.reins.image.dir
		if (dirpath == null || dirpath == "") {
			dirpath = "./dir"
		}
	}

	final def IMAGE_SET = ["png", "jpg", "bmp", "jpeg", "gif"]

	/**
	 * スレッド停止用メソッド<br>
	 * スレッド作成元のスレッドで呼ぶように作ること。
	 */
	void stopRunning(){
		loop = false
	}


	@Override
	void run() {

		Sql db = Sql.newInstance(ReinsConstants.JDBC_MAP)
		listMstDao = new ListMstDao(db)
		listDataDao = new ListDataDao(db)

		// スリープ
		int waittime = config.reins.loop.waittime

		while(loop) {

			// リスト一覧の取得
			def listInfos = listMstDao.getListAll()

			// リストが全然ないなら動きようがないですな
			if (listInfos == null || listInfos.size() == 0) {
				// リストができるまでしばらく待つ
				log.info "reins is working to create list information. wait 20000 ms until next search."
				sleep(20000)
				continue
			}
			else {

				def attributes = ["twitter", "gif"]		// TODO:取得対象
				attributes.each { attribute ->

					listInfos.collect {
						[ listId : it.get("listId"), listName : it.get("listName") ]
					}.each { listInfo ->

						log.info "listInfo:$listInfo"

						int max_getimage = 200	// 1テーブルが1回の動作で取得する回数はMAXを定めておく
						def imageInfos = listDataDao.getImageInfo(listInfo.listId, attribute, max_getimage)

						if (imageInfos == null || imageInfos.size() == 0) {
							log.info "no image url : ${listInfo.listName}"
						}
						else {
							imageInfos.collect {

								[
									id : it.get("id"),
									imageUrl : it.get("imageUrl"),
									screenName : it.get("screenName"),
									counterStatus : it.get("counterStatus"),
									statusId : it.get("statusId"),
									tweetDate : it.get("tweetDate"),
									retweetUser : it.get("retweetUser")
								]
							}.each { imageInfo ->

								// ユーザー単位の画像フォルダを作成する
								File dirpath = mkdir(listInfo.listName, imageInfo.screenName, imageInfo.retweetUser)
								File filepath = createFileName(dirpath, imageInfo)

								if (imageInfo.attribute != "pixiv") {
									// 拡張子が画像ファイルのものを対象にする
									if (filepath != null) {

										log.info "imageInfo:$imageInfo"

										// ファイル名をDBに保存
										listDataDao.updateImageName(
												listInfo.listId, imageInfo.id, filepath.getName())
										try {
											// Retweetをダウンロードするかの判定
											if (!StringUtil.isBlank(imageInfo.retweetUser) && config.reins.retweet.target) {
												WebUtil.download(imageInfo.imageUrl, filepath)
											} else if (StringUtil.isBlank(imageInfo.retweetUser)) {
												WebUtil.download(imageInfo.imageUrl, filepath)
											}
											// 終了を設定
											imageInfo.counterStatus = -1
										} catch (e) {
											// 施行回数を上げる
											imageInfo.counterStatus += 1
											log.warn "don't save [count ${imageInfo.counterStatus}] : ${imageInfo.imageUrl}"
											log.warn "->tweet url: " + WebUtil.getTwitterUrl(imageInfo.screenName, imageInfo.statusId)
										}
									}
								}
								listDataDao.updateStatus(listInfo.listId, imageInfo)
							}
						}
					}
				}
			}

			// 1周したら結構待つ
			log.info "image download completed. wait ${waittime / 2}s until next download process."
			sleep(waittime * 500)
		}
	}

	/**
	 * 画像の保存先ディレクトリを作成する
	 *
	 * @param list_name リスト名
	 * @param screen_name Twitterユーザ名
	 * @param retweet_user Retweetしたユーザ名
	 * @return ディレクトリパス
	 */
	File mkdir(String list_name, String screen_name, String retweet_user) {

		String targetDirPath = null
		File targetDir = null

		if (list_name == null || list_name.empty) {
			// Retweetの場合にディレクトリを分けるかどうかを判定
			if (!config.reins.retweet.separate || retweet_user == null || retweet_user.empty) {
				targetDirPath = "${dirpath}/${screen_name}"
			}
			else {
				targetDirPath = "${dirpath}/${retweet_user}/rt/${screen_name}"
			}
		} else {
			if (!config.reins.retweet.separate || retweet_user == null || retweet_user.empty) {
				targetDirPath = "${dirpath}/${list_name}/${screen_name}"
			}
			else {
				targetDirPath = "${dirpath}/${list_name}/${retweet_user}/rt/${screen_name}"
			}
		}

		targetDir = new File(targetDirPath)
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
		String[] strings = (imageInfo.imageUrl).split('\\.')
		String identifer = strings[strings.length - 1]
		String namestr = "_${imageInfo.screenName}.$identifer"

		if (!IMAGE_SET.contains(identifer) || identifer.length() > 5) {
			return null
		}
		File filepath = new File(dirpath, datestr.concat(namestr))

		// 重複していたら_9までファイル名を別のものを作成する（TODO:それ以上は無視）
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
