package box.white.reins

abstract class ManagedThread extends Thread {

	/** ループ処理の継続判定用 */
	private volatile boolean loop = true
	
	@Override
	void run() {
		try {
			preProcess()
			while (loop) {
				mainProcess()
			}
		} finally {
			postProcess()
		}
	}
	
	/**
	 * スレッド停止用メソッド<br>
	 * スレッド作成元のスレッドで呼ぶように作ること。
	 */
	void stopRunning() {
		loop = false
	}

	abstract void preProcess()
	abstract void mainProcess()
	abstract void postProcess()
}
