package white.box.reins.util

import java.awt.Desktop
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

//import groovyx.net.http.HttpURLClient

/**
 * WEB操作系のユーティリティ
 *
 * @author seri
 *
 */
abstract class WebUtil {

	/**
	 * 指定のTweetが表示されるTwitterのURLを返す
	 *
	 * @param screenName Twitterユーザ名
	 * @param statusId TweetID
	 * @return TweetのURL
	 */
	static String getTwitterUrl(String screenName, Long statusId) {
		"https://twitter.com/${screenName}/status/${statusId}"
	}

	/**
	 * 指定したURLのページをデフォルトのブラウザで表示する
	 *
	 * @param url 表示するURL
	 * @return 表示できたtrue
	 */
	static void viewUrlPage(String url) {
		Desktop desktop = Desktop.getDesktop()
		desktop.browse(new URI(url))
	}

	//	public static String getURL(String text) {
	//		def urlList = text.findAll {
	//			it.startsWith('http') && it.endsWith('dles')
	//		}
	//	}


	//	public String expandQueryURL(String urlStr, String queryStr) {
	//		HttpURLClient http = new HttpURLClient(followRedirects:false)
	//
	//		def params = [ url:urlStr,
	//				query:[e:queryStr],
	//				headers:['User-Agent':'Mozilla/5.0'] ]
	//		def resp = http.request( params )
	//
	//		return resp.headers.'Location'
	//	  }


	/**
	 * WEBから画像をダウンロードする。
	 *
	 * @param url 画像URL
	 * @param filepath 画像保存先ファイル
	 */
	static void download(String url, File filepath) {
		String imageUrl = url
		if (url =~ """pbs.twimg.com""") {
			imageUrl = url.concat(":large")
		}
		URL website = new URL(imageUrl)
		ReadableByteChannel rbc = Channels.newChannel(website.openStream())
		FileOutputStream fos = new FileOutputStream(filepath)
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE)
		fos.close()
	}
}
