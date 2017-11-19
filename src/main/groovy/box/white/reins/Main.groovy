package box.white.reins

import groovy.util.logging.Slf4j

/**
 * 実行クラス
 *
 * @author seri
 */
@Slf4j
class Main {

    static void main(String[] args) {

        def bs = new BootStrap()
        def config = bs.init()

//		bs.destroy()

        def tw = new TwitterWatcher(config)
        def ig = new ImageGetter(config)
        def cl = new CommandListener(config)

        Runtime.getRuntime().addShutdownHook(new Thread() {
            void run() {
                ThreadManager.stopRecovering()

                tw.stopRunning()
                ig.stopRunning()
                cl.stopRunning()

                println "exit."
            }
        })

        // 常駐プログラムの実行
        ThreadManager.execute(tw)
        ThreadManager.execute(ig)
        ThreadManager.execute(cl)

        // recoverから落ちたと判定されて帰ってきたインスタンスを再作成するのがいいのではないだろうか
        while (true) {
            try {
                ThreadManager.recover()
            } catch (e) {
                switch (e.getMessage()) {
                    case "TwitterWatcher":
                        // TODO:15分くらいのSleepかけたほうがいいかもしてない
                        tw = new TwitterWatcher(config)
                        ThreadManager.execute(tw)
                        break
                    case "ImageGetter":
                        ig = new ImageGetter(config)
                        ThreadManager.execute(ig)
                        break
                    case "CommandListener":
                        cl = new CommandListener(config)
                        ThreadManager.execute(cl)
                        break
                    default:
                        log.error("${e.getMessage()} is not found!")
                        System.exit(-1)
                }
            }
        }
    }
}
