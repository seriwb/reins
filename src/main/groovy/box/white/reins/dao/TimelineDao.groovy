package box.white.reins.dao

import box.white.reins.model.Timeline
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

/**
 * timelineテーブルを操作するDAO
 *
 * @author seri
 */
class TimelineDao implements DataDao {

    Sql db = null

    TimelineDao(Sql db) {
        this.db = db
    }

    def create() {

        db.execute("""create table if not exists timeline (
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

    def insert(Timeline timeline) {

        def listSet = db.dataSet("timeline")

        try {
            listSet.add(timeline.getProperties().findAll {
                !(it.key in ['id', 'class'])
            })
        } catch (e) {
            // 一意制約なので特に何もしない
        }
    }

    /**
     * ダウンロード画像データ取得
     * TODO:do test
     * @return
     */
    @Override
    def getImageInfo(String tablename, String attribute, int max) {
        db.rows("""select id, imageUrl, screenName, retweetUser, counterStatus, statusId, tweetDate
                 | from $tablename
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
    @Override
    def updateStatus(String tablename, Map imageInfo) {
        db.execute(
                "update $tablename set counterStatus = ${imageInfo.counterStatus} where id = ${imageInfo.id}".toString())
    }

    @Override
    def updateImageName(String tablename, long id, String imageName) {
        db.execute(
                "update $tablename set imageName = '$imageName' where id = $id".toString())
    }

    /**
     * @param id テーブルのID
     */
    def find(long id) {
        return db.firstRow("""select count(*) from timeline where id = $id""".toString()).get("count(*)")
    }

    def countAll() {
        return db.firstRow("""select count(*) from timeline""".toString()).get("count(*)")
    }

    def findTwitterUrl(String imageName) {

        List<GroovyRowResult> results = db.rows(
                """select screenName, statusId from timeline where imageName = '$imageName'""".toString())
        if (results != null && results.size() > 0) {
            return results
        }
        results
    }

    def delete() {
        // TODO:later
    }

    def drop(long listId) {
        db.execute("""drop table timeline""".toString())
    }
}
