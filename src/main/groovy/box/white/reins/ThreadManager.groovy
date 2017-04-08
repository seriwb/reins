package box.white.reins

import groovy.util.logging.Slf4j

@Slf4j
final class ThreadManager {
	static def execute(Thread thread) {	// TODO:change to ManagedThread
		try {
			thread.start()
		} catch (e) {
			
			log.error("${thread.getClass().getName()} thread error!", e)
		}
	}
}