package white.box.reins.util

public abstract class Utility {

	public static String getURL(String text) {
		
		def urlList = text.findAll {
			it.startsWith('http') && it.endsWith('dles')
		}
	}

}
