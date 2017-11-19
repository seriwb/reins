package box.white.reins.dao

/**
 * reinsのデータを扱うDAOで必要なインターフェース
 *
 * 画像処理で利用する
 */
interface DataDao {

    def getImageInfo(String tablename, String attribute, int max)
    def updateImageName(String tablename, long id, String imageName)
    def updateStatus(String tablename, Map imageInfo)
}