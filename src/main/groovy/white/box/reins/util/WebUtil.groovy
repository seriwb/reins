package white.box.reins.util

import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel

//import groovyx.net.http.HttpURLClient

/**
 * WEB操作系のユーティリティ
 *
 * @author seri
 *
 */
public abstract class WebUtil {

	public static String getURL(String text) {

		def urlList = text.findAll {
			it.startsWith('http') && it.endsWith('dles')
		}
	}

	public static String getTwitterUrl(String screenName, String statusId) {
		"https://twitter.com/${screenName}/status/${statusId}"
	}



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
	public static void download(String url, File filepath) {
		URL website = new URL(url);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream())
		FileOutputStream fos = new FileOutputStream(filepath)
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE)
		fos.close()
	}
}
