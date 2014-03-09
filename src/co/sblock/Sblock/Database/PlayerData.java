package co.sblock.Sblock.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.ChatUserManager;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;
import co.sblock.Sblock.Utilities.Log;
import co.sblock.Sblock.Utilities.Broadcast;

/**
 * A small helper class containing all methods that access the PlayerData table.
 * <p>
 * The PlayerData table is created by the following call:
 * CREATE TABLE PlayerData (name varchar(16) UNIQUE KEY, class varchar(6),
 * aspect varchar(6), mPlanet varchar(5), dPlanet varchar(7), towerNum tinyint,
 * sleepState boolean, currentChannel varchar(16), isMute boolean, nickname varchar(16),
 * channels text, ip varchar(15), timePlayed varchar(20), previousLocation varchar(40),
 * programs varchar(31), uhc tinyint, client varchar(16), server varchar(16), isServer boolean);
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
	protected static void saveUserData(String name) {
		ChatUser cUser = ChatUserManager.getUserManager().removeUser(name);
		SblockUser sUser = UserManager.getUserManager().removeUser(name);
		if (cUser == null || sUser == null || !sUser.isLoaded()) {
			SblockData.getDB().getLogger().warning("Player " + name
					+ " does not appear to have userdata loaded, skipping save.");
			return;
		}
			PreparedStatement pst = null;
			try {
				pst = SblockData.getDB().connection()
						.prepareStatement(Call.PLAYER_SAVE.toString());
				pst.setString(1, sUser.getPlayerName());
				pst.setString(2, sUser.getClassType().getDisplayName());
				pst.setString(3, sUser.getAspect().getDisplayName());
				pst.setString(4, sUser.getMPlanet().getShortName());
				pst.setString(5, sUser.getDPlanet().getDisplayName());
				pst.setShort(6, sUser.getTower());
				pst.setBoolean(7, sUser.isSleeping());
				try {
					pst.setString(8, cUser.getCurrent().getName());
				} catch (NullPointerException e1) {
					pst.setString(8, "#" + cUser.getCurrentRegion().name());
				}
				pst.setBoolean(9, cUser.isMute());
				StringBuilder sb = new StringBuilder();
				for (String s : cUser.getListening()) {
					sb.append(s + ",");
				}
				pst.setString(10, sb.substring(0, sb.length() - 1));
				pst.setString(11, sUser.getUserIP());
				pst.setString(12, sUser.getTimePlayed());
				try {
					pst.setString(13, sUser.getPreviousLocationString());
				} catch (NullPointerException e) {
					sUser.setPreviousLocation(Bukkit.getWorld("Earth").getSpawnLocation());
					pst.setString(13, sUser.getPreviousLocationString());
				}
				pst.setString(14, sUser.getProgramString());
				pst.setByte(15, sUser.getUHCMode());
			} catch (Exception e) {
				Log.err(e);
				return;
			}

			new AsyncCall(pst).schedule();
	}

	/**
	 * Create a PreparedStatement with which to query the SQL database.
	 * 
	 * @param name the name of the user to load data for
	 */
	protected static void loadUserData(String name) {
		try {
			PreparedStatement pst = SblockData.getDB().connection()
					.prepareStatement(Call.PLAYER_LOAD.toString());
			pst.setString(1, name);

			new AsyncCall(pst, Call.PLAYER_LOAD).schedule();
		} catch (SQLException e) {
			Log.err(e);
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
			Log.err(e);
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
				String name = rs.getString("name");
				SblockUser sUser = UserManager.getUserManager().getUser(name);
				ChatUser cUser = ChatUserManager.getUserManager().getUser(name);
				if (sUser == null || cUser == null || !sUser.getPlayer().isOnline()) {
					SblockData.getDB().getLogger().warning(name + "'s SblockUser was not instantiated!");
					return;
				}
				sUser.setAspect(rs.getString("aspect"));
				sUser.setPlayerClass(rs.getString("class"));
				sUser.setMediumPlanet(rs.getString("mPlanet"));
				sUser.setDreamPlanet(rs.getString("dPlanet"));
				short tower = rs.getShort("towerNum");
				if (tower != -1) {
					sUser.setTower((byte) tower);
				}
				sUser.updateSleepstate();
				if (rs.getBoolean("isMute")) {
					cUser.setMute(true);
				}
				if (rs.getString("channels") != null) {
					String[] channels = rs.getString("channels").split(",");
					for (int i = 0; i < channels.length; i++) {
						if (!cUser.isListening(channels[i])) {
							cUser.addListening(channels[i]);
						}
					}
				}
				if (rs.getString("previousLocation") != null) {
					sUser.setPreviousLocationFromString(rs.getString("previousLocation"));
				} else {
					sUser.setPreviousLocation(Bukkit.getWorld("Earth").getSpawnLocation());
				}
				cUser.setCurrent(rs.getString("currentChannel"));
				sUser.setTimePlayed(rs.getString("timePlayed"));
				sUser.setPrograms(rs.getString("programs"));
				sUser.setUHCMode(rs.getByte("uhc"));
				sUser.setLoaded();
				UserManager.getUserManager().team(sUser.getPlayer());
			} else {
				String name = rs.getStatement().toString().replaceAll("com.*name='(.*)'", "$1");
				Broadcast.lilHal("It would seem that "
				+ name + " is joining us for the first time! Please welcome them.");
				try {
					SblockUser.getUser(name).setLoaded();
				} catch (NullPointerException e) {
					SblockData.getDB().getLogger().warning(name + "'s SblockUser was not instantiated!");
				}
			}
		} catch (SQLException e) {
			Log.err(e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				Log.err(e);
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
			Log.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Log.err(e);
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
	 * @param rs the ResultSet to load from
	 */
	protected static void startOfflineLookup(CommandSender sender, String name) {
		if (Bukkit.getOfflinePlayer(name).hasPlayedBefore()) {
			sender.sendMessage(ChatColor.GREEN + "Initiating offline lookup for " + name);
		} else {
			sender.sendMessage("Unknown player. Be sure to capitalize properly!");
			return;
		}
		try {
			Call c = Call.PLAYER_LOOKUP;
			PreparedStatement pst = SblockData.getDB().connection().prepareStatement(c.toString());
			pst.setString(1, name);

			c.setSender(sender);
			new AsyncCall(pst, c).schedule();
		} catch (SQLException e) {
			Log.err(e);
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
		String s = "Error retrieving player data.";

		try {
			if (rs.first()) {
				s = sys + "-----------------------------------------\n" + 
						txt + rs.getString("name") + div + rs.getString("class") + " of " + rs.getString("aspect") + "\n"
						+  rs.getString("mPlanet") + div + rs.getString("dPlanet") + div + "Tower: "
						+ rs.getShort("towerNum") + div + "Sleeping: " + rs.getBoolean("sleepState") + "\n"
						+ rs.getBoolean("isMute") + div + rs.getString("currentChannel") + div + rs.getString("channels") + "\n"
						+ "Region: OFFLINE" + div + "Prev Loc: " + rs.getString("previousLocation") + "\n"
						+ rs.getString("ip") + "\n"
						+ "Time: " + rs.getString("timePlayed") + div + "Last login (long): "
						+ Bukkit.getOfflinePlayer(rs.getString("name")).getLastPlayed() + "\n"
						+ sys + "-----------------------------------------";
			}
		} catch (SQLException e) {
			Log.err(e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				Log.err(e);
			}
		}
		if (!(sender instanceof Player) || ((Player) sender).isOnline()) {
			sender.sendMessage(s);
		}
	}
}
