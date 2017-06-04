package com.easterlyn.discord.listeners;

import com.easterlyn.discord.Discord;

import org.bukkit.scheduler.BukkitRunnable;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;

/**
 * IListener for successful Discord connection.
 * 
 * @author Jikoo
 */
public class DiscordReadyListener implements IListener<ReadyEvent> {

	private final Discord discord;

	public DiscordReadyListener(Discord discord) {
		this.discord = discord;
	}

	@Override
	public void handle(ReadyEvent event) {
		discord.setReady(true);

		new BukkitRunnable() {
			@Override
			public void run() {
				postReady();
			}
		}.runTaskLaterAsynchronously(discord.getPlugin(), 150L);
	}

	private void postReady() {
		StringBuilder sb = new StringBuilder();
		this.discord.getClient().getGuilds().forEach(guild -> {
			discord.getLogger().info("Available channels in " + guild.getName() + " (" + guild.getLongID() + "):");

			if (sb.length() > 0) {
				sb.delete(0, sb.length());
			}

			guild.getChannels().forEach(channel -> sb.append(channel.getName()).append(':').append(channel.getLongID()).append(' '));

			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}

			discord.getLogger().info(sb.toString());
		});

		discord.getClient().getUsers().forEach(discord::updateUser);
	}

}
