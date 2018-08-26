package com.easterlyn.users;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Color;
import com.easterlyn.module.Module;
import com.easterlyn.utilities.CollectionConversions;
import com.easterlyn.utilities.player.PermissionBridge;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Class that keeps track of players currently logged on to the game.
 *
 * @author FireNG, Jikoo
 */
public class Users extends Module {

	/**
	 * Add a Player to a Team colored based on permissions.
	 *
	 * @param player the Player
	 */
	public static void team(final Player player, String prefix) {
		if (player == null) {
			return;
		}
		StringBuilder prefixBuilder = new StringBuilder();
		if (prefix != null && !prefix.isEmpty()) {
			prefixBuilder.append(prefix);
		}
		for (net.md_5.bungee.api.ChatColor color : Color.COLORS) {
			if (player.hasPermission("easterlyn.chat.color." + color.name().toLowerCase())) {
				prefixBuilder.append(color);
				break;
			}
		}

		if (prefix == null && prefixBuilder.length() == 0 || prefixBuilder.length() - prefix.length() == 0) {
			UserRank[] ranks = UserRank.values();
			for (int i = ranks.length - 1; i >= 0; --i) {
				UserRank rank = ranks[i];
				if (player.hasPermission(rank.getPermission())) {
					prefixBuilder.append(rank.getColor());
					break;
				}
			}
		}

		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		String teamName = player.getName();
		Team team = board.getTeam(teamName);
		if (team == null) {
			team = board.registerNewTeam(teamName);
		}
		prefix = prefixBuilder.length() <= 16 ? prefixBuilder.toString()
				: prefixBuilder.substring(prefixBuilder.length() - 16, prefixBuilder.length());
		team.setPrefix(prefix);
		team.addEntry(player.getName());
		team.addEntry(player.getPlayerListName());
	}

	public static void unteam(final Player player) {
		Users.unteam(player.getName());
	}

	private static void unteam(final String teamName) {
		if (teamName == null) {
			return;
		}
		Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
		if (team != null) {
			team.unregister();
		}
	}

	/* The Cache of Player UUID and relevant Users. */
	private final LoadingCache<UUID, User> userCache;

	public Users(final Easterlyn plugin) {
		super(plugin);
		this.userCache = CacheBuilder.newBuilder()
				.expireAfterAccess(30L, TimeUnit.MINUTES)
				.removalListener(notification -> {
					User user = (User) notification.getValue();
					user.save();
					Users.unteam(user.getPlayerName());
					PermissionBridge.releasePermissionData(user.getUUID());
				}).build(new CacheLoader<UUID, User>() {
					@Override
					public User load(final UUID uuid) {
						User user = User.load(Users.this.getPlugin(), uuid);
						Users.team(user.getPlayer(), null);
						return user;
					}
				});
	}

	@Override
	public String getName() {
		return "Users";
	}

	public Set<User> getOnlineUsers() {
		return CollectionConversions.toSet(Bukkit.getOnlinePlayers(), player -> this.getUser(player.getUniqueId()));
	}

	/**
	 * Fetch a User. A User is always returned, even if the Player by the given UUID is not online.
	 *
	 * @param uuid the UUID of the User
	 *
	 * @return the User
	 */
	public User getUser(final UUID uuid) {
		return this.userCache.getUnchecked(uuid);
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	protected void onDisable() {
		// Invalidating and cleaning up causes our removal listener to save all cached users.
		this.userCache.invalidateAll();
		this.userCache.cleanUp();
	}

	@Override
	protected void onEnable() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			this.getUser(player.getUniqueId());
			Users.team(player, null);
		}
	}

}
