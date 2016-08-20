package co.sblock.discord.abstraction;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * A simple abstraction allowing for easier handling of Discord API calls.
 * 
 * @author Jikoo
 */
public abstract class DiscordCallable implements Comparable<DiscordCallable> {

	private final CallPriority priority;
	private final long queueTime;
	private int retries;

	public DiscordCallable() {
		this(CallPriority.MEDIUM);
	}

	public DiscordCallable(CallPriority priority) {
		this(priority, 0);
	}

	public DiscordCallable(CallPriority priority, int retries) {
		this.priority = priority;
		this.queueTime = System.currentTimeMillis();
		this.retries = retries;
	}

	public abstract void call() throws DiscordException, RateLimitException, MissingPermissionsException;

	public final boolean retryOnException() {
		return --retries >= 0;
	}

	@Override
	public int compareTo(DiscordCallable o) {
		// Lower priority, return 1.
		if (this.priority.ordinal() < o.priority.ordinal()) {
			return 1;
		}
		// Same priority, return based on queue time.
		if (this.priority.ordinal() == o.priority.ordinal()) {
			// Later queue time, return 1.
			if (this.queueTime > o.queueTime) {
				return 1;
			}
			// Same queue time, same priority.
			if (this.queueTime == o.queueTime) {
				return 0;
			}
			// Earlier queue time.
			return -1;
		}
		// Higher queue priority.
		return -1;
	}

}
