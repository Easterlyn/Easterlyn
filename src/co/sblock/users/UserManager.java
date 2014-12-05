package co.sblock.users;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import co.sblock.Sblock;
import co.sblock.chat.ColorDef;
import co.sblock.module.Module;

/**
 * Class that keeps track of players currently logged on to the game.
 * 
 * @author FireNG, Jikoo
 */
public class UserManager  extends Module {

	private static UserManager instance;

	/** Map containing all server/client player requests */
	private Map<String, String> requests;

	@Override
	protected void onEnable() {
		instance = this;
		requests = new HashMap<String, String>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			UserManager.getGuaranteedUser(p.getUniqueId());
		}
	}

	@Override
	protected void onDisable() {
		for (OfflineUser u : UserManager.getUsers().toArray(new OfflineUser[0])) {
			UserManager.saveUser(UserManager.unloadUser(u.getUUID()));
		}
		instance = null;
	}

	@Override
	protected String getModuleName() {
		return "Sblock UserManager";
	}

	/**
	 * Gets a Map of all pending requests.
	 * 
	 * @return a Map of all pending requests
	 */
	public Map<String, String> getRequests() {
		return requests;
	}

	public static UserManager getUserManager() {
		return instance;
	}

	/* The Map of Player UUID and relevant SblockUsers currently online. */
	private static final Map<UUID, OfflineUser> users = new ConcurrentHashMap<>();

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

	public static void saveUser(OfflineUser user) {
		File file;
		try {
			file = new File(Sblock.getInstance().getUserDataFolder(), user.getUUID().toString() + ".yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to save data for " + user.getUUID().toString(), e);
		}
		Player player = Bukkit.getPlayer(user.getUUID());
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		yaml.set("name", player != null ? user.getPlayerName() : Bukkit.getOfflinePlayer(user.getUUID()).getName());
		yaml.set("ip", user.getUserIP());
		if (player != null) {
			yaml.set("nickname", player.getDisplayName());
			yaml.set("location", BukkitSerializer.locationToBlockCenterString(player.getLocation()));
			yaml.set("playtime", user.getTimePlayed());
		}
		yaml.set("region", user.getCurrentRegion().getDisplayName());
		yaml.set("previousLocation", BukkitSerializer.locationToBlockCenterString(user.getPreviousLocation()));
		yaml.set("previousRegion", null);
		yaml.set("flying", user.canFly());
		yaml.set("classpect.class", user.getUserClass().getDisplayName());
		yaml.set("classpect.aspect", user.getAspect().getDisplayName());
		yaml.set("classpect.dream", user.getDreamPlanet().getDisplayName());
		yaml.set("classpect.medium", user.getMediumPlanet().getDisplayName());
		yaml.set("progression.progression", user.getProgression().name());
		yaml.set("progression.programs", user.getPrograms());
		yaml.set("progression.server", user.getServer() != null ? user.getServer().toString() : null);
		yaml.set("progression.client", user.getClient() != null ? user.getClient().toString() : null);
		yaml.set("chat.current", user.getCurrentChannel() != null ? user.getCurrentChannel().getName() : "#");
		yaml.set("chat.listening", user.getListening());
		yaml.set("chat.muted", user.isMute());
		yaml.set("chat.suppressing", user.isSuppressing());
		yaml.set("chat.ignoring", null);
		yaml.set("chat.highlights", null);
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save data for " + user.getPlayerName(), e);
		}
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
	 * @param p the Player
	 */
	public static void team(Player p) {
		String teamPrefix = null;
		for (ChatColor c : ChatColor.values()) {
			if (p.hasPermission("sblockchat." + c.name().toLowerCase())) {
				teamPrefix = c.toString();
				break;
			}
		}
		if (teamPrefix != null) {
			// Do nothing, we've got a fancy override going on
		} else if (p.hasPermission("group.horrorterror")) {
			teamPrefix = ColorDef.RANK_HORRORTERROR.toString();
		} else if (p.hasPermission("group.denizen")) {
			teamPrefix = ColorDef.RANK_DENIZEN.toString();
		} else if (p.hasPermission("group.felt")) {
			teamPrefix = ColorDef.RANK_FELT.toString();
		} else if (p.hasPermission("group.helper")) {
			teamPrefix = ColorDef.RANK_HELPER.toString();
		} else if (p.hasPermission("group.donator")) {
			teamPrefix = ColorDef.RANK_DONATOR.toString();
		} else if (p.hasPermission("group.godtier")) {
			teamPrefix = ColorDef.RANK_GODTIER.toString();
		} else {
			teamPrefix = ColorDef.RANK_HERO.toString();
		}
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = board.getTeam(p.getName());
		if (team == null) {
			team = board.registerNewTeam(p.getName());
		}
		team.setPrefix(teamPrefix);
		team.addPlayer(p);
	}

	public static void unteam(Player p) {
		Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(p.getName());
		if (team != null) {
			team.unregister();
		}
	}

	public static Location getSpawnLocation() {
		return new Location(Bukkit.getWorld("Earth"), -3.5, 20, 6.5, 179.99F, 1F);
	}
}
