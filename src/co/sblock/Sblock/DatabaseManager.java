package co.sblock.Sblock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import co.sblock.Sblock.Chat.ChatModule;
import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.UserData.SblockUser;

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

	public void saveUserData(SblockUser user) {
		try {
			PreparedStatement pst = connection.prepareStatement(
					"INSERT INTO PlayerData(playerName, class, aspect, " +
					"mplanet, dplanet, towernum, sleepstate, currentChannel, " +
					"isMute, nickname, channels, ip, LastLogin, timePlayed) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
					"ON DUPLICATE KEY UPDATE " +
					"class=VALUES(class), aspect=VALUES(aspect), " +
					"mplanet=VALUES(mplanet), dplanet=VALUES(dplanet), " +
					"towernum=VALUES(towernum), sleepstate=VALUES(sleepstate), " +
					"currentChannel=VALUES(currentChannel), isMute=VALUES(isMute), " +
					"nickname=VALUES(nickname), channels=VALUES(channels), ip=VALUES(ip), " +
					"LastLogin=VALUES(LastLogin), timePlayed=VALUES(timePlayed)");
			
			pst.setString(1, user.getPlayerName());
			pst.setString(2, user.getClassType().getDisplayName());
			pst.setString(3, user.getAspect().getDisplayName());
			pst.setString(4, user.getMPlanet().getShortName());
			pst.setString(5, user.getDPlanet().getDisplayName());
			pst.setShort(6, user.getTower());
			pst.setBoolean(7, user.isSleeping());
			pst.setString(8, user.getCurrent().getName());
			pst.setBoolean(9, user.isMute());
			pst.setString(10, user.getNick());
			StringBuilder sb = new StringBuilder();
			for (String s : user.getListening()) {
				sb.append(s + ",");
			}
			pst.setString(11, sb.substring(0, sb.length() - 1));
			pst.setString(12, user.getUserIP());
			pst.setTimestamp(13, null); // TODO lastLogin
			pst.setTime(14, null);
			
			pst.executeUpdate();
			pst.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void loadUserData(SblockUser user) {
		try {
			PreparedStatement pst = connection.prepareStatement(
					"SELECT * FROM PlayerData WHERE name=?");
			
			pst.setString(1, user.getPlayerName());
			
			ResultSet rs = pst.executeQuery();
			
			try {
				user.setAspect(rs.getString("aspect"));
				user.setPlayerClass(rs.getString("class"));
				user.setMediumPlanet(rs.getString("mplanet"));
				user.setDreamPlanet(rs.getString("dplanet"));
				user.setTower(rs.getShort("tower"));
				user.setIsSleeping(rs.getBoolean("sleepstate"));
				user.setCurrent(ChatModule.getInstance().getChannelManager().getChannel(rs.getString("currentChannel")));
				user.setMute(rs.getBoolean("isMute"));
				user.setNick(rs.getString("nickname"));
				String[] channels = rs.getString("channels").split(",");
				for (int i = 0; i < channels.length; i++) {
					user.addListening(ChatModule.getInstance().getChannelManager().getChannel(channels[i]));
				}
				// IP should not be set here. Update-only, for offline IPban.
				// TODO lastLogin
				// TODO timePlayed
				
				pst.close();
				
				
			} catch (Exception e) {
				// User may not be defined in the database
				plugin.getLogger().warning("Player "
						+ user.getPlayerName() +
						" does not have an entry in the database. Or something.");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteUser(SblockUser user) {
		try {
			PreparedStatement pst = connection.prepareStatement(
					"DELETE FROM PlayerData WHERE name = ?");
			pst.setString(1, user.getPlayerName());
			
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveChannelData(Channel c) {
		try {
			PreparedStatement pst = connection.prepareStatement(
					"INSERT INTO ChatChannels(name, alias, channelType, listenAccess, " +
					"sendAccess, owner, modList, banList, approvedList) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)" +
					"ON DUPLICATE KEY UPDATE alias=VALUES(alias), " +
					"channelType=VALUES(channelType), listenAccess=VALUES(listenAccess), " + 
					"sendAccess=VALUES(sendAccess), owner=VALUES(owner), " +
					"modList=VALUES(modList), banList=VALUES(banList), " +
					"approvedList=VALUES(approvedList), ");
			
			pst.setString(1, c.getName());
//			pst.setString(2, c.getAlias);
			pst.setString(3, c.getType().name());
//			pst.setString(4, c.getLAccessLevel().name()); // TODO
//			pst.setString(5, c.getSAccessLevel().name()); // TODO
			pst.setString(6, c.getOwner());
			StringBuilder sb = new StringBuilder();
			for (String s : c.getModList()) {
				sb.append(s + ",");
			}
			pst.setString(7, sb.substring(0, sb.length() - 1));
			sb = new StringBuilder();
			for (String s : c.getBanList()) {
				sb.append(s + ",");
			}
			pst.setString(8, sb.substring(0, sb.length() - 1));
			sb = new StringBuilder();
			for (String s : c.getApprovedUsers()) {
				sb.append(s + ",");
			}
			pst.setString(9, sb.substring(0, sb.length() - 1));
			
			pst.executeUpdate();
			pst.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void loadAllChannelData() {
		PreparedStatement pst;
		try {
			pst = connection.prepareStatement(
					"SELECT * FROM ChatChannels");
			
			ResultSet rs = pst.executeQuery();
			
			ChannelManager cm = ChatModule.getInstance().getChannelManager();
			
			while (rs.next()) {
				cm.createNewChannel(rs.getString("name"),
						AccessLevel.valueOf(rs.getString("sendAccess")),
						AccessLevel.valueOf(rs.getString("listenAccess")),
						rs.getString("owner")/*, ChannelType.valueOf(rs.getString(channelType))*/);
				Channel c = ChatModule.getInstance().getChannelManager().getChannel(rs.getString("name"));
//				c.setAlias(rs.getString("alias"));
				String[] modList = rs.getString("modList").split(",");
				for (int i = 0; i < modList.length; i++) {
					c.loadMod(modList[i]); // TODO
				}
				String[] banList = rs.getString("banList").split(",");
				for (int i = 0; i < banList.length; i++) {
					c.loadBan(banList[i]); // TODO
				}
				String[] approvedList = rs.getString("approvedList").split(",");
				for (int i = 0; i < approvedList.length; i++) {
					c.loadApproval(approvedList[i]); //TODO
				}
			}
			
			pst.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteChannel(String channelName) {
		try {
			PreparedStatement pst = connection.prepareStatement(
					"DELETE FROM ChatChannels WHERE name = ?");
			pst.setString(1, channelName);
			
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

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
			e.printStackTrace();
			return null;
		}
	}
}
