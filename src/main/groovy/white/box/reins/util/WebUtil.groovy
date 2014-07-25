package white.box.reins.util

//import groovyx.net.http.HttpURLClient

public abstract class WebUtil {

	public static String getURL(String text) {

		def urlList = text.findAll {
			it.startsWith('http') && it.endsWith('dles')
		}
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


}
