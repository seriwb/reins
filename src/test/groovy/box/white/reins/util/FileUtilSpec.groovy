package box.white.reins.util

import spock.lang.Specification

class FileUtilSpec extends Specification {

	def "コピーテスト"() {
		setup:
		List<String> hoge1 = ["hoge", "huga"]
		List<String> hoge2 = hoge1
		hoge1.remove(0)
		println hoge2		
	}
	
	def "Class test"() {
		setup:
		int val = 2
		List<String> hoge1 = ["hoge", "huga"]
		AAAA a = new AAAA(val, hoge1)
		println a.aaaa
		println a.list
		val = 3
		hoge1.remove(0)
		println a.aaaa
		println a.list
		
		println "\\#sample\\".replaceAll(/(\\)(.)/) { it[2] }
		println '#s\\amp\\le\\hoge'.replaceAll(/(\\)(.)/) { it[2] }
		println "\\"
	}
}

class AAAA {
	int aaaa = 1
	List<String> list = null
	public AAAA(int val, List<String> list) {
		this.aaaa = val
		this.list = list.collect()
	}
}