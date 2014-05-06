package co.sblock.users;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import co.sblock.chat.ColorDef;
import co.sblock.data.SblockData;

/**
 * Class that keeps track of players currently logged on to the game
 * 
 * @author FireNG, Jikoo
 */
public class UserManager {

	/** The UserManager instance. */
	private static UserManager manager;

	/** The Map of Player UUID and relevant SblockUsers currently online. */
	private Map<UUID, User> users;

	/** Constructor for UserManager. */
	UserManager() {
		manager = this;
		this.users = new HashMap<>();
	}

	/**
	 * Adds a Player to the users list
	 * 
	 * @param player the name of the Player
	 */
	public User addUser(UUID userID) {
		if (users.containsKey(userID)) {
			return users.get(userID);
		}
		User u = new User(userID);
		users.put(userID, u);
		return u;
	}

	/**
	 * Removes a Player from the users list.
	 * 
	 * @param player the name of the Player to remove
	 * @return the SblockUser for the removed player, if any
	 */
	public User removeUser(UUID userID) {
		return users.remove(userID);
	}

	/**
	 * Gets a SblockUserby Player name.
	 * 
	 * @param name the name of the Player to look up
	 * 
	 * @return the SblockUser associated with the given Player, or null if no
	 *         Player with the given name is currently online.
	 */
	public User getUser(UUID userID) {
		if (users.containsKey(userID)) {
			return users.get(userID);
		}
		if (Bukkit.getPlayer(userID) != null) {
			return SblockData.getDB().loadUserData(userID);
		}
		return null;
	}

	/**
	 * Gets a Collection of SblockUsers currently online.
	 * 
	 * @return the SblockUsers currently online
	 */
	public Collection<User> getUserlist() {
		return this.users.values();
	}

	/**
	 * Add a Player to their group's Team.
	 * 
	 * @param p the Player
	 */
	public void team(Player p) {
		String teamName;
		if (p.hasPermission("group.horrorterror")) {
			teamName = "horrorterror";
		} else if (p.hasPermission("group.denizen")) {
			teamName = "denizen";
		} else if (p.hasPermission("group.felt")) {
			teamName = "felt";
		} else if (p.hasPermission("group.helper")) {
			teamName = "helper";
		} else if (p.hasPermission("group.donator")) {
			teamName = "donator";
		} else if (p.hasPermission("group.godtier")) {
			teamName = "godtier";
		} else {
			teamName = "hero";
		}
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = board.getTeam(teamName);
		if (team == null) {
			team = board.registerNewTeam(teamName);
			team.setPrefix(getTeamPrefix(teamName));
		}
		team.addPlayer(p);
	}

	/**
	 * Fetches team prefixes.
	 */
	private String getTeamPrefix(String teamName) {
		if (teamName.equals("horrorterror")) {
			return ColorDef.RANK_HORRORTERROR.toString();
		} else if (teamName.equals("denizen")) {
			return ColorDef.RANK_DENIZEN.toString();
		} else if (teamName.equals("felt")) {
			return ColorDef.RANK_FELT.toString();
		} else if (teamName.equals("helper")) {
			return ColorDef.RANK_HELPER.toString();
		} else if (teamName.equals("godtier")) {
			return ColorDef.RANK_GODTIER.toString();
		}
		return ColorDef.RANK_HERO.toString();
	}

	/**
	 * Gets the UserManager instance.
	 * 
	 * @return the UserManager instance
	 */
	public static UserManager getUserManager() {
		if (manager == null)
			manager = new UserManager();
		return manager;
	}
}
