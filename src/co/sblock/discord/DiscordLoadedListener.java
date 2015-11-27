package co.sblock.discord;

import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.event.EventListener;
import me.itsghost.jdiscord.events.APILoadedEvent;
import me.itsghost.jdiscord.talkable.Group;

/**
 * EventListener 
 * 
 * @author Jikoo
 */
public class DiscordLoadedListener implements EventListener {

	private final Discord discord;

	protected DiscordLoadedListener(Discord discord) {
		this.discord = discord;
	}

	public void onAPILoadEvent(APILoadedEvent event) {
		StringBuilder sb = new StringBuilder();
		for (Server server : discord.getAPI().getAvailableServers()) {
			discord.getLogger().info("Available channels in " + server.getName() + " (" + server.getId() + "):");
			for (Group group : server.getGroups()) {
				sb.append(group.getName()).append(':').append(group.getId()).append(' ');
			}
			sb.deleteCharAt(sb.length() - 1);
			discord.getLogger().info(sb.toString());
			sb.delete(0, sb.length());
		}

		discord.startPostingMessages();
	}

}
