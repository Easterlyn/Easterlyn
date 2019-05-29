package com.easterlyn.discord.abstraction;

import com.easterlyn.discord.Discord;

/**
 * Abstraction for to split up Discord functions into more focused modules.
 * 
 * @author Jikoo
 */
public abstract class DiscordModule {

	private final Discord discord;

	protected DiscordModule(Discord discord) {
		this.discord = discord;
	}

	protected Discord getDiscord() {
		return discord;
	}

	/**
	 * A method for handling any post-load setup that needs to happen.
	 * 
	 * Anything that requires calls to Discord should be done here, as this method is called when
	 * the DiscordClient has successfully logged in.
	 */
	public abstract void doSetup();

	/**
	 * A method for handling any periodic checks. This is called at regular intervals by the Discord
	 * module.
	 */
	public abstract void doHeartbeat();

}
