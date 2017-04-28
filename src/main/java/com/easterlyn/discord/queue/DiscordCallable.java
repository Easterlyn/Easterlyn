package com.easterlyn.discord.queue;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * A simple abstraction allowing for easier handling of Discord API calls.
 * 
 * @author Jikoo
 */
public abstract class DiscordCallable implements Comparable<DiscordCallable> {

	private final Long guildID;
	private final CallType callType;
	private final long queueTime;

	private CallPriority priority;
	private DiscordCallable chain;
	private int retries;

	public DiscordCallable(Long guildID, CallType callType) {
		this.guildID = guildID;
		this.callType = callType;
		this.queueTime = System.currentTimeMillis();
		this.priority = CallPriority.MEDIUM;
		this.retries = 0;
	}

	/**
	 * Set the CallPriority for this DiscordCallable.
	 * 
	 * @param priority the CallPriority
	 * @return this DiscordCallable for call chaining
	 */
	public DiscordCallable withPriority(CallPriority priority) {
		this.priority = priority;
		return this;
	}

	/**
	 * Set the number of retries for this DiscordCallable to attempt when failing.
	 * 
	 * @param retries the number of retries
	 * @return this DiscordCallable for call chaining
	 */
	public DiscordCallable withRetries(int retries) {
		this.retries = retries;
		return this;
	}

	/**
	 * Set the DiscordCallable to be queued when this DiscordCallable is completed.
	 * 
	 * @param call the DiscordCallable
	 * @return the given DiscordCallable for easier extended chain setup
	 */
	public DiscordCallable withChainedCall(DiscordCallable call) {
		this.chain = call;
		return call;
	}

	public Long getGuildID() {
		return this.guildID;
	}

	public CallType getCallType() {
		return this.callType;
	}

	public DiscordCallable getChainedCall() {
		return this.chain;
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
