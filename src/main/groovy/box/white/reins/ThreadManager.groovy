package box.white.reins

import groovy.transform.Synchronized
import groovy.util.logging.Slf4j
import java.util.concurrent.ConcurrentHashMap

@Slf4j
final class ThreadManager {
	//private static Set<ManagedThread> threadSet = Collections.newSetFromMap(new ConcurrentHashMap<ManagedThread, Boolean>());
	private static Map<String, ManagedThread> threadMap = new ConcurrentHashMap<>()

	private static volatile boolean loop = true

	@Synchronized('threadMap')
	static void execute(ManagedThread thread) {
		String threadName = thread.getClass().getSimpleName()
		if (threadMap.containsKey(threadName)) {
			return
		}
		threadMap.put(threadName, thread)

		try {
			thread.start()
		} catch (e) {
			log.error("$threadName thread error!", e)
		}
	}

	/**
	 * 登録されているThreadでエラー落ちしているものがあれば復帰させる。
	 * 本メソッドはThreadの登録処理が終わってから呼び出すこと。
	 * ・ManagedThreadのListに登録されているThreadをチェックし、生きていれば何もしない。
	 * ・死んでいれば、当該Thread名をメッセージに含めてエラーを投げる
	 */
	static void recover() throws Exception {
		while (loop) {
			threadMap.each {
				if (it.value == null || !it.value.isAlive()) {
					log.error("This thread is dead!")
					threadMap.remove(it.key)

					throw new Exception(it.key)
				}
			}
		}
	}

	static void stopRecovering() {
		loop = false
	}
}
