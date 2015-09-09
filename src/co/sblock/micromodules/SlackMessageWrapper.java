package co.sblock.micromodules;

import java.util.UUID;

import com.ullink.slack.simpleslackapi.impl.SlackChatConfiguration;

import net.md_5.bungee.api.ChatColor;

/**
 * Small tuple for data required for a Slack message to be sent later.
 * 
 * @author Jikoo
 */
public class SlackMessageWrapper {

	// Alternative services:
	// https://crafatar.com/renders/head/%s?helm
	// https://minotar.net/cube/%s/128.png
	private static final String ICON_URL = "https://cravatar.eu/helmhead/%s/128.png";

	private final String channel;
	private final String message;
	private final UUID uuid;
	private final String name;

	public SlackMessageWrapper(String channel, String message, UUID uuid, String name) {
		this.channel = channel;
		this.message = ChatColor.stripColor(message);
		this.uuid = uuid;
		this.name = ChatColor.stripColor(name);
	}

	public String getChannel() {
		return this.channel;
	}

	public String getMessage() {
		return this.message;
	}

	public SlackChatConfiguration getConfiguration() {
		SlackChatConfiguration config = SlackChatConfiguration.getConfiguration().withName(name);
		if (uuid != null) {
			config.withIcon(String.format(ICON_URL, uuid));
		}
		return config;
	}
}
