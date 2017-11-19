package box.white.reins

import box.white.reins.util.DateUtil
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class ImageGetterSpec extends Specification {

    def 起動テスト() {

        setup:
//		ImageGetter imageGetter = new ImageGetter()
//		imageGetter.start()

//		imageGetter.mkdir("h2", "hoge2")

        String[] strings = "http://pbs.twimg.com/media/BrJBmjsCEAAWwo-.jpg".split('\\.')
        println strings[strings.length - 1]
    }

    def 画像ファイルの作成テスト() {
        setup:
        def config = new ConfigSlurper().parse(
                new File('./conf/config.txt').toURI().toURL())

        def imageGetter = new ImageGetter(config)

        Map imageInfo = [
                id           : 1,
                imageUrl     : "http://hoge/sample.jpg",
                screenName   : "username",
                counterStatus: 1,
                statusId     : 1,
                tweetDate    : DateUtil.convertDate("2016-10-22 12:30:12", "yyyy-MM-dd HH:mm:ss")
        ]
        Map imageInfo2 = [
                id           : 1,
                imageUrl     : "http://hoge/sample.jpg",
                screenName   : "username",
                counterStatus: 1,
                statusId     : 1,
                tweetDate    : DateUtil.convertDate("2016-10-23 12:30:12", "yyyy-MM-dd HH:mm:ss")
        ]
        def dirpath = new File("./")
        File createFileName = imageGetter.createFileName(dirpath, imageInfo)

        File createFileName2 = imageGetter.createFileName(dirpath, imageInfo2)
        createFileName2.createNewFile()
        File createFileName3 = imageGetter.createFileName(dirpath, imageInfo2)

        expect:
        createFileName.getPath() == './20161022-123012_username.jpg'
        createFileName3.getPath() == './20161023-123012-2_username.jpg'

        cleanup:
        Files.deleteIfExists(Paths.get(createFileName.getPath()))
        Files.deleteIfExists(Paths.get(createFileName2.getPath()))
    }
}