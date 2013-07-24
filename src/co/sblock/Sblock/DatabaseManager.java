package co.sblock.Sblock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import co.sblock.Sblock.PlayerData.SblockPlayer;

/**
 * Collection of all database-related functions
 * 
 * @author Jikoo
 * 
 */
public class DatabaseManager {

	private static DatabaseManager dbm;
	Sblock plugin;

	public static DatabaseManager getDatabaseManager() {
		if (dbm == null)
			dbm = new DatabaseManager();
		return dbm;
	}

	public DatabaseManager() {
		plugin = Sblock.getInstance();
	}

	private Connection connection;

	public boolean enable() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://"
					+ plugin.getConfig().getString("host") + ":"
					+ plugin.getConfig().getString("port") + "/"
					+ plugin.getConfig().getString("database"),
					plugin.getConfig().getString("username"),
					plugin.getConfig().getString("password"));
		} catch (ClassNotFoundException e) {
			// if we can't connect to the database, we're pretty much done here.
			plugin.getLogger().severe("The database driver was not found. " +
					"Plugin functionality will be limited.");
			return false;
		} catch (SQLException e) {
			plugin.getLogger().severe("An error occurred while connecting to the database. " +
					"Plugin functionality will be limited.");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void disable() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		dbm = null;
		connection = null;
	}

	public void firstPlayerDataSave(SblockPlayer user) {
		try {
			PreparedStatement pst = connection.prepareStatement(
					"INSERT INTO PlayerData(playerName, class, aspect, mplanet, " +
					"dplanet, towernum, sleepstate, currentChannel, isMute, " +
					"nickname, channels, ip, LastLogin, timePlayed) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			pst.setString(1, user.getPlayerName());
			pst.setString(2, user.getClassType().getDisplayName());
			pst.setString(3, user.getAspect().getDisplayName());
			pst.setString(4, user.getMPlanet().getShortName());
			pst.setString(5, user.getDPlanet().getDisplayName());
			pst.setShort(6, user.getTower());
			pst.setBoolean(7, user.isSleeping());
//			pst.setString(8, user.getCurrent().getName()); // TODO currentChannel
//			pst.setString(9, user.isMute());
//			pst.setString(10, user.getNick());
//			pst.setArray(11, user.getListening()); // TODO Keiko, may need to be a List, not Set. Not sure.
//			pst.setString(12, user.getUserIP());
//			pst.setString(13, null); // TODO lastLogin     Keiko, to String, but need methods.
//			pst.setString(14, null); // TODO timePlayed
			
			pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updatePlayerData(SblockPlayer user) {
		// TODO just copy the meat from firstPlayerDataSave
		// AFTER finalization >.> majority may change
	}

	public void loadPlayerData(SblockPlayer user) {
		// TODO find out exact order from Keiko
		// Correction: Get Keiko to fill in all actual MySQL names
		// and datatypes, can't seem to find the last puush of it.
		try {
			PreparedStatement pst = connection.prepareStatement(
					"SELECT * FROM PlayerData WHERE name=?");
			
			pst.setString(1, user.getPlayer().getName());
			
			ResultSet rs = pst.executeQuery();
			
			try {
				user.setAspect(rs.getString("aspect"));
				user.setPlayerClass(rs.getString("class"));
				user.setMediumPlanet(rs.getString("mplanet"));
				user.setDreamPlanet(rs.getString("dplanet"));
				user.setTower(rs.getShort("tower"));
				user.setIsSleeping(rs.getBoolean("sleepstate"));
//				user.setCurrent(ChannelManager.getInstance().getChannel(rs.getString("currentChannel")));
//				user.setMute(rs.getBoolean("isMute"));
//				user.setNick(rs.getString("nickname"));
//				for (Entry e : rs.getArray("channels")) { // TODO Keiko, may need to be a List, not Set. Not sure.
//					if (e instanceof String) {
//						user.addListening(ChannelManager.getInstance().getChannel((String) e));
//					}
//				}
//				// IP should not be set here. Update-only, for offline IPban.
//				// TODO lastLogin     Keiko, from String, but need methods.
//				// TODO timePlayed
				
			} catch (Exception e) {
				// User may not be defined in the database
				plugin.getLogger().warning("Player "
						+ user.getPlayer().getName() +
						" does not have an entry in the database. Or something.");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deletePlayer() {
		
	}

	public void firstChannelDataSave(/* Keiko, channel object goes in here. */) {
		try {
			PreparedStatement pst = connection.prepareStatement(
					"INSERT INTO ChatChannels(name, alias, channelType, listenAccess, " +
					"sendAccess, owner, modList, banList, listening, approvedList) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			pst.setString(1, null); // TODO name
			pst.setString(2, null); // TODO alias
			pst.setString(3, null); // TODO channelType
			pst.setString(4, null); // TODO listenAccess
			pst.setString(5, null); // TODO sendAccess
			pst.setString(6, null); // TODO owner
			pst.setArray(7, null); // TODO modList
			pst.setArray(8, null); // TODO banList
			pst.setArray(9, null); // TODO listening
			pst.setArray(10, null); // TODO apprivedList
			
			pst.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateChannelData(/* Channel ch */) {
		
	}

	public void loadChannelData() {
		
	}

	public void deleteChannel() {
		
	}

	public ResultSet makeCustomCall(String MySQLStatement, boolean resultExpected) {
		try {
			PreparedStatement pst = connection.prepareStatement(MySQLStatement);
			if (resultExpected) {
				return pst.executeQuery();
			} else {
				pst.executeUpdate();
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
