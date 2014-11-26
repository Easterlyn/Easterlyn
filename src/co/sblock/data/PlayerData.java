package co.sblock.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.chat.channel.Channel;
import co.sblock.users.ProgressionState;
import co.sblock.users.User;
import co.sblock.users.UserManager;

/**
 * A small helper class containing all methods that access the PlayerData table.
 * <p>
 * The PlayerData table is created by the following call:
 * CREATE TABLE PlayerData (name varchar(255), class varchar(6), aspect varchar(6), mPlanet
 * varchar(5), dPlanet varchar(11), sleepState boolean, currentChannel varchar(16), isMute boolean,
 * nickname varchar(16), channels text, ip varchar(15), timePlayed varchar(255), previousLocation
 * varchar(255), programs varchar(255), uuid varchar(255) UNIQUE KEY, client varchar(255), server
 * varchar(255), progression varchar(16));
 * 
 * @author Jikoo
 */
public class PlayerData {
	/**
	 * Save the data stored for all related parts of a Player.
	 * 
	 * @param userID the Player UUID to save data for
	 */
	public static void saveUserData(UUID userID) {
		User user = UserManager.getUser(userID);
		if (user == null || !user.isLoaded()) {
			SblockData.getDB().getLogger().warning("UUID " + userID.toString()
					+ " does not appear to have userdata loaded, skipping save.");
			return;
		}
		PreparedStatement pst = null;
		try {
			pst = SblockData.getDB().connection()
					.prepareStatement(Call.PLAYER_SAVE.toString());
			pst.setString(1, user.getPlayerName());
			pst.setString(2, user.getUserClass().getDisplayName());
			pst.setString(3, user.getAspect().getDisplayName());
			pst.setString(4, user.getMediumPlanet().name());
			pst.setString(5, user.getDreamPlanet().name());
			pst.setBoolean(6, user.canFly());
			Channel c = user.getCurrent();
			pst.setString(7, c != null ? c.getName() : "#" + user.getCurrentRegion().name());
			pst.setBoolean(8, user.isMute());
			StringBuilder sb = new StringBuilder();
			for (String s : user.getListening()) {
				sb.append(s + ",");
			}
			pst.setString(9, sb.substring(0, sb.length() - 1));
			pst.setString(10, user.getUserIP());
			pst.setString(11, user.getTimePlayed());
			String location = user.getPreviousLocationString();
			if (location == null) {
				user.setPreviousLocation(Bukkit.getWorld("Earth").getSpawnLocation());
				location = user.getPreviousLocationString();
			}
			pst.setString(12, location);
			pst.setString(13, user.getProgramString());
			pst.setString(14, user.getPlayer().getUniqueId().toString());
			pst.setString(15, user.getClient() != null ? user.getClient().toString() : null);
			pst.setString(16, user.getServer() != null ? user.getServer().toString() : null);
			pst.setString(17, user.getProgression().name());

			if (Sblock.getInstance().isEnabled()) {
				new AsyncCall(pst).schedule();
			} else {
				// Plugin is disabling, tasks cannot be scheduled.
				pst.executeUpdate();
			}
		} catch (Exception e) {
			SblockData.getDB().getLogger().err(e);
			return;
		}
	}

	/**
	 * Create a PreparedStatement with which to query the SQL database.
	 * 
	 * @param userID the UUID of the user to load data for
	 */
	public static void loadUserData(UUID userID) {
		try {
			PreparedStatement pst = SblockData.getDB().connection()
					.prepareStatement(Call.PLAYER_LOAD_UUID.toString());
			pst.setString(1, userID.toString());

			new AsyncCall(pst, Call.PLAYER_LOAD_UUID).schedule();
		} catch (SQLException e) {
			SblockData.getDB().getLogger().err(e);
		}
	}

	/**
	 * Create a PreparedStatement with which to query the SQL database.
	 * 
	 * @param userID the UUID of the user to delete data for
	 */
	public static void deleteUser(UUID userID) {
		try {
			PreparedStatement pst = SblockData.getDB().connection()
					.prepareStatement(Call.PLAYER_DELETE.toString());
			pst.setString(1, userID.toString());

			new AsyncCall(pst).schedule();
		} catch (SQLException e) {
			SblockData.getDB().getLogger().err(e);
		}
	}

	/**
	 * Load a Player's data from a ResultSet.
	 * 
	 * @param rs the ResultSet to load from
	 */
	public static void loadPlayer(ResultSet rs) {
		try {
			if (rs.next()) {
				User user = UserManager.addUser(UUID.fromString(rs.getString("uuid")));
				if (user == null || user.getPlayer() == null) {
					UserManager.unloadUser(user.getUUID());
					return;
				}
				user.setAspect(rs.getString("aspect"));
				user.setPlayerClass(rs.getString("class"));
				user.setMediumPlanet(rs.getString("mPlanet"));
				user.setDreamPlanet(rs.getString("dPlanet"));
				user.updateFlight();
				if (rs.getBoolean("isMute")) {
					user.setMute(true);
				}
				if (rs.getString("channels") != null) {
					user.loginAddListening(rs.getString("channels").split(","));
				}
				if (rs.getString("previousLocation") != null) {
					user.setPreviousLocationFromString(rs.getString("previousLocation"));
				} else {
					user.setPreviousLocation(Bukkit.getWorld("Earth").getSpawnLocation());
				}
				if (rs.getString("currentChannel") != null) {
					user.setCurrent(rs.getString("currentChannel"));
				} else {
					user.setCurrent("#");
				}
				user.setPrograms(rs.getString("programs"));
				if (rs.getString("client") != null) {
					user.setClient(UUID.fromString(rs.getString("client")));
				}
				if (rs.getString("server") != null) {
					user.setServer(UUID.fromString(rs.getString("server")));
				}
				if (rs.getString("progression") != null) {
					user.setProgression(ProgressionState.valueOf(rs.getString("progression")));
				}
				user.updateCurrentRegion(user.getPlayerRegion());
				user.setLoaded();
			} else {
				String uuid = rs.getStatement().toString().replaceAll("com.*uuid = '(.*)'", "$1");
				Player p = Bukkit.getPlayer(UUID.fromString(uuid));
				UserManager.doFirstLogin(p);
			}
		} catch (SQLException e) {
			SblockData.getDB().getLogger().err(e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				SblockData.getDB().getLogger().err(e);
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
	public static String getUserFromIP(String hostAddress) {
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
			SblockData.getDB().getLogger().err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					SblockData.getDB().getLogger().err(e);
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
	public static void startOfflineLookup(CommandSender sender, String name) {
		sender.sendMessage(ChatColor.GREEN + "Initiating offline lookup for " + name);
		try {
			Call c = Call.PLAYER_LOAD_NAME;
			PreparedStatement pst = SblockData.getDB().connection().prepareStatement(c.toString());
			pst.setString(1, name);

			c.setSender(sender);
			new AsyncCall(pst, c).schedule();
		} catch (SQLException e) {
			SblockData.getDB().getLogger().err(e);
		}
	}

	/**
	 * Create a /whois from a Player's saved data.
	 * 
	 * @param sender the CommandSender requesting information
	 * @param rs the ResultSet to load from
	 */
	public static void loadOfflineLookup(CommandSender sender, ResultSet rs) {
		ChatColor sys = ChatColor.DARK_AQUA;
		ChatColor txt = ChatColor.YELLOW;
		String div = sys + ", " + txt;
		StringBuilder sb = new StringBuilder();

		try {
			while (rs.next()) {
				sb.append(sys).append("-----------------------------------------\n").append(txt);
				sb.append(rs.getString("name")).append(div).append(rs.getString("class")).append(" of ")
						.append(rs.getString("aspect")).append('\n');
				sb.append(rs.getString("mPlanet")).append(div).append(rs.getString("dPlanet")).append(div)
						.append("Sleeping: ").append(rs.getBoolean("sleepState")).append('\n');
				sb.append(rs.getBoolean("isMute")).append(div).append(rs.getString("currentChannel"))
						.append(div).append(rs.getString("channels")).append('\n');
				sb.append("Region: OFFLINE").append(div).append("Prev Loc: ")
						.append(rs.getString("previousLocation")).append('\n');
				sb.append(rs.getString("ip")).append('\n');
				sb.append("Time: ").append(rs.getString("timePlayed")).append(div).append("Last login: ")
						.append(new SimpleDateFormat("HH:mm:ss 'on' dd/MM/YY").format(new Date(
								Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("uuid")))
								.getLastPlayed()))).append('\n');
			}
			sb.append(sys).append("-----------------------------------------");
			if (!(sender instanceof Player) || ((Player) sender).isOnline()) {
				sender.sendMessage(sb.length() > 0 ? sb.toString()
						: "No player data found for " + rs.getStatement().toString().replaceAll("com.*name='(.*)'", "$1"));
			}
		} catch (SQLException e) {
			SblockData.getDB().getLogger().err(e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				SblockData.getDB().getLogger().err(e);
			}
		}
	}
}
