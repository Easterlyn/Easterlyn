package com.easterlyn.discord.queue;

/**
 *
 *
 * @author Jikoo
 */
public enum CallType {

	MESSAGE_SEND(350),
	MESSAGE_EDIT(600),
	MESSAGE_DELETE(350),
	MESSAGE_POPULATE(5000),
	EMOJI_EDIT(600),
	BULK_DELETE(2000),
	GUILD_USER_NICKNAME(2000),
	GUILD_USER_KICK(2000),
	GUILD_USER_ROLE(1000),
	GUILD_MODIFY(5000),
	CHANNEL_CREATE(5000),
	CHANNEL_DELETE(5000),
	CHANNEL_MODIFY(5000);

	private final long rateLimit;

	CallType(long rateLimit) {
		this.rateLimit = rateLimit;
	}

	public long getRateLimit() {
		return this.rateLimit;
	}

}
