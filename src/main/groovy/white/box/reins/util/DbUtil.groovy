package white.box.reins.util

import groovy.sql.Sql

import white.box.reins.ReinsConstants

/**
 * DBアクセスの共通化クラス
 *
 * @author seri
 */
public abstract class DbUtil {

	def db = Sql.newInstance(ReinsConstants.JDBC_MAP)

}
