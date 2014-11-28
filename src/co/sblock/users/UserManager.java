package co.sblock.users;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import co.sblock.Sblock;
import co.sblock.chat.ColorDef;
import co.sblock.users.User.UserBuilder;

/**
 * Class that keeps track of players currently logged on to the game
 * 
 * @author FireNG, Jikoo
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
	public static void loadUser(final UUID uuid) {
		File file;
		try {
			file = new File(Sblock.getInstance().getUserDataFolder(), uuid.toString() + ".yml");
			if (!file.exists()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					SblockUsers.getSblockUsers().getLogger().warning("File " + uuid.toString() + ".yml does not exist!");
					return;
				}
				player.teleport(getSpawnLocation());

				UserBuilder builder = new UserBuilder();
				User user = builder.build(uuid);
				HashSet<String> defaults = new HashSet<>();
				defaults.add("#");
				defaults.add(Region.EARTH.getChannelName());
				user.loginAddListening(defaults);
				user.updateCurrentRegion(Region.EARTH);
				UserManager.addUser(user);

				Bukkit.broadcastMessage(ColorDef.HAL + "It would seem that " + player.getName()
						+ " is joining us for the first time! Please welcome them.");
				return;
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load data for " + uuid, e);
		}
		UserBuilder builder = new UserBuilder();
		Player player = Bukkit.getPlayer(uuid);
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		if (player != null) {
			builder.setIPAddr(player.getAddress().getHostString());
		} else {
			builder.setIPAddr(yaml.getString("ip"));
		}
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
		if (player != null) {
			user.updateFlight();
			user.updateCurrentRegion(current);
			final String nick = yaml.getString("nickname");

			// Set display name on delay so it takes effect properly prior to listening announce
			new BukkitRunnable() {
				@Override
				public void run() {
					Player player = Bukkit.getPlayer(uuid);
					User user = UserManager.getUser(uuid);
					if (player == null || user == null) {
						return;
					}
					player.setDisplayName(nick);
					user.loginAddListening(user.getListening());
				}
			}.runTask(Sblock.getInstance());
		}
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
		return user;
	}

	public static void saveUser(User user) {
		if (!user.isLoaded()) {
			return;
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
		yaml.set("name", player != null ? user.getPlayerName() : Bukkit.getOfflinePlayer(user.getUUID()).getName());
		yaml.set("ip", user.getUserIP());
		if (player != null) {
			yaml.set("nickname", player.getDisplayName());
			yaml.set("location", BukkitSerializer.locationToString(player.getLocation()));
			yaml.set("playtime", user.getTimePlayed());
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
	}

	public static String getOfflineUserInfo(UUID uuid) {
		File file;
		try {
			file = new File(Sblock.getInstance().getUserDataFolder(), uuid.toString() + ".yml");
			if (!file.exists()) {
				return ChatColor.RED + "No data stored for that user.";
			}
		} catch (IOException e) {
			SblockUsers.getSblockUsers().getLogger().err(e);
			return ChatColor.RED + "Unable to load data! Please report this issue.";
		}
		StringBuilder sb = new StringBuilder();
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
		//+-- Name aka Nickname from IP --+
		sb.append(ChatColor.YELLOW).append(ChatColor.STRIKETHROUGH).append("+--")
				.append(ChatColor.DARK_AQUA).append(' ').append(player.getName())
				.append(ChatColor.YELLOW).append(" aka ").append(ChatColor.DARK_AQUA)
				.append(yaml.getString("nickname", "no nick")).append(ChatColor.YELLOW)
				.append(" from ").append(ChatColor.DARK_AQUA).append(yaml.getString("ip", "ip"))
				.append(ChatColor.YELLOW).append(' ').append(ChatColor.STRIKETHROUGH)
				.append("--+\n");

		// Class of Aspect, dream, planet
		sb.append(ChatColor.DARK_AQUA).append(yaml.getString("classpect.class", "class"))
				.append(ChatColor.YELLOW).append(" of ").append(ChatColor.DARK_AQUA)
				.append(yaml.getString("classpect.aspect", "aspect")).append(ChatColor.YELLOW)
				.append(", ").append(ChatColor.DARK_AQUA)
				.append(yaml.getString("classpect.dream", "dream")).append(ChatColor.YELLOW)
				.append(", ").append(ChatColor.DARK_AQUA)
				.append(yaml.getString("classpect.medium", "medium")).append('\n');

		// Loc: current location TODO, Region: region
		sb.append(ChatColor.YELLOW).append("Loc: ").append(ChatColor.DARK_AQUA)
				.append(yaml.getString("location", "unknown")).append(ChatColor.YELLOW)
				.append('\n');

		// Prev loc: loc prior to change to/from dreamplanet, Prev region: region of said location
		sb.append("Prev loc: ").append(ChatColor.DARK_AQUA)
				.append(yaml.getString("previousLocation", "prev loc")).append(ChatColor.YELLOW)
				.append(", Prev region: ").append(ChatColor.DARK_AQUA)
				.append(yaml.getString("previousRegion", "prev region")).append('\n');

		// Progression: PROGRESSION, Programs: [TODO array]
		sb.append(ChatColor.YELLOW).append("Progression: ").append(ChatColor.DARK_AQUA)
				.append(yaml.getString("progression.progression", "none")).append(ChatColor.YELLOW)
				.append(", Programs: ").append(ChatColor.DARK_AQUA)
				.append(yaml.get("progression.programs")).append('\n');

		// Server: UUID, Client: UUID
		sb.append(ChatColor.YELLOW).append("Server: ").append(ChatColor.DARK_AQUA)
				.append(yaml.getString("progression.server", "none")).append(ChatColor.YELLOW)
				.append(", Client: ").append(ChatColor.DARK_AQUA)
				.append(yaml.getString("progression.client", "none")).append('\n');

		// Pestering: current, Listening: [list]
		sb.append(ChatColor.YELLOW).append("Pestering: ").append(ChatColor.DARK_AQUA)
				.append(yaml.getString("chat.current", "none")).append(ChatColor.YELLOW)
				.append(", Listening: ").append(ChatColor.DARK_AQUA)
				.append(yaml.get("chat.listening")).append('\n');

		// Muted: boolean, Suppressing: boolean
		sb.append(ChatColor.YELLOW).append("Muted: ").append(ChatColor.DARK_AQUA)
				.append(yaml.getBoolean("chat.muted", false)).append(ChatColor.YELLOW)
				.append(", Suppressing: ").append(ChatColor.DARK_AQUA)
				.append(yaml.getBoolean("chat.suppressing", false)).append('\n');

		// Last seen: date, Playtime: X days, XX:XX
		sb.append(ChatColor.YELLOW).append("Last login: ").append(ChatColor.DARK_AQUA)
				.append(new SimpleDateFormat("HH:mm 'on' dd/MM/YY").format(new Date(player.getLastPlayed())))
				.append(ChatColor.YELLOW).append(", Time ingame: ").append(ChatColor.DARK_AQUA)
				.append(yaml.getString("playtime", "unknown"));

		return sb.toString();
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

	public static Location getSpawnLocation() {
		return new Location(Bukkit.getWorld("Earth"), -3.5, 20, 6.5, 179.99F, 1F);
	}
}
