package white.box.reins.util

/**
 * 文字列操作系のユーティリティ
 * 
 * @author seri
 */
abstract class StringUtil {

	/**
	 * 文字列がnullか空文字の場合、trueを返す。
	 * 
	 * @param str チェックする文字列
	 * @return nullか空文字の場合、true。それ以外はfalse。
	 */
	static boolean isBlank(String str) {
		str == null || str.size() == 0
	}
}
