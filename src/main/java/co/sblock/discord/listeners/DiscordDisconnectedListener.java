package co.sblock.discord.listeners;

import co.sblock.discord.Discord;

import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.util.DiscordException;

/**
 * IListener for Discord disconnection.
 * 
 * @author Jikoo
 */
public class DiscordDisconnectedListener implements IListener<DiscordDisconnectedEvent> {

	private final Discord discord;

	public DiscordDisconnectedListener(Discord discord) {
		this.discord = discord;
	}

	@Override
	public void handle(DiscordDisconnectedEvent event) {
		discord.setReady(false);
		discord.getLogger().info("Disconnected from Discord: " + event.getReason().name());
		switch (event.getReason()) {
		case MISSED_PINGS:
		case TIMEOUT:
		case UNKNOWN:
			try {
				discord.getClient().login();
			} catch (DiscordException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}

}
