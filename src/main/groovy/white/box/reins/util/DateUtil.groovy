package white.box.reins.util

import java.text.SimpleDateFormat


/**
 * 日付処理のユーティリティ
 *
 * @author seri
 */
abstract class DateUtil {

	/**
	 * 指定の日付のDateインスタンスを返す
	 *
	 * @param dateStr
	 * @param format
	 * @return Dateインスタンス
	 */
	static Date convertDate(String dateStr, String format) {
		def dateFormat = new SimpleDateFormat(format)
		dateFormat.parse(dateStr)
	}
}
