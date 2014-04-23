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

	/**The Scoreboard used to color Player names by rank. */
	private Scoreboard board;

	/** Constructor for UserManager. */
	UserManager() {
		manager = this;
		this.users = new HashMap<>();
		this.createTeams();
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
		return SblockData.getDB().loadUserData(userID);
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
	 * Creates teams for display name coloring.
	 */
	private void createTeams() {
		this.board = Bukkit.getScoreboardManager().getMainScoreboard();
		String[] teams = new String[] {"horrorterror", "denizen", "felt", "helper", "donator", "godtier", "hero"};
		Team team;
		for (String teamName : teams) {
			team = this.board.getTeam(teamName);
			if (team == null) {
				team = this.board.registerNewTeam(teamName);
				if (teamName.equals("horrorterror")) {
					team.setPrefix(ColorDef.RANK_HORRORTERROR.toString());
				} else if (teamName.equals("denizen")) {
					team.setPrefix(ColorDef.RANK_DENIZEN.toString());
				} else if (teamName.equals("felt")) {
					team.setPrefix(ColorDef.RANK_FELT.toString());
				} else if (teamName.equals("helper")) {
					team.setPrefix(ColorDef.RANK_HELPER.toString());
				} else if (teamName.equals("godtier")) {
					team.setPrefix(ColorDef.RANK_GODTIER.toString());
				} else {
					team.setPrefix(ColorDef.RANK_HERO.toString());
				}
			}
		}
	}

	/**
	 * Add a Player to their group's Team.
	 * 
	 * @param p the Player
	 */
	public void team(Player p) {
		Team team = null;
		if (p.hasPermission("group.horrorterror")) {
			team = this.board.getTeam("horrorterror");
		} else if (p.hasPermission("group.denizen")) {
			team = this.board.getTeam("denizen");
		} else if (p.hasPermission("group.felt")) {
			team = this.board.getTeam("felt");
		} else if (p.hasPermission("group.helper")) {
			team = this.board.getTeam("helper");
		} else if (p.hasPermission("group.donator")) {
			team = this.board.getTeam("donator");
		} else if (p.hasPermission("group.godtier")) {
			team = this.board.getTeam("godtier");
		} else {
			team = this.board.getTeam("hero");
		}
		team.addPlayer(p);
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
