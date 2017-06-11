package box.white.reins.util

import twitter4j.Query
import twitter4j.Twitter

import java.awt.*
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

/**
 * Twitterのデータ操作ユーティリティ
 * TODO:TwitterのPKかURLからデータを取得できるようにする
 *
 * @author seri
 */
class TwitterUtil {

    private TwitterUtil() {}

    // imageInfo:[id:2, url:http://pbs.twimg.com/media/Cv5wq5YUIAApA8R.jpg, screenName:tsukachuma, counterStatus:0, statusId:792272973675716608, tweetDate:2016-10-29 16:52:51.0]
    static Twitter getData(Twitter twitter, long statusId) {
        def query = Query.sinceId(statusId)
    }
}
