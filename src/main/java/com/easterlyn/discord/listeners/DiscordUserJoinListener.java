package com.easterlyn.discord.listeners;

import com.easterlyn.discord.Discord;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;

/**
 * Listener for when a user rejoins after leaving or being kicked.
 * 
 * @author Jikoo
 */
public class DiscordUserJoinListener implements IListener<UserJoinEvent> {

	private final Discord discord;

	public DiscordUserJoinListener(Discord discord) {
		this.discord = discord;
	}

	@Override
	public void handle(UserJoinEvent event) {
		// Reset unlinked time for new joins
		discord.getDatastore().set("unlinked." + event.getGuild().getLongID() + '.'  + event.getUser().getLongID(), null);
		discord.updateUser(event.getUser());
	}

}
