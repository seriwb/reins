package box.white.reins

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import box.white.reins.dao.ListDataDao
import box.white.reins.util.StringUtil
import box.white.reins.util.WebUtil

/**
 * reinsコマンドを処理するクラス。<br>
 *
 * ・利用可能なコマンドのリストを取得する。<br>
 * ・リストにあるコマンドのクラスが存在しなければエラーとする。<br>
 * ・コマンドクラスで許可していないパラメータ数が指定された場合、エラーメッセージを出力する。<br>
 * ・基底クラスである程度処理をまとめる<br>
 *
 * @author seri
 */
@Slf4j
class CommandListener extends Thread {

	/** システム設定値 */
	def config = null

	/** コマンドクラス */
	final Set COMMAND_SETS = ["logout", "url", "refresh", "quit", "stop"]

	/** ループ処理の継続判定用 */
	private volatile boolean loop = true

	ListDataDao listDataDao = null

	/**
	 * コンストラクタ<br>
	 * ・各コマンドクラスは初期化時にインスタンス化し、それを利用する。<br>
	 * ・各コマンドクラスにはConfigを渡す。<br>
	 * @param config システム設定値
	 */
	CommandListener(config) {

		this.config = config
		// TODO:コマンドクラスのインスタンスを生成してMapに格納する
	}

	/**
	 * スレッド停止用メソッド<br>
	 * スレッド作成元のスレッドで呼ぶように作ること。
	 */
	void stopRunning() {
		loop = false
	}

	@Override
	void run() {

		Sql db = Sql.newInstance(ReinsConstants.JDBC_MAP)
		listDataDao = new ListDataDao(db)

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in))

		while (loop) {

			String command = br.readLine()
			if (StringUtil.isBlank(command)) {
				continue
			}
			String[] commandInfo = command.split(/\s/)

			if (commandInfo[0] == "url") {
				try {

					Set<String> imageUrls = null
					if (commandInfo.length > 1 && commandInfo[1] != "-o") {
						// 画像ファイルの解析
						imageUrls = createImageUrl(commandInfo[1])
					} else if (commandInfo.length > 2 && commandInfo[1] == "-o") {
						imageUrls = createImageUrl(commandInfo[2])
						imageUrls.each {
							WebUtil.viewUrlPage(it)
						}
					}
					// コンソール出力
					imageUrls.each {
						println(it)
					}

				} catch (e) {
					log.error(e)
				}
			}
		}
	}

	Set<String> createImageUrl(String imageName) {

		if (StringUtil.isBlank(imageName)) {
			throw new IllegalArgumentException("画像ファイル名が指定されていません")
		}

		Set<String> urlSet = new HashSet<>()
		List<GroovyRowResult> results = listDataDao.findTwitterUrl(imageName)

		for (def result: results) {
			urlSet.add(WebUtil.getTwitterUrl(result.get("screenName"), result.get("statusId")))
		}
		urlSet
	}
}
