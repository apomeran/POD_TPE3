package ar.edu.itba.pod.mmxivii.alumno;

import java.util.Random;

import org.jgroups.JChannel;
import org.jgroups.Message;

import ar.edu.itba.pod.mmxivii.tweetwars.Status;

public class FakeTweetGen extends Thread {

	private JChannel channel;
	private String gamePlayerID;
	private String hash;

	public static final int MIN_DELAY = 200;
	public static final int MAX_DELTA = 800;
	public static final float PROBABILITY = (float) 0.001;
	private final boolean slow = true;

	public FakeTweetGen(JChannel channel, String gamePlayerID, String hash) {
		this.channel = channel;
		this.gamePlayerID = gamePlayerID;
		this.hash = hash;
	}

	boolean flipACoin() {

		return (Math.random() < PROBABILITY);

	}

	public void run() {
		while (true) {
			if (flipACoin()) {
				Status fakeTweet = new Status((new Random()).nextInt(),
						FakeTweetGen.GetRandomString(140), gamePlayerID, hash);
				try {
					channel.send(new Message(null, null, fakeTweet));
					System.out
							.println("Sent Fake Tweet to everyone (Broadcast)");
				} catch (Exception e) {
					System.out.println("Error en FakeTweet");
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			delay();
		}
	}

	void delay() {
		if (slow)
			try {
				Thread.sleep(new Random().nextInt(MAX_DELTA) + MIN_DELAY);
			} catch (InterruptedException ignore) {
			}
	}

	public static String GetRandomString(int maxlength) {
		String result = "";
		int i = 0, n = 0, min = 33, max = 122;
		while (i < maxlength) {
			n = (int) (Math.random() * (max - min) + min);
			if (n >= 33 && n < 123) {
				result += (char) n;
				++i;
			}
		}
		return (result);
	}

}
