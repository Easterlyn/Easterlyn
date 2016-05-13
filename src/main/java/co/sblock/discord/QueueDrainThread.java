package co.sblock.discord;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import co.sblock.discord.abstraction.DiscordCallable;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;

/**
 * A Thread for periodically executing DiscordCallables from the given Queue.
 * 
 * @author Jikoo
 */
public class QueueDrainThread extends Thread {

	private final Discord discord;
	private final Queue<DiscordCallable> queue;
	private final long delay;

	public QueueDrainThread(Discord discord, long delay, String threadName) {
		super(threadName);
		this.discord = discord;
		this.queue = new PriorityBlockingQueue<>();
		this.delay = delay;
	}

	@Override
	public void run() {

		while (discord.isEnabled()) {

			if (!discord.isReady()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
				continue;
			}

			if (queue.isEmpty()) {
				// Sleep for duration specified
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					break;
				}
				continue;
			}

			if (!discord.getClient().isReady()) {
				discord.getLogger().warning("Discord client is not ready, but the module claims to be.");
				break;
			}

			DiscordCallable callable = queue.remove();
			try {
				callable.call();
			} catch (DiscordException e) {
				try {
					// If we encounter an exception, pause for extra security.
					Thread.sleep(500L);
				} catch (InterruptedException ie) {
					break;
				}
				if (callable.retryOnException()) {
					/*
					 * Rather than skip removal in this case to preserve order, we re-add the
					 * DiscordCallable. This does ruin order in the case of messages sent, however,
					 * sent messages do not currently retry. It also allows us to modify the queue from inside
					 * the callables safely.
					 */
					queue.add(callable);
					// Don't log when retrying, we only retry because of a Discord4J fault generally.
					continue;
				}
				e.printStackTrace();
			} catch (HTTP429Exception e) {
				try {
					// Pause, we're rate limited.
					Thread.sleep(e.getRetryDelay() + 100L);
				} catch (InterruptedException ie) {
					break;
				}
				continue;
			} catch (Exception e) {
				// Likely permissions, but can be malformed JSON when odd responses are received
				e.printStackTrace();
			}
		}
	}

	public void queue(DiscordCallable callable) {
		if (this.isAlive()) {
			this.queue.add(callable);
		}
	}

}
