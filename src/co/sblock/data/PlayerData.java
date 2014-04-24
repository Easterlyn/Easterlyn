package co.sblock.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.chat.channel.Channel;
import co.sblock.users.ChatData;
import co.sblock.users.ProgressionState;
import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.Broadcast;

/**
 * A small helper class containing all methods that access the PlayerData table.
 * <p>
 * The PlayerData table is created by the following call:
 * CREATE TABLE PlayerData (name varchar(255), class varchar(6), aspect varchar(6),
 * mPlanet varchar(5), dPlanet varchar(7), towerNum tinyint, sleepState boolean,
 * currentChannel varchar(16), isMute boolean, nickname varchar(16), channels text, ip varchar(15),
 * timePlayed varchar(255), previousLocation varchar(255), programs varchar(255),
 * uuid varchar(255) UNIQUE KEY, client varchar(255), server varchar(255), progression varchar(16));
 * 
 * @author Jikoo
 */
public class PlayerData {
	/**
	 * Save the data stored for all related parts of a Player.
	 * 
	 * @param cUser the ChatUser to save data for
	 * @param sUser the SblockUser to save data for
	 */
	protected static void saveUserData(UUID userID) {
		User user = UserManager.getUserManager().removeUser(userID);
		if (user == null || !user.isLoaded()) {
			SblockData.getLogger().warning("UUID " + userID.toString()
					+ " does not appear to have userdata loaded, skipping save.");
			return;
		}
			PreparedStatement pst = null;
			try {
				pst = SblockData.getDB().connection()
						.prepareStatement(Call.PLAYER_SAVE.toString());
				pst.setString(1, user.getPlayerName());
				pst.setString(2, user.getPlayerClass().getDisplayName());
				pst.setString(3, user.getAspect().getDisplayName());
				pst.setString(4, user.getMediumPlanet().getShortName());
				pst.setString(5, user.getDreamPlanet().getDisplayName());
				pst.setShort(6, user.getTower());
				pst.setBoolean(7, user.canFly());
				Channel c = ChatData.getCurrent(user);
				pst.setString(8, c != null ? c.getName() : "#" + user.getCurrentRegion().name());
				pst.setBoolean(9, ChatData.isMute(user));
				StringBuilder sb = new StringBuilder();
				for (String s : ChatData.getListening(user)) {
					sb.append(s + ",");
				}
				pst.setString(10, sb.substring(0, sb.length() - 1));
				pst.setString(11, user.getUserIP());
				pst.setString(12, user.getTimePlayed());
				String location = user.getPreviousLocationString();
				if (location == null) {
					user.setPreviousLocation(Bukkit.getWorld("Earth").getSpawnLocation());
					location = user.getPreviousLocationString();
				}
				pst.setString(13, location);
				pst.setString(14, user.getProgramString());
				pst.setString(15, user.getPlayer().getUniqueId().toString());
				pst.setString(16, user.getClient() != null ? user.getClient().toString() : null);
				pst.setString(17, user.getServer() != null ? user.getServer().toString() : null);
				pst.setString(18, user.getProgression().name());
			} catch (Exception e) {
				SblockData.getLogger().err(e);
				return;
			}

			new AsyncCall(pst).schedule();
	}

	/**
	 * Create a PreparedStatement with which to query the SQL database.
	 * 
	 * @param name the name of the user to load data for
	 */
	protected static void loadUserData(UUID userID) {
		try {
			PreparedStatement pst = SblockData.getDB().connection()
					.prepareStatement(Call.PLAYER_LOAD_UUID.toString());
			pst.setString(1, userID.toString());

			new AsyncCall(pst, Call.PLAYER_LOAD_UUID).schedule();
		} catch (SQLException e) {
			SblockData.getLogger().err(e);
		}
	}

	/**
	 * Create a PreparedStatement with which to query the SQL database.
	 * 
	 * @param name the name of the user to delete data for
	 */
	protected static void deleteUser(String name) {
		try {
			PreparedStatement pst = SblockData.getDB().connection()
					.prepareStatement(Call.PLAYER_DELETE.toString());
			pst.setString(1, name);

			new AsyncCall(pst).schedule();
		} catch (SQLException e) {
			SblockData.getLogger().err(e);
		}
	}

	/**
	 * Load a Player's data from a ResultSet.
	 * 
	 * @param rs the ResultSet to load from
	 */
	protected static void loadPlayer(ResultSet rs) {
		try {
			if (rs.next()) {
				User user = UserManager.getUserManager().getUser(UUID.fromString(rs.getString("uuid")));
				if (user == null || user.getPlayer() == null) {
					SblockData.getLogger().warning(rs.getString("uuid") + "'s SblockUser was not instantiated!");
					return;
				}
				user.setAspect(rs.getString("aspect"));
				user.setPlayerClass(rs.getString("class"));
				user.setMediumPlanet(rs.getString("mPlanet"));
				user.setDreamPlanet(rs.getString("dPlanet"));
				short tower = rs.getShort("towerNum");
				if (tower != -1) {
					user.setTower((byte) tower);
				}
				user.updateFlight();
				if (rs.getBoolean("isMute")) {
					ChatData.setMute(user, true);
				}
				if (rs.getString("channels") != null) {
					ChatData.loginAddListening(user, rs.getString("channels").split(","));
				}
				if (rs.getString("previousLocation") != null) {
					user.setPreviousLocationFromString(rs.getString("previousLocation"));
				} else {
					user.setPreviousLocation(Bukkit.getWorld("Earth").getSpawnLocation());
				}
				ChatData.setCurrent(user, rs.getString("currentChannel"));
				if (ChatData.getCurrent(user) == null || ChatData.getListening(user).size() == 0) {
					ChatData.setCurrent(user, "#");
				}
				user.setTimePlayed(rs.getString("timePlayed"));
				user.setPrograms(rs.getString("programs"));
				try {
					user.setClient(rs.getString("client").equals("null") ? null : UUID.fromString(rs.getString("client")));
					user.setServer(rs.getString("server").equals("null") ? null : UUID.fromString(rs.getString("server")));
				} catch (Exception e) {
					// TODO remove after pushing to live and calling UPDATE PlayerData SET client = NULL;UPDATE PlayerData SET server = NULL;
				}
				if (rs.getString("progression") != null) {
					user.setProgression(ProgressionState.valueOf(rs.getString("progression")));
				}
				if (user.getPlayer() != null) { // Player is still logged in, complete load
					user.updateCurrentRegion(user.getPlayerRegion());
					user.setLoaded();
				}
				UserManager.getUserManager().team(user.getPlayer());
			} else {
				String uuid = rs.getStatement().toString().replaceAll("com.*uuid = '(.*)'", "$1");
				Player p = Bukkit.getPlayer(UUID.fromString(uuid));
				if (p == null) {
					// Player is not logged in any more
					return;
				}
				Broadcast.lilHal("It would seem that " + p.getName()
						+ " is joining us for the first time! Please welcome them.");
				p.teleport(new Location(Bukkit.getWorld("Earth"), -3.5, 20, 6.5, 179.99F, 1F));

				User user = UserManager.getUserManager().getUser(p.getUniqueId());
				ChatData.setCurrent(user, "#");
				user.updateCurrentRegion(user.getPlayerRegion());
				user.setLoaded();
			}
		} catch (SQLException e) {
			SblockData.getLogger().err(e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				SblockData.getLogger().err(e);
			}
		}
	}

	/**
	 * Get a SblockUser's name by the IP they last connected with.
	 * 
	 * @param hostAddress the IP to look up
	 * 
	 * @return the name of the SblockUser, "Player" if invalid
	 */
	protected static String getUserFromIP(String hostAddress) {
		PreparedStatement pst = null;
		String name = "Player";
		try {
			pst = SblockData.getDB().connection().prepareStatement("SELECT * FROM PlayerData WHERE ip=?");

			pst.setString(1, hostAddress);

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				name = rs.getString("name");
			}
		} catch (SQLException e) {
			SblockData.getLogger().err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					SblockData.getLogger().err(e);
				}
			}
		}
		if (name != null) {
			return name;
		} else {
			return "Player";
		}
	}


	/**
	 * Create a PreparedStatement from a Player's saved data.
	 * 
	 * @param sender the CommandSender requesting information
	 */
	protected static void startOfflineLookup(CommandSender sender, String name) {
		sender.sendMessage(ChatColor.GREEN + "Initiating offline lookup for " + name);
		try {
			Call c = Call.PLAYER_LOAD_NAME;
			PreparedStatement pst = SblockData.getDB().connection().prepareStatement(c.toString());
			pst.setString(1, name);

			c.setSender(sender);
			new AsyncCall(pst, c).schedule();
		} catch (SQLException e) {
			SblockData.getLogger().err(e);
		}
	}

	/**
	 * Create a /whois from a Player's saved data.
	 * 
	 * @param sender the CommandSender requesting information
	 * @param rs the ResultSet to load from
	 */
	protected static void loadOfflineLookup(CommandSender sender, ResultSet rs) {
		ChatColor sys = ChatColor.DARK_AQUA;
		ChatColor txt = ChatColor.YELLOW;
		String div = sys + ", " + txt;
		StringBuilder sb = new StringBuilder();

		try {
			while (rs.next()) {
				sb.append(sys).append("-----------------------------------------\n").append(txt);
				sb.append(rs.getString("name")).append(div).append(rs.getString("class")).append(" of ")
						.append(rs.getString("aspect")).append('\n');
				sb.append(rs.getString("mPlanet")).append(div).append(rs.getString("dPlanet"))
						.append(div).append("Tower: ").append(rs.getShort("towerNum")).append(div)
						.append("Sleeping: ").append(rs.getBoolean("sleepState")).append('\n');
				sb.append(rs.getBoolean("isMute")).append(div).append(rs.getString("currentChannel"))
						.append(div).append(rs.getString("channels")).append('\n');
				sb.append("Region: OFFLINE").append(div).append("Prev Loc: ")
						.append(rs.getString("previousLocation")).append('\n');
				sb.append(rs.getString("ip")).append('\n');
				sb.append("Time: ").append(rs.getString("timePlayed")).append(div).append("Last login: ")
						.append(new SimpleDateFormat("HH:mm:ss 'on' dd:MM:YY").format(new Date(
								Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("uuid")))
								.getLastPlayed()))).append('\n');
				// TODO getOfflinePlayer cannot be called on main thread
			}
			sb.append(sys).append("-----------------------------------------\n");
			if (!(sender instanceof Player) || ((Player) sender).isOnline()) {
				sender.sendMessage(sb.length() > 0 ? sb.toString()
						: "No player data found for " + rs.getStatement().toString().replaceAll("com.*name='(.*)'", "$1"));
			}
		} catch (SQLException e) {
			SblockData.getLogger().err(e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				SblockData.getLogger().err(e);
			}
		}
	}
}
