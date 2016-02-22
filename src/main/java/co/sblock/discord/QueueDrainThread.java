package co.sblock.discord;

import java.util.Queue;

import sx.blah.discord.api.DiscordException;
import sx.blah.discord.util.HTTP429Exception;

/**
 * 
 * 
 * @author Jikoo
 */
public class QueueDrainThread extends Thread {

	private final Discord discord;
	private final Queue<DiscordCallable> queue;
	private final long delay;

	public QueueDrainThread(Discord discord, Queue<DiscordCallable> queue, long delay, String threadName) {
		super(threadName);
		this.discord = discord;
		this.queue = queue;
		this.delay = delay;
	}

	@Override
	public void run() {

		while (discord.isEnabled()) {
			// Sleep for duration specified
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (queue.isEmpty()) {
				continue;
			}

			DiscordCallable callable = queue.element();
			try {
				callable.call();
			} catch (DiscordException e) {
				if (callable.retryOnException()) {
					// Don't log when retrying, we only retry because of a Discord4J fault generally.
					continue;
				}
				e.printStackTrace();
			} catch (HTTP429Exception e) {
				try {
					// Pause, we're rate limited.
					Thread.sleep(e.getRetryDelay());
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			} catch (Exception e) {
				// Likely permissions, but can be malformed JSON when odd responses are received
				e.printStackTrace();
			}
			queue.remove();
		}
	}

}
