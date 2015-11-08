package co.sblock.users;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import co.sblock.chat.Color;
import co.sblock.module.Module;

/**
 * Class that keeps track of players currently logged on to the game.
 * 
 * @author FireNG, Jikoo
 */
public class Users extends Module {

	private static Users instance;

	/* The Map of Player UUID and relevant SblockUsers currently online. */
	private static final Map<UUID, OfflineUser> users = new ConcurrentHashMap<>();

	@Override
	protected void onEnable() {
		instance = this;
		for (Player p : Bukkit.getOnlinePlayers()) {
			getGuaranteedUser(p.getUniqueId());
			team(p);
		}
	}

	@Override
	protected void onDisable() {
		for (OfflineUser u : Users.getUsers().toArray(new OfflineUser[0])) {
			unloadUser(u.getUUID()).save();
			unteam(u.getUUID().toString().replace("-", "").substring(0, 16));
		}
		instance = null;
	}

	@Override
	protected String getModuleName() {
		return "Sblock UserManager";
	}

	public static Users getInstance() {
		return instance;
	}

	/**
	 * User is not guaranteed to be online, but an OfflineUser will be fetched no matter what.
	 * 
	 * @param uuid
	 * @return
	 */
	public static OfflineUser getGuaranteedUser(UUID uuid) {
		if (users.containsKey(uuid)) {
			return users.get(uuid);
		}
		OfflineUser user = OfflineUser.load(uuid);
		if (user.isOnline()) {
			user = user.getOnlineUser();
			users.put(uuid, user);
		} else {
			return user;
		}
		return user;
	}

	protected static OnlineUser getOnlineUser(UUID uuid) {
		if (users.containsKey(uuid)) {
			OfflineUser user = users.get(uuid);
			if (user instanceof OnlineUser) {
				return (OnlineUser) user;
			}
		}
		return null;
	}

	/**
	 * Adds the given User.
	 * 
	 * @param user the User to add
	 */
	public static void addUser(OfflineUser user) {
		users.put(user.getUUID(), user);
	}

	/**
	 * Removes a Player from the users list.
	 * 
	 * @param player the name of the Player to remove
	 * @return the SblockUser for the removed player, if any
	 */
	public static OfflineUser unloadUser(UUID userID) {
		if (!users.containsKey(userID)) {
			return null;
		}
		return users.remove(userID);
	}

	/**
	 * Gets a Collection of OfflineUsers currently loaded.
	 * 
	 * @return the OfflineUsers
	 */
	public static Collection<OfflineUser> getUsers() {
		return users.values();
	}

	/**
	 * Add a Player to their group's Team.
	 * 
	 * @param player the Player
	 */
	public static void team(Player player) {
		StringBuilder prefixBuilder = new StringBuilder();
		for (net.md_5.bungee.api.ChatColor color : Color.COLORS) {
			if (player.hasPermission("sblockchat." + color.name().toLowerCase())) {
				prefixBuilder.append(color);
				break;
			}
		}
		if (prefixBuilder.length() > 0) {
			// Do nothing, we've got a fancy override going on
		} else if (player.hasPermission("sblock.horrorterror")) {
			prefixBuilder.append(Color.RANK_HORRORTERROR.toString());
		} else if (player.hasPermission("sblock.denizen")) {
			prefixBuilder.append(Color.RANK_DENIZEN.toString());
		} else if (player.hasPermission("sblock.felt")) {
			prefixBuilder.append(Color.RANK_FELT.toString());
		} else if (player.hasPermission("sblock.helper")) {
			prefixBuilder.append(Color.RANK_HELPER.toString());
		} else if (player.hasPermission("sblock.donator")) {
			prefixBuilder.append(Color.RANK_DONATOR.toString());
		} else if (player.hasPermission("sblock.godtier")) {
			prefixBuilder.append(Color.RANK_GODTIER.toString());
		} else {
			prefixBuilder.append(Color.RANK_HERO.toString());
		}
		for (net.md_5.bungee.api.ChatColor color : Color.FORMATS) {
			if (player.hasPermission("sblockchat." + color.name().toLowerCase())) {
				prefixBuilder.append(color);
			}
		}
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		String teamName = player.getName();
		Team team = board.getTeam(teamName);
		if (team == null) {
			team = board.registerNewTeam(teamName);
		}
		team.setPrefix(prefixBuilder.toString());
		team.addEntry(player.getName());
		String name = player.getDisplayName();
		if (name != null && name.length() <= 16) {
			team.addEntry(name);
		}

		Objective objective = board.getObjective("deaths");
		if (objective == null) {
			objective = board.registerNewObjective("deaths", "deathCount");
			objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		}

		// Since Mojang doesn't, we'll force deathcount to persist - it's been a feature for ages
		Score score = objective.getScore(player.getName());
		score.setScore(player.getStatistic(Statistic.DEATHS));
	}

	public static void unteam(Player player) {
		unteam(player.getName());
	}

	private static void unteam(String teamName) {
		Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
		if (team != null) {
			team.unregister();
		}
	}

	public static Location getSpawnLocation() {
		return new Location(Bukkit.getWorld("Earth"), -3.5, 20, 6.5, 179.99F, 1F);
	}
}
