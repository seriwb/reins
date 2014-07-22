package util

import spock.lang.Specification
import util.UrlUtility

class UrlUtilitySpec extends Specification {

	def 短縮URLを通常のURLに変換する() {
		setup:
		expect: UrlUtility.expandUrl(shortUrl) == normalUrl
		where:
		shortUrl | normalUrl
		new URL('http://t.co/fjbygXyxhj') | new URL('http://seri.hatenablog.com/')
//		new URL('http://t.co/fjbygXyxhj') | new URL('http://seri.com/')

	}
}
