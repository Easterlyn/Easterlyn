package co.sblock.Sblock.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.ChatUserManager;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Events.EventModule;
import co.sblock.Sblock.Machines.Type.Machine;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.TowerData;
import co.sblock.Sblock.UserData.UserManager;
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * Collection of all database-related functions.
 * 
 * @author Jikoo, FireNG
 */
public class DBManager {

	/** The <code>DatabaseManager</code> instance. */
	private static DBManager dbm;

	/**
	 * <code>DatabaseManager</code> singleton.
	 * 
	 * @return the <code>DatabaseManager</code> instance
	 */
	public static DBManager getDBM() {
		if (dbm == null)
			dbm = new DBManager();
		return dbm;
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
					+ Sblock.getInstance().getConfig().getString("host") + ":"
					+ Sblock.getInstance().getConfig().getString("port") + "/"
					+ Sblock.getInstance().getConfig().getString("database"),
					Sblock.getInstance().getConfig().getString("username"),
					Sblock.getInstance().getConfig().getString("password"));
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		dbm = null;
		connection = null;
	}

	protected Connection connection() {
		return connection;
	}

	/**
	 * Initiate user data saving for a <code>Player</code> by name.
	 * 
	 * @param name
	 *            the name of the <code>Player</code>
	 */
	public void saveUserData(String name) {
		PlayerData.saveUserData(name);
	}

	/**
	 * Initiate loading of a <code>Player</code>'s stored data.
	 * 
	 * @param name
	 *            the name of the <code>Payer</code> to load data for
	 */
	public ChatUser loadUserData(String name) {
		UserManager.getUserManager().addUser(name);
		ChatUser cUser = ChatUserManager.getUserManager().addUser(name);
		PlayerData.loadUserData(name);
		return cUser;
	}

	/**
	 * Delete specified <code>Player</code>'s data from database.
	 * 
	 * @param name
	 *            the name of the <code>Player</code> whose data is to be deleted
	 */
	public void deleteUser(String name) {
		PlayerData.deleteUser(name);
	}

	/**
	 * Save <code>Channel</code> data to database.
	 * 
	 * @param c
	 *            the <code>Channel</code> to save data for
	 */
	public void saveChannelData(Channel c) {
		ChatChannels.saveChannelData(c);
	}

	/**
	 * Creates and loads all <code>Channel</code>s from saved data.
	 */
	public void loadAllChannelData() {
		ChatChannels.loadAllChannelData();
	}

	/**
	 * Delete a <code>Channel</code> by name.
	 * 
	 * @param channelName
	 *            the name of the <code>Channel</code> to delete
	 */
	public void deleteChannel(String channelName) {
		ChatChannels.deleteChannel(channelName);
	}

	/**
	 * Save <code>Machine</code> data to database.
	 * 
	 * @param m
	 *            the <code>Machine</code> to save data for
	 */
	public void saveMachine(Machine m) {
		Machines.saveMachine(m);
	}

	/**
	 * Delete a specified <code>Machine</code>'s data from database.
	 * 
	 * @param m
	 *            the <code>Machine</code> to delete data of
	 */
	public void deleteMachine(Machine m) {
		Machines.deleteMachine(m);
	}

	/**
	 * Creates and loads all <code>Machine</code>s from saved data.
	 */
	public void loadAllMachines() {
		Machines.loadAllMachines();
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
	public ResultSet makeCustomCall(String MySQLStatement) {
		try {
			return connection.prepareStatement(MySQLStatement).executeQuery();
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
			// Adam redo this utter heap of shitty code
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
		BannedPlayers.addBan(target, reason);
	}

	/**
	 * Remove a ban by name or IP.
	 * 
	 * @param target
	 *            the name or IP to unban
	 */
	public void removeBan(String target) {
		BannedPlayers.loadAndDeleteBans(target);
	}
}
