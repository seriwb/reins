package box.white.reins

final class ReinsConstants {

    /**
     * JDBCプロパティ
     */
    static final Map<String, Object> JDBC_MAP = [
            url     : 'jdbc:h2:./db/h2.db',
            user    : 'sa',
            password: '',
            driver  : 'org.h2.Driver'
    ]

    /**
     * 保持しているタイムラインのsince_idを取得するキー値
     */
    static final String TIMELINE_SINCEID = "timeline_sinceid"
}
