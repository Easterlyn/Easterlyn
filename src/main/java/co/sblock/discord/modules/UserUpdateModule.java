package co.sblock.discord.modules;

import co.sblock.discord.Discord;
import co.sblock.discord.abstraction.DiscordModule;

/**
 * Module for
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
