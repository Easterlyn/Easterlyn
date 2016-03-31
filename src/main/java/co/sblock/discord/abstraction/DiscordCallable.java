package co.sblock.discord.abstraction;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

/**
 * A simple abstraction allowing for easier handling of Discord API calls.
 * 
 * @author Jikoo
 */
public abstract class DiscordCallable implements Comparable<DiscordCallable> {

	private final CallPriority priority;
	private final boolean retryOnException;

	public DiscordCallable() {
		this(CallPriority.MEDIUM);
	}

	public DiscordCallable(CallPriority priority) {
		this(priority, false);
	}

	public DiscordCallable(CallPriority priority, boolean retryOnException) {
		this.priority = priority;
		this.retryOnException = retryOnException;
	}

	public abstract void call() throws DiscordException, HTTP429Exception, MissingPermissionsException;

	public final boolean retryOnException() {
		return retryOnException;
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
