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
	private int retries;

	public DiscordCallable() {
		this(CallPriority.MEDIUM);
	}

	public DiscordCallable(CallPriority priority) {
		this(priority, 0);
	}

	public DiscordCallable(CallPriority priority, int retries) {
		this.priority = priority;
		this.retries = retries;
	}

	public abstract void call() throws DiscordException, RateLimitException, MissingPermissionsException;

	public final boolean retryOnException() {
		return --retries >= 0;
	}

	@Override
	public int compareTo(DiscordCallable o) {
		if (priority.ordinal() < o.priority.ordinal()) {
			return 1;
		}
		if (priority.ordinal() == o.priority.ordinal()) {
			return 0;
		}
		return -1;
	}

}
