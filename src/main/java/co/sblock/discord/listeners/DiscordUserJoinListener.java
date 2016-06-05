package co.sblock.discord.listeners;

import java.util.UUID;

import co.sblock.discord.Discord;

import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.UserJoinEvent;

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

		UUID uuid = discord.getUUIDOf(event.getUser());

		if (uuid == null) {
			return;
		}

		discord.updateDiscordState(event.getUser(), uuid);
	}

}
