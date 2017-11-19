package box.white.reins

import box.white.reins.component.OAuthComponent
import box.white.reins.dao.ListDataDao
import box.white.reins.dao.ListMstDao
import box.white.reins.dao.ReinsMstDao
import box.white.reins.dao.TimelineDao
import box.white.reins.model.ListData
import box.white.reins.model.Timeline
import box.white.reins.util.FileUtil
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.UserList
import twitter4j.conf.ConfigurationBuilder

/**
 * Twitterアカウントのリストから定期的に画像のURLを取得する。<br>
 *
 * @author seri
 */
@Slf4j
class TwitterWatcher extends ManagedThread {

    def config = null

    /** Twitterインスタンス */
    Twitter twitter = null

    /** 1度に取得要求するTweet数 */
    final int TWEET_MAX_COUNT

    /** Tweetをたどるページングの回数 */
    final int PAGING_MAX_COUNT = 30

    /** sleepのベース時間：リスト毎は短め、チェック後は長め */
    final int WAIT_TIME

    /** リストマスタ参照用のDAO */
    ListMstDao listMstDao = null

    /** リストデータの作成に利用するDAO */
    ListDataDao listDataDao = null

    TimelineDao timelineDao = null
    ReinsMstDao reinsMstDao = null

    def userinfo = null
    Sql db = null

    List<String> allowList = null
    List<String> denyList = null

    /**
     * コンストラクタ<br>
     * Config値の設定を行う。
     *
     * @param config Config値
     */
    TwitterWatcher(config) {
        this.config = config

        TWEET_MAX_COUNT = config.reins.tweet.maxcount
        WAIT_TIME = config.reins.loop.waittime
    }

    @Override
    void preProcess() {
        // Twitter認証
        authenticationProcess()

        db = Sql.newInstance(ReinsConstants.JDBC_MAP)
        listMstDao = new ListMstDao(db)
        listDataDao = new ListDataDao(db)
        timelineDao = new TimelineDao(db)
        reinsMstDao = new ReinsMstDao(db)

        allowList = FileUtil.skipCommentLine(FileUtil.readLinesExcludeBlank("./conf/allow.txt"))
        denyList = FileUtil.skipCommentLine(FileUtil.readLinesExcludeBlank("./conf/deny.txt"))
    }


    @Override
    void mainProcess() {
        try {
            if (config.reins.timeline.target) {
                // タイムラインの画像URLの取得処理
                loopTimelineImageGetTask()
            }
            if (config.reins.list.target) {
                // リストの画像URLの取得処理
                loopListImageGetTask()
            }

            // 1周したら結構待つ
            log.info "list check completed. wait ${WAIT_TIME}s until next search."
            sleep(WAIT_TIME * 1000)
        }
        catch (TwitterException te) {
            log.error("Twitter service or network is unavailable.", te)
            log.info "Twitter service or network is unavailable. wait ${15} minutes until next search."
            println(te.getMessage())
            sleep(15 * 60 * 1000)
            authenticationProcess()     // エラー後なので一応認証チェックをする
        }
    }

    @Override
    void postProcess() {
        listDataDao = null
        listMstDao = null
        db = null
        userinfo = null
        twitter = null
    }

    /**
     * OAuth認証を行う
     * 認証処理でtwitterインスタンスの更新を行う
     *
     * @return 認証に成功した場合、true
     */
    protected void authenticationProcess() throws TwitterException {

        // Twitterオブジェクトの作成
        ConfigurationBuilder cb = new ConfigurationBuilder()
        String consumerKey = config.get("oauth.consumerKey")
        String consumerSecret = config.get("oauth.consumerSecret")
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
        TwitterFactory factory = new TwitterFactory(cb.build())
        twitter = factory.getInstance()

        // 認証
        def oauth = new OAuthComponent()
        oauth.setOAuthAccessToken(twitter)
        if (!oauth.isAuthorized(twitter)) {
            oauth.authorize(twitter)
        }

        // ユーザ情報を取得し、使いまわす
        userinfo = twitter.verifyCredentials()
    }

    /**
     * Twitterアカウントのタイムラインから画像のURLを取得し、DBに保存する
     *
     * @throws TwitterException
     */
    protected void loopTimelineImageGetTask() throws TwitterException {

        // 現在チェックしているところまでのsince_idを設定
        String sinceid = reinsMstDao.getValue(ReinsConstants.TIMELINE_SINCEID)
        long currentSinceId = sinceid ? Long.valueOf(sinceid) : -1

        // --------------- ツイート取得して解析 -----------------
        Paging paging = new Paging(1, TWEET_MAX_COUNT)
        if (currentSinceId != -1) {
            paging.sinceId = currentSinceId
        }

        log.info("[check]timeline current since_id:" + currentSinceId)

        // 最大(TWEET_MAX_COUNT × PAGING_MAX_COUNT)のツイートを取得し、チェックする
        for (int i = 1; i <= PAGING_MAX_COUNT; i++) {
            paging.page = i
            ResponseList<Status> statuses = twitter.getHomeTimeline(paging)     // getUserTimelineでユーザのみのが取れる

            if (statuses == null || statuses.size() == 0) {
                break
            }

            for (Status status : statuses) {
                registerImageUrl(status)
            }

            if (i == 1) {
                // since_idの保持
                if (reinsMstDao.findKey(ReinsConstants.TIMELINE_SINCEID)) {
                    reinsMstDao.updateValue(ReinsConstants.TIMELINE_SINCEID, statuses.get(0).getId().toString())
                } else {
                    reinsMstDao.insert(ReinsConstants.TIMELINE_SINCEID, statuses.get(0).getId().toString())
                }
            }
        }
    }

    /**
     * Twitterアカウントのリストから画像のURLを取得し、DBに保存する
     *
     * @throws TwitterException
     */
    protected void loopListImageGetTask() throws TwitterException {

        // 認証ユーザが持つリストを取得
        ResponseList<UserList> lists = null
        while (lists == null) {
            try {
                lists = twitter.getUserLists(userinfo.getScreenName())
            }
            catch (TwitterException te) {
                log.error(te.getMessage())
                // リスト取得で401が返ってきた場合は、再認証処理を行い、必要なTwitter情報を取り直す
                authenticationProcess()
            }
        }

        // 出力するリストを選定
        lists.retainAll(selectedUserList)
        // リストごとに情報を取得
        lists.each(analyzeAndSaveTweet)
    }

    /**
     * ホワイトリストがあれば、そのリスト情報だけ返し、
     * ブラックリストがあれば、そのリスト情報を除く
     * W:exist, B:non -> W only
     * W:exist, B:exist -> W - B
     * W:non, B:exist -> All - B
     * W:non, B:non -> All
     */
    Closure selectedUserList = { UserList list ->

        // TODO:\#->#のように\の置換処理をする
        //"\\#sample\\".replaceAll(/(\\)(.)/) { it[2] }
        List whiteList = allowList
        List blackList = denyList

        if (whiteList.size() == 0 && blackList.size() == 0) {
            log.debug("ホワイトリスト／ブラックリストの指定がない")
            return true
        }

        // ホワイトリストになければ全部NG
        boolean flag = false
        if (whiteList.size() != 0 && whiteList.contains(list.name)) {
            flag = true
        }
        if (blackList.size() != 0) {
            if (whiteList.size() == 0) {
                flag = true
            }
            if (blackList.contains(list.name)) {
                flag = false
            }
        }

        return flag
    }


    Closure analyzeAndSaveTweet = { UserList list ->

        // list_idでマスタを探し、存在しなければリスト用のテーブルを作成する。
        long listId = list.getId()
        String listname = list.getName()

        if (!listMstDao.find(listId)) {
            listMstDao.insert(listId, listname)
            listDataDao.create(listId)
        } else {
            // TODO:リスト名が変わっていないかをチェックし、変わっていたらマスタ値更新
        }

        // 現在チェックしているところまでのsince_idを設定
        long currentSinceId = listMstDao.getSinceId(listId) ?: -1

        // --------------- ツイート取得して解析 -----------------
        Paging paging = new Paging(1, TWEET_MAX_COUNT)
        if (currentSinceId != -1) {
            paging.sinceId = currentSinceId
        }

        log.info("[check]$listname current since_id:" + currentSinceId)

        // 最大(TWEET_MAX_COUNT × PAGING_MAX_COUNT)のツイートを取得し、チェックする
        for (int i = 1; i <= PAGING_MAX_COUNT; i++) {
            paging.page = i
            ResponseList<Status> statuses = twitter.getUserListStatuses(listId, paging)

            if (statuses == null || statuses.size() == 0) {
                break
            }

            for (Status status : statuses) {
                registerImageUrl(listId, status)
            }

            if (i == 1) {
                // since_idの保持
                listMstDao.updateSinceId(listId, statuses.get(0).getId())
            }
        }

        // リストごとにちょっと待つ
        sleep(WAIT_TIME * 10)
    }

    /**
     * Tweetに画像関係のURLが含まれていれば、
     * そのURLをtimelineテーブルに登録する。<br>
     *
     * @param status Tweet情報
     */
    protected void registerImageUrl(Status status) {

        // Tweetしたユーザー名
        String screenName = null
        // Retweetしたユーザ名
        String retweetUser = null

        if (status.getRetweetedStatus() != null && config.reins.retweet.target) {
            // RTの場合はRT元のユーザー名を格納する
            screenName = status.getRetweetedStatus().getUser().getScreenName()
            retweetUser = status.getUser().getScreenName()
        } else if (status.getRetweetedStatus() == null) {
            screenName = status.getUser().getScreenName()
        } else {
            return
        }

        // DB登録時の共通値設定
        Timeline timeline = new Timeline(
                statusId: status.getId(),
                tweetDate: status.getCreatedAt(),
                screenName: screenName,
                retweetUser: retweetUser,
                counterStatus: 0)

        // media_urlならTwitter公式、それ以外は別形式で保存
        status.getMediaEntities().each {
            timeline.imageUrl = it.getMediaURL()
            timeline.attribute = "twitter"
            timelineDao.insert(timeline)
        }
        // ----------■公式以外のリンクを取得する場合はここに書く--------------
        status.getURLEntities().each {
            // pixivリンクの保存
            if (it.getExpandedURL() =~ """www.pixiv.net/member_illust.php""") {
                timeline.attribute = "pixiv"
            }
            // gifの保存
            else if (it.getExpandedURL() =~ /.gif$/) {
                timeline.attribute = "gif"
            }
            timeline.imageUrl = it.getExpandedURL()
            timelineDao.insert(timeline)
        }
        // ---------------------------------------------------------------------
    }

    /**
     * Tweetに画像関係のURLが含まれていれば、
     * そのURLをlist_${listId}テーブルに登録する。<br>
     *
     * @param listId チェック対象のリストのID
     * @param status Tweet情報
     */
    protected void registerImageUrl(long listId, Status status) {

        // Tweetしたユーザー名
        String screenName = null
        // Retweetしたユーザ名
        String retweetUser = null

        if (status.getRetweetedStatus() != null && config.reins.retweet.target) {
            // RTの場合はRT元のユーザー名を格納する
            screenName = status.getRetweetedStatus().getUser().getScreenName()
            retweetUser = status.getUser().getScreenName()
        } else if (status.getRetweetedStatus() == null) {
            screenName = status.getUser().getScreenName()
        } else {
            return
        }

        // DB登録時の共通値設定
        ListData listData = new ListData(
                statusId: status.getId(),
                tweetDate: status.getCreatedAt(),
                screenName: screenName,
                retweetUser: retweetUser,
                counterStatus: 0)

        // media_urlならTwitter公式、それ以外は別形式で保存
        status.getMediaEntities().each {
            listData.imageUrl = it.getMediaURL()
            listData.attribute = "twitter"
            listDataDao.insert(listId, listData)
        }
        // ----------■公式以外のリンクを取得する場合はここに書く--------------
        status.getURLEntities().each {
            // pixivリンクの保存
            if (it.getExpandedURL() =~ """www.pixiv.net/member_illust.php""") {
                listData.attribute = "pixiv"
            }
            // gifの保存
            else if (it.getExpandedURL() =~ /.gif$/) {
                listData.attribute = "gif"
            }
            listData.imageUrl = it.getExpandedURL()
            listDataDao.insert(listId, listData)
        }
        // ---------------------------------------------------------------------
    }
}
