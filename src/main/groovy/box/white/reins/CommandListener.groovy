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
class CommandListener extends ManagedThread {

    /** システム設定値 */
    def config = null

    /** コマンドクラス */
    final Set<String> COMMAND_SETS = [
            "logout",
            "url",
            "refresh",
            "quit",
            "stop",
            "help"
    ]

    Sql db = null
    ListDataDao listDataDao = null
    BufferedReader br = null

    /**
     * コンストラクタ<br>
     * ・各コマンドクラスは初期化時にインスタンス化し、それを利用する。<br>
     * ・各コマンドクラスにはConfigを渡す。<br>
     * @param config システム設定値
     */
    CommandListener(config) {
        super()
        this.config = config
        // TODO:コマンドクラスのインスタンスを生成してMapに格納する
    }

    @Override
    void preProcess() {
        db = Sql.newInstance(ReinsConstants.JDBC_MAP)
        listDataDao = new ListDataDao(db)
        br = new BufferedReader(new InputStreamReader(System.in))
    }

    @Override
    void mainProcess() {
        // OAuth認証との衝突を避けるため、コマンド入力は標準入力にあるときだけreadLineを呼ぶ
        String command = null
        while (!command) {
            if (br.ready()) {
                command = br.readLine()
            }
            sleep(100);
        }

        if (StringUtil.isBlank(command)) {
            return
        }
        String[] commandInfo = command.split(/\s/)

        if (commandInfo[0] == "url") {
            urlCommand(commandInfo)
        }
    }

    @Override
    void postProcess() {
        if (br != null) {
            br.close()
        }
        listDataDao = null
        db = null
    }

    /**
     * @param commandInfo
     */
    protected void urlCommand(String[] commandInfo) {
        try {
            Set<String> imageUrls = null
            if (commandInfo.length > 1 && commandInfo[1] != "-o") {
                // 画像ファイルの解析
                imageUrls = createImageUrl(commandInfo[1])
            } else if (commandInfo.length > 2 && commandInfo[1] == "-o") {
                imageUrls = createImageUrl(commandInfo[2])
                imageUrls.each { WebUtil.viewUrlPage(it) }
            }
            // コンソール出力
            imageUrls.each { println(it) }
        } catch (e) {
            log.error(e)
        }
    }

    /**
     * @param imageName
     * @return
     */
    Set<String> createImageUrl(String imageName) {
        if (StringUtil.isBlank(imageName)) {
            throw new IllegalArgumentException("画像ファイル名が指定されていません")
        }

        Set<String> urlSet = new HashSet<>()
        List<GroovyRowResult> results = listDataDao.findTwitterUrl(imageName)

        for (def result : results) {
            urlSet.add(WebUtil.getTwitterUrl(result.get("screenName"), result.get("statusId")))
        }
        urlSet
    }
}
