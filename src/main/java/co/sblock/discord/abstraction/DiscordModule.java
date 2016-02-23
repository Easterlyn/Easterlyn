package co.sblock.discord.abstraction;

import co.sblock.discord.Discord;

/**
 * Abstraction for to split up Discord functions into more focused modules.
 * 
 * @author Jikoo
 */
public abstract class DiscordModule {

	private final Discord discord;

	public DiscordModule(Discord discord) {
		this.discord = discord;
	}

	protected Discord getDiscord() {
		return discord;
	}

	public abstract void doHeartbeat();

}
