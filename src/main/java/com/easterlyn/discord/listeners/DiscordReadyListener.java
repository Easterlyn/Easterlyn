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
				discord.getClient().getUsers().forEach(discord::updateUser);
			}
		}.runTaskLaterAsynchronously(discord.getPlugin(), 150L);
	}

}
