package co.sblock.users;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import co.sblock.Sblock;
import co.sblock.chat.ColorDef;
import co.sblock.effects.EffectManager;
import co.sblock.users.User.UserBuilder;

/**
 * Class that keeps track of players currently logged on to the game
 * 
 * @author FireNG, Jikoo, tmathmeyer
 */
public class UserManager {

	/* The Map of Player UUID and relevant SblockUsers currently online. */
	private static final Map<UUID, User> users = new ConcurrentHashMap<>();

	/**
	 * Get a User object for the UUID given.
	 * 
	 * @param player the name of the Player
	 */
	public static User addUser(UUID userID) {
		User user = null;
		if (users.containsKey(userID)) {
			user = users.get(userID);
		}
		if (user == null || !user.isLoaded()) {
			user = new UserBuilder().build(userID);
			users.put(userID, user);
		}
		return user;
	}

	/**
	 * Adds the given User.
	 * 
	 * @param user the User to add
	 */
	public static void addUser(User user) {
		users.put(user.getUUID(), user);
		user.setLoaded();
	}

	@SuppressWarnings("unchecked")
	public static void loadUser(UUID uuid) {
		UserBuilder builder = new UserBuilder();
		File file;
		try {
			file = new File(Sblock.getInstance().getUserDataFolder(), uuid.toString() + ".yml");
			if (!file.exists()) {
				//getLogger().warning("File " + uuid.toString() + ".yml does not exist!");
				// TODO Do first login
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load data for " + uuid, e);
		}
		Player player = Bukkit.getPlayer(uuid);
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		player.setDisplayName(yaml.getString("nickname"));
		builder.setIPAddr(yaml.getString("ip"));
		builder.setPreviousLocationFromString(yaml.getString("previousLocation"));
		//yaml.getString("previousRegion");
		builder.setUserClass(UserClass.getClass(yaml.getString("classpect.class")));
		builder.setAspect(UserAspect.getAspect(yaml.getString("classpect.aspect")));
		Region dream = Region.getRegion(yaml.getString("classpect.dream"));
		builder.setDreamPlanet(dream);
		Region current = Region.getRegion(player.getWorld().getName());
		if (current.isDream()) {
			current = dream;
		}
		builder.setCurrentRegion(current);
		builder.setMediumPlanet(Region.getRegion(yaml.getString("classpect.medium")));
		builder.setProgression(ProgressionState.valueOf(yaml.getString("progression.progression")));
		builder.setPrograms((HashSet<Integer>) yaml.get("progression.programs"));
		if (yaml.getString("progression.server") != null) {
			builder.setServer(UUID.fromString(yaml.getString("progression.server")));
		}
		if (yaml.getString("progression.client") != null) {
			builder.setClient(UUID.fromString(yaml.getString("progression.client")));
		}
		builder.setCurrentChannel(yaml.getString("chat.current"));
		builder.setListening((HashSet<String>) yaml.get("chat.listening"));
		builder.setGlobalMute(new AtomicBoolean(yaml.getBoolean("chat.muted")));
		builder.setSuppress(new AtomicBoolean(yaml.getBoolean("chat.suppressing")));
		//(Set<String>) yaml.get("chat.ignoring");
		User user = builder.build(uuid);
		user.updateFlight();
		user.updateCurrentRegion(current);
		user.loginAddListening(user.getListening());
		addUser(user);
	}

	/**
	 * Removes a Player from the users list.
	 * 
	 * @param player the name of the Player to remove
	 * @return the SblockUser for the removed player, if any
	 */
	public static User unloadUser(UUID userID) {
		User user = users.remove(userID);
		if (!user.isLoaded()) {
			return user;
		}
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
		yaml.set("name", user.getPlayerName());
		yaml.set("ip", user.getUserIP());
		if (player != null) {
			yaml.set("nickname", player.getDisplayName());
			yaml.set("location", BukkitSerializer.locationToString(player.getLocation()));
		}
		yaml.set("region", user.getCurrentRegion().getDisplayName());
		yaml.set("previousLocation", BukkitSerializer.locationToString(user.getPreviousLocation()));
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
		yaml.set("chat.current", user.getCurrent() != null ? user.getCurrent().getName() : "#");
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
		return user;
	}

	/**
	 * 
	 * @param userID the UUID of the Player to look up
	 * 
	 * @return the SblockUser associated with the given Player, or null if no
	 *         Player with the given ID is currently online.
	 */
	public static User getUser(UUID userID) {
		if (users.containsKey(userID)) {
			return users.get(userID);
		}
		return null;
	}

	/**
	 * Gets a Collection of SblockUsers currently online.
	 * 
	 * @return the SblockUsers currently online
	 */
	public static Collection<User> getUsers() {
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

	/**
	 * 
	 * @param p the player to build the user around
	 */
	public static User doFirstLogin(Player p)
	{
		//player's first login
		Bukkit.broadcastMessage(ColorDef.HAL + "It would seem that " + p.getName() + " is joining us for the first time! Please welcome them.");
		p.teleport(getSpawnLocation());

		User user = addUser(p.getUniqueId());
		user.loginAddListening(new HashSet<String>(Arrays.asList(new String[]{"#" , "#" + user.getPlayerRegion().name()}))); // TODO hiiiiideous
		// TODO: oh god plz
		user.updateCurrentRegion(user.getPlayerRegion());

		user.setAllPassiveEffects(EffectManager.passiveScan(p));
		EffectManager.applyPassiveEffects(user);

		user.setLoaded();

		return user;
	}

	public static Location getSpawnLocation() {
		return new Location(Bukkit.getWorld("Earth"), -3.5, 20, 6.5, 179.99F, 1F);
	}
}
