package com.easterlyn.discord.queue;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * A simple abstraction allowing for easier handling of Discord API calls.
 * 
 * @author Jikoo
 */
public abstract class DiscordCallable {

	private final Long guildID;
	private final CallType callType;
	private final long queueTime;

	private DiscordCallable chain, parent;
	private int retries;

	protected DiscordCallable(IChannel channel, CallType callType) {
		this(channel.isPrivate() ? channel.getLongID() : channel.getGuild().getLongID(), callType);
	}

	protected DiscordCallable(Long guildID, CallType callType) {
		this.guildID = guildID;
		this.callType = callType;
		this.queueTime = System.currentTimeMillis();
		this.retries = 0;
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
		if (call != null) {
			call.parent = this;
			return call;
		}
		return this;
	}

	public DiscordCallable getChainStart() {
		if (this.parent != null) {
			return this.parent.getChainStart();
		}
		return this;
	}

	Long getGuildID() {
		return this.guildID;
	}

	CallType getCallType() {
		return this.callType;
	}

	DiscordCallable getChainedCall() {
		return this.chain;
	}

	public abstract void call() throws DiscordException, RateLimitException, MissingPermissionsException;

	final boolean retryOnException() {
		return --retries >= 0;
	}

	public long getQueueTime() {
		return queueTime;
	}
}
