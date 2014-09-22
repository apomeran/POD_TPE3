package ar.edu.itba.pod.mmxivii.alumno;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import ar.edu.itba.pod.mmxivii.tweetwars.GameMaster;
import ar.edu.itba.pod.mmxivii.tweetwars.GamePlayer;
import ar.edu.itba.pod.mmxivii.tweetwars.Status;
import ar.edu.itba.pod.mmxivii.tweetwars.TweetsProvider;

public class MyApp extends ReceiverAdapter {

	public static final String TWEETS_PROVIDER_NAME = "tweetsProvider";
	public static final String GAME_MASTER_NAME = "gameMaster";
	JChannel channel;
	String user_name = System.getProperty("user.name", "alan2");
	private TweetContainer repo = TweetContainer.getInstance();
	private GamePlayer gp;
	private TweetsProvider tweetsProvider;
	private GameMaster gameMaster;

	private MyApp() {
	}

	private void start() throws Exception {
		channel = new JChannel(); // use the default config, udp.xml
		channel.setReceiver(this);
		channel.connect("ChatCluster");
		eventLoop();

	}

	private void eventLoop() {
		try {
			this.gp = new GamePlayer("USUARIO__17", "Alan P.");
			final Registry registry = LocateRegistry.getRegistry(7242);
			this.tweetsProvider = (TweetsProvider) registry
					.lookup(TWEETS_PROVIDER_NAME);
			this.gameMaster = (GameMaster) registry.lookup(GAME_MASTER_NAME);
			final String hash = "abceddd";
			try {
				gameMaster.newPlayer(gp, hash);
			} catch (IllegalArgumentException e) {
				System.out.println("Already in group");
				// e.printStackTrace();
			}

			new FakeTweetGen(channel, gp.getId(), hash).start();
			new TweetRepeat(gp, hash, tweetsProvider, channel).start();
			new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						int score = -1;
						try {
							score = gameMaster.getScore(gp);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("The score for: " + gp.getId()
								+ " is " + score);
					}
				}
			}).start();

			// while (true) {
			// Message msg;
			// final Status[] tweets = tweetsProvider.getNewTweets(gp, hash,
			// 3);
			// for (Status tweet : tweets) {
			// msg = new Message(null, null, tweet);
			// channel.send(msg);
			// }
			//
			// }
		} catch (RemoteException | NotBoundException e) {
			System.out.println("RemoteException | NotBoundException Error");
			e.printStackTrace();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Exception General Error");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		new MyApp().start();
	}

	public void viewAccepted(View new_view) {
		System.out.println("** view: " + new_view);
	}

	public void receive(Message msg) {
		String OTHER = ((Status) msg.getObject()).getSource();
		String MYSELF = this.gp.getId();
		if (!(OTHER.equals(MYSELF))) {
			System.out.println("Received tweet from "
					+ ((Status) msg.getObject()).getSource());
			try {
				this.checkTweet((Status) msg.getObject(), repo, gp, gameMaster);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void checkTweet(Status tweet, TweetContainer repo, GamePlayer p,
			GameMaster master) throws RemoteException {
		if (tweet == null)
			return;
		Status actualTweet = tweetsProvider.getTweet(tweet.getId());
		if (actualTweet == null)
			reportFakeArray(repo, tweet, p, master);
		if (compareTweet(actualTweet, tweet)) {
			reportFakeArray(repo, tweet, p, master);
		} else {
			master.tweetReceived(p, tweet);
			System.out.println("ACK Tweet to Master");

		}
	}

	private boolean compareTweet(Status actualTweet, Status originalTweet) {
		if (actualTweet == null)
			return true;
		if (actualTweet.getCheck() == null)
			return true;
		if (actualTweet.getText() == null)
			return true;
		if (actualTweet.getSource() == null)
			return true;

		if (!actualTweet.getCheck().equals(originalTweet.getCheck())
				|| !actualTweet.getText().equals(originalTweet.getText())
				|| !actualTweet.getSource().equals(originalTweet.getSource()))
			return true;
		return false;
	}

	private void reportFakeArray(TweetContainer repo, Status tweet,
			GamePlayer p, GameMaster master) throws RemoteException {
		if (!tweet.getSource().equals(p.getId())) {
			repo.addFakeTweet(tweet);
			List<Status> fakeTweetsList = repo.fakeTweetsForPlayer(tweet
					.getSource());
			if (fakeTweetsList.size() >= GameMaster.MIN_FAKE_TWEETS_BATCH) {

				Status[] s = new Status[fakeTweetsList.toArray().length];
				int i = 0;
				for (Object status : fakeTweetsList.toArray()) {
					s[i++] = (Status) status;

				}

				master.reportFake(p, s);
				repo.removeFakeTweetsForPlayer(tweet.getSource());
				System.out.println("Reported FAKE TWEETS BATCH");

			}
		}
	}

	public void catedraExample() {
		final GamePlayer gp = new GamePlayer("ap", "Alan P.");
		final GamePlayer gp2 = new GamePlayer("yo2", "aquel otro");
		System.out.println("empezando!");
		try {
			final Registry registry = LocateRegistry.getRegistry(7242);
			final TweetsProvider tweetsProvider = (TweetsProvider) registry
					.lookup(TWEETS_PROVIDER_NAME);
			final GameMaster gameMaster = (GameMaster) registry
					.lookup(GAME_MASTER_NAME);

			final String hash = "abceddd";
			try {
				gameMaster.newPlayer(gp, hash);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			try {
				gameMaster.newPlayer(gp2, hash);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}

			final Status[] tweets = tweetsProvider.getNewTweets(gp, hash, 10);
			for (Status tweet : tweets) {
				System.out.println("tweet = " + tweet);
				gameMaster.tweetReceived(gp2, tweet);
			}

			for (int i = 0; i < 10; i++) {
				System.out.println("new tweets " + i);
				final Status[] newTweets = tweetsProvider.getNewTweets(gp,
						hash, 100);
				gameMaster.tweetsReceived(gp2, newTweets);
			}

		} catch (RemoteException | NotBoundException e) {
			System.err.println("App Error: " + e.getMessage());
			System.exit(-1);
		}
		System.out.println("Hola alumno!");
	}

}
