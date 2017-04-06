package box.white.reins.util

import groovy.sql.Sql

import box.white.reins.ReinsConstants

/**
 * DBアクセスの共通化クラス
 *
 * @author seri
 */
abstract class DbUtil {

	def db = Sql.newInstance(ReinsConstants.JDBC_MAP)

}
