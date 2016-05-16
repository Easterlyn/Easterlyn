package co.sblock.discord.listeners;

import java.util.UUID;

import co.sblock.discord.Discord;

import org.bukkit.scheduler.BukkitRunnable;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

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
		for (IGuild guild : discord.getClient().getGuilds()) {
			discord.getLogger().info("Available channels in " + guild.getName() + " (" + guild.getID() + "):");
			for (IChannel channel : guild.getChannels()) {
				sb.append(channel.getName()).append(':').append(channel.getID()).append(' ');
			}
			sb.deleteCharAt(sb.length() - 1);
			discord.getLogger().info(sb.toString());

			for (IUser user : guild.getUsers()) {
				UUID uuid = discord.getUUIDOf(user);
				if (uuid == null) {
					continue;
				}
				discord.updateDiscordState(user, uuid);
			}
		}
	}

}
