package co.sblock.discord.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import co.sblock.discord.Discord;
import co.sblock.discord.DiscordEndpointUtils;
import co.sblock.discord.abstraction.CallPriority;

import sx.blah.discord.api.IListener;
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

		StringBuilder sb = new StringBuilder();
		guild: for (IGuild guild : discord.getClient().getGuilds()) {
			discord.getLogger().info("Available channels in " + guild.getName() + " (" + guild.getID() + "):");
			for (IChannel channel : guild.getChannels()) {
				sb.append(channel.getName()).append(':').append(channel.getID()).append(' ');
			}
			sb.deleteCharAt(sb.length() - 1);
			discord.getLogger().info(sb.toString());
			sb.delete(0, sb.length());

			for (IUser user : guild.getUsers()) {
				UUID uuid = discord.getUUIDOf(user);
				if (uuid == null) {
					return;
				}
				OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				if (player == null || player.getName() == null) {
					return;
				}
				try {
					DiscordEndpointUtils.queueNickSet(discord, CallPriority.LOW, guild, user, player.getName());
				} catch (Exception e) {
					continue guild;
				}
			};
		}
	}

}
