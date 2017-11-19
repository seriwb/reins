package box.white.rains.util;

import spock.lang.Specification
import box.white.reins.util.WebUtil

class WebUtilSpec extends Specification {

    def "Twitter公式画像が取得できることを確認"() {

        setup:
        String url = "https://pbs.twimg.com/media/C9CTyOWU0AAZVR2.jpg"
        def dir =  new File("test-classes")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        when:
        WebUtil.download(url, new File("test-classes/sample.jpg"))

        then:
        (new File("test-classes/sample.jpg")).exists() == true
    }
}
