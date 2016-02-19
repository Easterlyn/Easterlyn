package co.sblock.discord;

import sx.blah.discord.handle.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

/**
 * IListener for successful Discord connection.
 * 
 * @author Jikoo
 */
public class DiscordReadyListener implements IListener<ReadyEvent> {

	private final Discord discord;

	protected DiscordReadyListener(Discord discord) {
		this.discord = discord;
	}

	@Override
	public void handle(ReadyEvent event) {
		StringBuilder sb = new StringBuilder();
		for (IGuild guild : discord.getAPI().getGuilds()) {
			discord.getLogger().info("Available channels in " + guild.getName() + " (" + guild.getID() + "):");
			for (IChannel channel : guild.getChannels()) {
				sb.append(channel.getName()).append(':').append(channel.getID()).append(' ');
			}
			sb.deleteCharAt(sb.length() - 1);
			discord.getLogger().info(sb.toString());
			sb.delete(0, sb.length());
		}

		discord.startQueueDrain();
		discord.startHeartbeat();
	}

}
