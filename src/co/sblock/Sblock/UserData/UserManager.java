package co.sblock.Sblock.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import co.sblock.Sblock.Chat.ColorDef;

/**
 * Class that keeps track of players currently logged on to the game
 * 
 * @author FireNG, Jikoo
 */
public class UserManager {

	/** The UserManager instance. */
	private static UserManager manager;

	/** The Map of Player names and relevant SblockUsers currently online. */
	private Map<String, SblockUser> users;

	/**The Scoreboard used to color Player names by rank. */
	private Scoreboard board;

	/** Constructor for UserManager. */
	UserManager() {
		manager = this;
		this.users = new HashMap<String, SblockUser>();
		this.createTeams();
	}

	/**
	 * Adds a Player to the users list
	 * 
	 * @param player the name of the Player
	 */
	public SblockUser addUser(String name) {
		if (users.containsKey(name)) {
			return users.get(name);
		}
		SblockUser u = new SblockUser(name);
		users.put(name, u);
		return u;
	}

	/**
	 * Removes a Player from the users list.
	 * 
	 * @param player the Player to remove
	 * @return the SblockUser for the removed player, if any
	 */
	public SblockUser removeUser(Player player) {
		return users.remove(player.getName());
	}

	/**
	 * Removes a Player from the users list.
	 * 
	 * @param player the name of the Player to remove
	 * @return the SblockUser for the removed player, if any
	 */
	public SblockUser removeUser(String player) {
		return users.remove(player);
	}

	/**
	 * Gets a SblockUserby Player name.
	 * 
	 * @param name the name of the Player to look up
	 * 
	 * @return the SblockUser associated with the given Player, or null if no
	 *         Player with the given name is currently online.
	 */
	public SblockUser getUser(String name) {
		return users.get(name);
	}

	/**
	 * Gets a Collection of SblockUsers currently online.
	 * 
	 * @return the SblockUsers currently online
	 */
	public Collection<SblockUser> getUserlist() {
		return this.users.values();
	}

	/**
	 * Creates teams for display name coloring.
	 */
	private void createTeams() {
		this.board = Bukkit.getScoreboardManager().getNewScoreboard();
		Team team = this.board.registerNewTeam("admin");
		team.setPrefix(ColorDef.RANK_ADMIN.toString());
		team = this.board.registerNewTeam("mod");
		team.setPrefix(ColorDef.RANK_MOD.toString());
		team = this.board.registerNewTeam("helper");
		team.setPrefix(ColorDef.RANK_HELPER.toString());
		team = this.board.registerNewTeam("donator");
		team.setPrefix(ColorDef.RANK_DONATOR.toString());
		team = this.board.registerNewTeam("godtier");
		team.setPrefix(ColorDef.RANK_GODTIER.toString());
		team = this.board.registerNewTeam("hero");
		team.setPrefix(ColorDef.RANK_HERO.toString());
	}

	/**
	 * Add a Player to their group's Team.
	 * 
	 * @param p the Player
	 */
	public void team(Player p) {
		Team team = null;
		if (p.hasPermission("group.horrorterror")) {
			team = this.board.getTeam("admin");
		} else if (p.hasPermission("group.denizen")) {
			team = this.board.getTeam("mod");
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
		p.setScoreboard(board);
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
