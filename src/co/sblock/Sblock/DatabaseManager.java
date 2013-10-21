package co.sblock.Sblock;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import co.sblock.Sblock.Chat.ChatModule;
import co.sblock.Sblock.Chat2.ChatUser;
import co.sblock.Sblock.Chat2.ChatUserManager;
import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Chat.Channel.ChannelType;
import co.sblock.Sblock.Events.EventModule;
import co.sblock.Sblock.Machines.MachineManager;
import co.sblock.Sblock.Machines.MachineModule;
import co.sblock.Sblock.Machines.Type.Machine;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.TowerData;
import co.sblock.Sblock.UserData.UserManager;
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * Collection of all database-related functions
 * 
 * @author Jikoo, FireNG
 */
public class DatabaseManager {

	/** The <code>DatabaseManager</code> instance. */
	private static DatabaseManager dbm;

	/** The <code>Sblock</code> instance. */
	private Sblock plugin;

	/**
	 * Method for obtaining <code>DatabaseManager</code> instance.
	 * 
	 * @return the <code>DatabaseManager</code> instance
	 */
	public static DatabaseManager getDatabaseManager() {
		if (dbm == null)
			dbm = new DatabaseManager();
		return dbm;
	}

	/**
	 * Constructor for <code>DatabaseManager</code>.
	 */
	public DatabaseManager() {
		plugin = Sblock.getInstance();
	}

	/** The SQL <code>Connection</code> used by the <code>DatabaseManager</code>. */
	private Connection connection;

	/**
	 * Establish connection to database and create <code>DatabaseManager</code>
	 * instance.
	 * 
	 * @return true if enabled successfully
	 */
	public boolean enable() {
		Sblogger.info("SblockDatabase", "Connecting to database.");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://"
					+ plugin.getConfig().getString("host") + ":"
					+ plugin.getConfig().getString("port") + "/"
					+ plugin.getConfig().getString("database"), plugin
					.getConfig().getString("username"), plugin.getConfig()
					.getString("password"));
			Sblogger.info("SblockDatabase", "Connection established.");
		} catch (ClassNotFoundException e) {
			Sblogger.severe("Database", "The database driver was not found."
							+ " Plugin functionality will be limited.");
			return false;
		} catch (SQLException e) {
			Sblogger.severe("Database", "An error occurred while connecting to"
					+ " the database. Plugin functionality will be limited.");
			e.printStackTrace();
			return false;
		}

		Sblogger.info("SblockDatabase", "Database enabled");
		return true;
	}

	/**
	 * Close <code>Connection</code> and set instance to null.
	 */
	public void disable() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		dbm = null;
		connection = null;
	}


	/**
	 * Save the data stored for a <code>SblockUser</code> user.
	 * 
	 * @param user
	 *            the <code>SblockUser</code> to save data for
	 */
	public void saveUserData(String name) {
		PreparedStatement pst = null;
		try {
			ChatUser cUser = ChatUserManager.getUserManager().removeUser(name);
			SblockUser sUser = UserManager.getUserManager().removeUser(name);
			pst = connection.prepareStatement("INSERT INTO PlayerData(name, class, aspect, "
							+ "mPlanet, dPlanet, towerNum, sleepState, currentChannel, isMute, "
							+ "nickname, channels, ip, timePlayed, previousLocation, programs, uhc) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
							+ "ON DUPLICATE KEY UPDATE "
							+ "class=VALUES(class), aspect=VALUES(aspect), "
							+ "mPlanet=VALUES(mPlanet), dPlanet=VALUES(dPlanet), "
							+ "towerNum=VALUES(towerNum), sleepState=VALUES(sleepState), "
							+ "currentChannel=VALUES(currentChannel), "
							+ "isMute=VALUES(isMute), nickname=VALUES(nickname), "
							+ "channels=VALUES(channels), ip=VALUES(ip), "
							+ "timePlayed=VALUES(timePlayed), "
							+ "previousLocation=VALUES(previousLocation), "
							+ "programs=VALUES(programs), uhc=VALUES(uhc)");
			pst.setString(1, name);
			pst.setString(2, sUser.getClassType().getDisplayName());
			pst.setString(3, sUser.getAspect().getDisplayName());
			pst.setString(4, sUser.getMPlanet().getShortName());
			pst.setString(5, sUser.getDPlanet().getDisplayName());
			pst.setShort(6, sUser.getTower());
			pst.setBoolean(7, sUser.isSleeping());
			pst.setString(8, cUser.getCurrent().getName());
			pst.setBoolean(9, cUser.isMute());
			pst.setString(10, cUser.getNick());
			StringBuilder sb = new StringBuilder();
			for (String s : cUser.getListening()) {
				sb.append(s + ",");
			}
			pst.setString(11, sb.substring(0, sb.length() - 1));
			pst.setString(12, sUser.getUserIP());
			sUser.updateTimePlayed();
			pst.setString(13, sUser.getTimePlayed());
			try {
				pst.setString(14, sUser.getPreviousLocationString());
			} catch (NullPointerException e) {
				sUser.setPreviousLocation(Bukkit.getWorld("Earth").getSpawnLocation());
				pst.setString(14, sUser.getPreviousLocationString());
			}
			pst.setString(15, sUser.getProgramString());
			pst.setByte(16, sUser.getUHCMode());

			pst.executeUpdate();

			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
	}

	/**
	 * Load userdata into a <code>SblockUser</code>.
	 * 
	 * @param user
	 *            the <code>SblockUser</code> to load data for
	 */
	public ChatUser loadUserData(String name) {
		PreparedStatement pst = null;
		ChatUser cUser = null;
		SblockUser sUser = UserManager.getUserManager().addUser(name);
		cUser = ChatUserManager.getUserManager().addUser(name);
		try {
			pst = connection.prepareStatement("SELECT * FROM PlayerData WHERE name=?");

			pst.setString(1, name);

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				sUser.setAspect(rs.getString("aspect"));
				sUser.setPlayerClass(rs.getString("class"));
				sUser.setMediumPlanet(rs.getString("mPlanet"));
				sUser.setDreamPlanet(rs.getString("dPlanet"));
				short tower = rs.getShort("towerNum");
				if (tower != -1) {
					sUser.setTower((byte) tower);
				}
				sUser.setIsSleeping(rs.getBoolean("sleepState"));
				if (rs.getBoolean("isMute")) {
					cUser.setMute(true);
				}
				cUser.setNick(rs.getString("nickname") != null ? rs.getString("nickname") : cUser.getNick());
				if (rs.getString("channels") != null) {
					String[] channels = rs.getString("channels").split(",");
					for (int i = 0; i < channels.length; i++) {
						cUser.syncJoinChannel(channels[i]);
					}
				}
				if (rs.getString("previousLocation") != null) {
					sUser.setPreviousLocationFromString(rs.getString("previousLocation"));
				} else {
					sUser.setPreviousLocation(Bukkit.getWorld("Earth").getSpawnLocation());
				}
				cUser.syncSetCurrentChannel(rs.getString("currentChannel"));
				sUser.setTimePlayed(rs.getString("timePlayed"));
				sUser.setPrograms(rs.getString("programs"));
				sUser.setUHCMode(rs.getByte("uhc"));
			} else {
				Sblogger.info("SblockDatabase", sUser.getPlayerName() + " is new to the server!");
			}
		} catch (Exception e) {
			Sblogger.criticalErr(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
		return cUser;
	}

	/**
	 * Delete specified <code>SblockUser</code>'s data from database.
	 * 
	 * @param user
	 *            the <code>SblockUser</code> to delete data of
	 */
	public void deleteUser(SblockUser user) {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement("DELETE FROM PlayerData WHERE name = ?");
			pst.setString(1, user.getPlayerName());

			pst.executeUpdate();
		} catch (SQLException e) {
			Sblogger.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
	}

	/**
	 * Save <code>Channel</code> data to database.
	 * 
	 * @param c
	 *            the <code>Channel</code> to save data for
	 */
	public void saveChannelData(Channel c) {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement(
					"INSERT INTO ChatChannels(name, channelType, access, owner, modList, "
							+ "banList, approvedList) VALUES (?, ?, ?, ?, ?, ?, ?) "
							+ "ON DUPLICATE KEY UPDATE channelType=VALUES(channelType), "
							+ "access=VALUES(access),owner=VALUES(owner),  "
							+ "modList=VALUES(modList), banList=VALUES(banList), "
							+ "approvedList=VALUES(approvedList)");

			pst.setString(1, c.getName());
			pst.setString(2, c.getType().name());
			pst.setString(3, c.getAccess().name());
			pst.setString(4, c.getOwner());
			StringBuilder sb = new StringBuilder();
			for (String s : c.getModList()) {
				sb.append(s + ",");
			}
			if (sb.length() > 0) {
				pst.setString(5, sb.substring(0, sb.length() - 1));
			} else {
				pst.setString(5, null);
			}
			sb = new StringBuilder();
			for (String s : c.getBanList()) {
				sb.append(s + ",");
			}
			if (sb.length() > 0) {
				pst.setString(6, sb.substring(0, sb.length() - 1));
			} else {
				pst.setString(6, null);
			}
			sb = new StringBuilder();
			for (String s : c.getApprovedUsers()) {
				sb.append(s + ",");
			}
			if (sb.length() > 0) {
				pst.setString(7, sb.substring(0, sb.length() - 1));
			} else {
				pst.setString(7, null);
			}

			pst.executeUpdate();
		} catch (SQLException e) {
			Sblogger.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
	}

	/**
	 * Creates and loads all <code>Channel</code>s from saved data.
	 */
	public void loadAllChannelData() {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement("SELECT * FROM ChatChannels");

			ResultSet rs = pst.executeQuery();

			ChannelManager cm = ChatModule.getChatModule().getChannelManager();

			while (rs.next()) {
				cm.createNewChannel(rs.getString("name"),
						AccessLevel.valueOf(rs.getString("access")), rs.getString("owner"),
						ChannelType.valueOf(rs.getString("channelType")));
				Channel c = ChatModule.getChatModule().getChannelManager()
						.getChannel(rs.getString("name"));
				String list = rs.getString("modList");
				if (list != null) {
					String[] modList = list.split(",");
					for (int i = 0; i < modList.length; i++) {
						c.loadMod(modList[i]);
					}
				}
				list = rs.getString("banList");
				if (list != null) {
					String[] banList = list.split(",");
					for (int i = 0; i < banList.length; i++) {
						c.loadBan(banList[i]);
					}
				}
				list = rs.getString("approvedList");
				if (list != null) {
					String[] approvedList = list.split(",");
					for (int i = 0; i < approvedList.length; i++) {
						c.loadApproval(approvedList[i]);
					}
				}
			}
		} catch (SQLException e) {
			Sblogger.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
	}

	/**
	 * Delete a <code>Channel</code> by name.
	 * 
	 * @param channelName
	 *            the name of the <code>Channel</code> to delete
	 */
	public void deleteChannel(String channelName) {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement("DELETE FROM ChatChannels WHERE name = ?");
			pst.setString(1, channelName);

			pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
	}

	/**
	 * Save <code>Machine</code> data to database.
	 * 
	 * @param m
	 *            the <code>Machine</code> to save data for
	 */
	public void saveMachine(Machine m) {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement(
					"INSERT INTO Machines(location, type, data) "
							+ "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE "
							+ "type=VALUES(type), data=VALUES(data)");

			pst.setString(1, m.getLocationString());
			pst.setString(2, m.getType().getAbbreviation());
			pst.setString(3, m.getData());

			pst.executeUpdate();
		} catch (SQLException e) {
			Sblogger.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
	}

	/**
	 * Delete a specified <code>Machine</code>'s data from database.
	 * 
	 * @param m
	 *            the <code>Machine</code> to delete data of
	 */
	public void deleteMachine(Machine m) {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement(
					"DELETE FROM Machines WHERE location = ?");
			pst.setString(1, m.getLocationString());

			pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
	}

	/**
	 * Creates and loads all <code>Machine</code>s from saved data.
	 */
	public void loadAllMachines() {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement("SELECT * FROM Machines");

			ResultSet rs = pst.executeQuery();
			MachineManager mm = MachineModule.getInstance().getManager();

			while (rs.next()) {
				mm.loadMachine(rs.getString("location"), rs.getString("type"), rs.getString("data"));
			}
		} catch (SQLException e) {
			Sblogger.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
	}

	/**
	 * @deprecated Make a custom call to the database. For testing purposes
	 *             only! Make a new method for new features.
	 * @param MySQLStatement
	 *            the call to make
	 * @param resultExpected
	 *            true if a <code>ResultSet</code> is expected
	 * @return the <code>ResultSet</code> generated, if any.
	 */
	public ResultSet makeCustomCall(String MySQLStatement, boolean resultExpected) {
		try {
			PreparedStatement pst = connection.prepareStatement(MySQLStatement);
			if (resultExpected) {
				ResultSet rs = pst.executeQuery();
				pst.close();
				return rs; // TODO find out if stream needs to be open for this
			} else {
				pst.executeUpdate();
				pst.close();
				return null;
			}
		} catch (SQLException e) {
			Sblogger.err(e);
			return null;
		}
	}

	/**
	 * Fills out <code>TowerData</code> from saved data.
	 */
	public void loadTowerData() {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement("SELECT * FROM TowerLocs");

			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				String towerID = rs.getString("towerID");
				String location = rs.getString("location");
				if (towerID != null && location != null) {
					EventModule.getEventModule().getTowerData().add(towerID, location);
				}
			}
		} catch (SQLException e) {
			Sblogger.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
		
	}

	/**
	 * Save all <code>TowerData</code>.
	 * @param towers the <code>TowerData</code> to save
	 */
	public void saveTowerData(TowerData towers) {
		PreparedStatement pst = null;
		try {
			for (byte i = 0; i < 8; i++) {
				pst = connection.prepareStatement(
						"INSERT INTO TowerLocs(towerID, location) "
								+ "VALUES (?, ?)"
								+ "ON DUPLICATE KEY UPDATE "
								+ "location=VALUES(location)");
	
				pst.setString(1, "Derse" + i);
				pst.setString(2, towers.getLocString("Derse", i));
	
				pst.executeUpdate();

				pst.close();

				pst = connection.prepareStatement(
						"INSERT INTO TowerLocs(towerID, location) "
								+ "VALUES (?, ?)"
								+ "ON DUPLICATE KEY UPDATE "
								+ "location=VALUES(location)");
	
					pst.setString(1, "Prospit" + i);
					pst.setString(2, towers.getLocString("Prospit", i));
	
				pst.executeUpdate();
				pst.close();
			}

		} catch (SQLException e) {
			Sblogger.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
	}

	/**
	 * Get a <code>SblockUser</code>'s name by the IP they last connected with.
	 * 
	 * @param hostAddress
	 *            the IP to look up
	 * @return the name of the <code>SblockUser</code>, "Player" if invalid
	 */
	public String getUserFromIP(String hostAddress) {
		PreparedStatement pst = null;
		String name = "Player";
		try {
			pst = connection.prepareStatement("SELECT * FROM PlayerData WHERE ip=?");

			pst.setString(1, hostAddress);

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				name = rs.getString("name");
			}
		} catch (SQLException e) {
			Sblogger.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
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
	 * Get the reason a <code>SblockUser</code> was banned.
	 * 
	 * @param name
	 *            the name of the banned <code>SblockUser</code>
	 * @param ip
	 *            the IP of the banned <code>SblockUser</code>
	 * @return the ban reason
	 */
	public String getBanReason(String name, String ip) {
		PreparedStatement pst = null;
		String ban = null;
		try {
			pst = connection.prepareStatement(
					"SELECT * FROM BannedPlayers WHERE name=? OR ip =?");

			pst.setString(1, name);
			pst.setString(2, ip);

			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				ban = rs.getString("reason");
				if (name.equals(rs.getString("name"))) {
					break;
				}
			}
		} catch (SQLException e) {
			Sblogger.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
		return ban;
	}

	/**
	 * Add a ban and reason to a <code>SblockUser</code>.
	 * 
	 * @param target
	 *            the <code>SblockUser</code> to add a ban for
	 * @param reason
	 *            the reason the <code>SblockUser</code> was banned
	 */
	public void addBan(SblockUser target, String reason) {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement(
					"INSERT INTO BannedPlayers(name, ip, banDate, reason) "
					+ "VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
					+ "name=VALUES(name), ip=VALUES(ip), "
					+ "banDate=VALUES(banDate), reason=VALUES(reason)");

			pst.setString(1, target.getPlayerName());
			pst.setString(2, target.getUserIP());
			pst.setDate(3, new Date(new java.util.Date().getTime()));
			pst.setString(4, reason);

			pst.executeUpdate();
		} catch (SQLException e) {
			Sblogger.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
	}

	/**
	 * Remove a ban by name or IP.
	 * 
	 * @param target
	 *            the name or IP to unban
	 */
	public void removeBan(String target) {
		PreparedStatement pst = null;

		try {
			pst = connection.prepareStatement(
					"SELECT * FROM BannedPlayers WHERE name=? OR ip =?");

			pst.setString(1, target);
			pst.setString(2, target);

			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				try {
					Bukkit.unbanIP(rs.getString("ip"));
				} catch (NullPointerException e) {
					// IP not saved
				}
				try {
					Bukkit.getOfflinePlayer(rs.getString("name")).setBanned(false);
				} catch (NullPointerException e) {
					// Name not saved
				}
			}

			pst.close();

			pst = connection.prepareStatement(
					"DELETE FROM BannedPlayers WHERE name=? OR ip=?");

			pst.setString(1, target);
			pst.setString(2, target);

			pst.executeUpdate();

		} catch (SQLException e) {
			Sblogger.severe("SblockDatabase", "Error removing ban for " + target
					+ "! Please unban and unIPban manually. MySQL call:"
					+ "\nDELETE * FROM BannedPlayers WHERE name="
					+ target +" OR ip=" + target + ";");
			Sblogger.err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					Sblogger.err(e);
				}
			}
		}
	}
}
