package white.box.reins

import groovy.util.logging.Slf4j

/**
 * reinsコマンドを処理するクラス。<br>
 * 
 * ・利用可能なコマンドのリストを取得する。<br>
 * ・リストにあるコマンドのクラスが存在しなければエラーとする。<br>
 * ・コマンドクラスで許可していないパラメータ数が指定された場合、エラーメッセージを出力する。<br>
 * ・基底クラスである程度処理をまとめる<br>
 * 
 * @author seri
 */
@Slf4j
class CommandListener extends Thread {

	/** システム設定値 */
	def config = null
	
	/** コマンドクラス */
	def commandMap = [:]

	/** ループ処理の継続判定用 */
	private boolean loop = true
	
	/**
	 * コンストラクタ<br>
	 * ・各コマンドクラスは初期化時にインスタンス化し、それを利用する。<br>
	 * ・各コマンドクラスにはConfigを渡す。<br>
	 * @param config システム設定値
	 */
	CommandListener(def config) {

		this.config = config
		// TODO:コマンドクラスのインスタンスを生成してMapに格納する
	}
	
	/**
	 * スレッド停止用メソッド<br>
	 * スレッド作成元のスレッドで呼ぶように作ること。
	 */
	public void stopRunning(){
		loop = false;
	}

	@Override
	public void run() {
		
		while(loop) {
			
		}
	} 
}
