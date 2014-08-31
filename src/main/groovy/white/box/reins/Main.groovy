package white.box.reins

import groovy.util.logging.Slf4j
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import white.box.reins.component.OAuthComponent

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

		// このプログラムで利用するTwitterオブジェクトを作成
		ConfigurationBuilder cb = new ConfigurationBuilder()
		String consumerKey = config.get("oauth.consumerKey")
		String consumerSecret = config.get("oauth.consumerSecret")
		cb.setDebugEnabled(true)
			.setOAuthConsumerKey(consumerKey)
			.setOAuthConsumerSecret(consumerSecret)
		TwitterFactory factory = new TwitterFactory(cb.build())
		Twitter twitter = factory.getInstance()


		def oauth = new OAuthComponent()
		if (!oauth.isAuthorized(twitter)) {

			twitter = factory.getInstance()
			oauth.authorize(twitter)
		}

		TwitterWatcher tw = new TwitterWatcher(config, twitter)
		ImageGetter ig = new ImageGetter(config)

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run () {
				tw.stopRunning()
				ig.stopRunning()

				println "exit."
			}
		});

		try {
			tw.start()
			ig.start()
		} catch (e) {
			log.error("Thread Error!", e)
		}
	}
}