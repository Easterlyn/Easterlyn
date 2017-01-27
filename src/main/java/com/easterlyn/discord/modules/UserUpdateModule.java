package com.easterlyn.discord.modules;

import com.easterlyn.discord.Discord;
import com.easterlyn.discord.abstraction.DiscordModule;

/**
 * Module for periodic user state update ticking.
 * 
 * @author Jikoo
 */
public class UserUpdateModule extends DiscordModule {

	public UserUpdateModule(Discord discord) {
		super(discord);
	}

	@Override
	public void doSetup() {}

	@Override
	public void doHeartbeat() {
		this.getDiscord().getClient().getUsers().forEach(user -> this.getDiscord().updateUser(user));
	}

}
