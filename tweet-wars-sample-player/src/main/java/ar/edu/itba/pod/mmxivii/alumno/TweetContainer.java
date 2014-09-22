package ar.edu.itba.pod.mmxivii.alumno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.edu.itba.pod.mmxivii.tweetwars.Status;

public class TweetContainer {

	private static TweetContainer instance = null;
	private Map<String, List<Status>> fakePlayerTweets;

	public static TweetContainer getInstance() {
		if (instance == null)
			instance = new TweetContainer();
		return instance;
	}

	public List<Status> fakeTweetsForPlayer(String playerId) {
		return fakePlayerTweets.get(playerId);
	}

	TweetContainer() {
		fakePlayerTweets = new HashMap<String, List<Status>>();
	}

	public void addFakeTweet(Status tweet) {
		synchronized (this.fakePlayerTweets) {
			List<Status> tweets;
			tweets = new ArrayList<Status>();
			String source = tweet.getSource();
			if (fakePlayerTweets.get(source) == null) {
				fakePlayerTweets.put(source, tweets);
			} else {
				tweets = fakePlayerTweets.get(source);
			}
			tweets.add(tweet);
		}
	}

	public void removeFakeTweetsForPlayer(String source) {
		List<Status> ss = fakePlayerTweets.get(source);
		ss.retainAll(new ArrayList<Status>());
		return;
	}
}
