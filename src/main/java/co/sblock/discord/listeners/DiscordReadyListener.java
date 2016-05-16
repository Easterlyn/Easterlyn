package co.sblock.discord.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.discord.Discord;
import co.sblock.discord.DiscordEndpointUtils;
import co.sblock.discord.abstraction.CallPriority;
import co.sblock.discord.abstraction.DiscordCallable;

import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

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
			sb.delete(0, sb.length());

			IRole linkedRole = null;
			if (discord.getConfig().isSet("linkedRole." + guild.getID())) {
				String roleID = discord.getConfig().getString("linkedRole." + guild.getID());
				linkedRole = guild.getRoleByID(roleID);
			}

			boolean hasPermission = true;
			for (IUser user : guild.getUsers()) {
				UUID uuid = discord.getUUIDOf(user);
				if (uuid == null) {
					continue;
				}
				if (linkedRole != null) {
					List<IRole> roles = user.getRolesForGuild(guild);
					if (!roles.contains(linkedRole)) {
						roles = new ArrayList<>(roles);
						roles.add(linkedRole);
						IRole[] roleArray = roles.toArray(new IRole[roles.size()]);
						discord.queue(new DiscordCallable() {
							@Override
							public void call() throws DiscordException, HTTP429Exception, MissingPermissionsException {
								guild.editUserRoles(user, roleArray);
							}
						});
					}
				}
				if (!hasPermission) {
					continue;
				}
				OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				if (player == null || player.getName() == null) {
					continue;
				}
				try {
					DiscordEndpointUtils.queueNickSet(discord, CallPriority.LOW, guild, user, player.getName());
				} catch (Exception e) {
					hasPermission = false;
				}
			}
		}
	}

}
