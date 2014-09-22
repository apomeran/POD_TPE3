package ar.edu.itba.pod.mmxivii.alumno;

import org.jgroups.JChannel;
import org.jgroups.Message;

import ar.edu.itba.pod.mmxivii.tweetwars.GamePlayer;
import ar.edu.itba.pod.mmxivii.tweetwars.Status;
import ar.edu.itba.pod.mmxivii.tweetwars.TweetsProvider;
import ar.edu.itba.pod.mmxivii.tweetwars.impl.TweetsProviderImpl;

public class TweetRepeat extends Thread {
	private TweetsProvider tweetProvider;
	private GamePlayer player;
	private String hash;
	private static final int TWEET_COUNT = TweetsProviderImpl.MAX_BATCH_SIZE;
	private JChannel channel;

	public TweetRepeat(GamePlayer player, String player_hash,
			TweetsProvider tweetProvider, JChannel channel) {
		this.player = player;
		this.hash = player_hash;
		this.tweetProvider = tweetProvider;
		this.channel = channel;

	}

	public void run() {
		while (true) {
			try {
				Status[] tweets = tweetProvider.getNewTweets(player, hash,
						TWEET_COUNT);
				for (Status tweet : tweets) {
					channel.send(new Message(null, null, tweet));
				}
				System.out.println("Sent " + tweets.length
						+ " new Tweets to the group (BROADCAST)");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Error en TweetFetcher");
				e.printStackTrace();
			}
		}
	}

}
