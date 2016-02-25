package co.sblock.discord;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import co.sblock.discord.abstraction.CallType;
import co.sblock.discord.abstraction.DiscordCallable;

import sx.blah.discord.api.DiscordException;
import sx.blah.discord.util.HTTP429Exception;

/**
 * A Thread for periodically executing DiscordCallables from the given Queue.
 * 
 * @author Jikoo
 */
public class QueueDrainThread extends Thread {

	private final Map<CallType, Pair<AtomicInteger, AtomicLong>> rateLimitInfo;
	private final Discord discord;
	private final Queue<DiscordCallable> queue;
	private final long delay;

	public QueueDrainThread(Discord discord, Queue<DiscordCallable> queue, long delay, String threadName) {
		super(threadName);
		this.discord = discord;
		this.queue = queue;
		this.delay = delay;
		this.rateLimitInfo = new HashMap<>();
	}

	@Override
	public void run() {

		while (discord.isEnabled()) {

			if (queue.isEmpty()) {
				// Sleep for duration specified
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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
				Pair<AtomicInteger, AtomicLong> pair;
				if (rateLimitInfo.containsKey(callable.getCallType())) {
					pair = rateLimitInfo.get(callable.getCallType());
					int timesLimited = pair.getLeft().incrementAndGet();
					long millisecondsSlept = pair.getRight().addAndGet(e.getRetryDelay());
					if (timesLimited % 50 == 0) {
						System.out.println(String.format("%s: average delay %s with %s failures totalling %sms.",
								callable.getCallType().name(), millisecondsSlept / timesLimited, timesLimited, millisecondsSlept));
					}
				} else {
					pair = new ImmutablePair<>(new AtomicInteger(1), new AtomicLong(e.getRetryDelay()));
					rateLimitInfo.put(callable.getCallType(), pair);
				}
				try {
					// Pause, we're rate limited.
					Thread.sleep(e.getRetryDelay());
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
				continue;
			} catch (Exception e) {
				// Likely permissions, but can be malformed JSON when odd responses are received
				e.printStackTrace();
			}
			queue.remove();
		}
	}

}
