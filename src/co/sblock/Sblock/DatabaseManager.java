package co.sblock.Sblock;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;

import co.sblock.Sblock.Chat.ChatModule;
import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Chat.Channel.ChannelType;
import co.sblock.Sblock.Events.EventModule;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.Utilities.Sblogger;
import co.sblock.Sblock.Utilities.TowerData;

/**
 * Collection of all database-related functions
 * 
 * @author Jikoo, FireNG
 * 
 */
public class DatabaseManager {

	private static DatabaseManager dbm;
	private Sblock plugin;
	private ArrayList<String> defaultChannels;

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

		this.establishDefaultChannels();

		Sblogger.info("SblockDatabase", "Database enabled");
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
		defaultChannels = null;
	}

	private void establishDefaultChannels() {
		defaultChannels = new ArrayList<String>();
		defaultChannels.add("#");
		defaultChannels.add("#rp");
		defaultChannels.add("#rp2");
		defaultChannels.add("#Earth");
		defaultChannels.add("#InnerCircle");
		defaultChannels.add("#OuterCircle");
		defaultChannels.add("#FurthestRing");
		defaultChannels.add("#LOWAS");
		defaultChannels.add("#LOLAR");
		defaultChannels.add("#LOHAC");
		defaultChannels.add("#LOFAF");
	}

	public void saveUserData(SblockUser user) {
		try {
			PreparedStatement pst = connection
					.prepareStatement("INSERT INTO PlayerData(name, class, aspect, "
							+ "mPlanet, dPlanet, towerNum, sleepState, currentChannel, "
							+ "isMute, nickname, channels, ip, timePlayed, previousLocation) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
							+ "ON DUPLICATE KEY UPDATE "
							+ "class=VALUES(class), aspect=VALUES(aspect), "
							+ "mPlanet=VALUES(mPlanet), dPlanet=VALUES(dPlanet), "
							+ "towerNum=VALUES(towerNum), sleepState=VALUES(sleepState), "
							+ "currentChannel=VALUES(currentChannel), "
							+ "isMute=VALUES(isMute), nickname=VALUES(nickname), "
							+ "channels=VALUES(channels), ip=VALUES(ip), "
							+ "timePlayed=VALUES(timePlayed), "
							+ "previousLocation=VALUES(previousLocation)");
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
			user.updateTimePlayed();
			pst.setString(13, user.getTimePlayed());
			try {
				pst.setString(14, user.getPreviousLocationString());
			} catch (NullPointerException e) {
				user.setPreviousLocation(Bukkit.getWorld("Earth").getSpawnLocation());
				pst.setString(14, user.getPreviousLocationString());
			}

			pst.executeUpdate();
			pst.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void loadUserData(SblockUser user) {
		try {
			PreparedStatement pst = connection
					.prepareStatement("SELECT * FROM PlayerData WHERE name=?");

			pst.setString(1, user.getPlayerName());

			ResultSet rs = pst.executeQuery();

			try {
				if (rs.next()) {
					user.setAspect(rs.getString("aspect"));
					user.setPlayerClass(rs.getString("class"));
					user.setMediumPlanet(rs.getString("mPlanet"));
					user.setDreamPlanet(rs.getString("dPlanet"));
					short tower = rs.getShort("towerNum");
					if (tower != -1) {
						user.setTower((byte) tower);
					} else {
						
					}
					user.setIsSleeping(rs.getBoolean("sleepState"));
					if(rs.getBoolean("isMute")) {
						user.setMute(true);
					}
					user.setNick(rs.getString("nickname") != null ?
							rs.getString("nickname") : user.getNick());
					if (rs.getString("channels") != null) {
						String[] channels = rs.getString("channels").split(",");
						for (int i = 0; i < channels.length; i++) {
							user.syncJoinChannel(channels[i]);
						}
					}
					if (rs.getString("previousLocation") != null) {
						user.setPreviousLocationFromString(rs.getString("previousLocation"));
					} else {
						user.setPreviousLocation(Bukkit.getWorld("Earth").getSpawnLocation());
					}
					user.syncSetCurrentChannel(rs.getString("currentChannel"));
					user.setTimePlayed(rs.getString("timePlayed"));
				} else {
					Sblogger.warning("SblockDatabase", "Player "
							+ user.getPlayerName()
							+ " does not have an entry in the database.");
				}

				pst.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteUser(SblockUser user) {
		try {
			PreparedStatement pst = connection
					.prepareStatement("DELETE FROM PlayerData WHERE name = ?");
			pst.setString(1, user.getPlayerName());

			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveChannelData(Channel c) {
		if ( defaultChannels.contains(c.getName())) {
			return;
		}
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement(
					"INSERT INTO ChatChannels(name, channelType, "
							+ "access, owner, modList, banList, approvedList) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?)"
							+ "ON DUPLICATE KEY UPDATE "
							+ "channelType=VALUES(channelType), access=VALUES(access), "
							+ "owner=VALUES(owner), modList=VALUES(modList), "
							+ "banList=VALUES(banList), "
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
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void loadAllChannelData() {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement("SELECT * FROM ChatChannels");

			ResultSet rs = pst.executeQuery();

			ChannelManager cm = ChatModule.getChatModule().getChannelManager();

			while (rs.next()) {
				cm.createNewChannel(rs.getString("name"),
						AccessLevel.valueOf(rs.getString("access")),
						rs.getString("owner"),
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
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void deleteChannel(String channelName) {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement(
					"DELETE FROM ChatChannels WHERE name = ?");
			pst.setString(1, channelName);

			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public ResultSet makeCustomCall(String MySQLStatement,
			boolean resultExpected) {
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

	/**
	 * 
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
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	/**
	 * @param towers
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
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

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
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		if (name != null) {
			return name;
		} else {
			return "Player";
		}
	}

	public String getBanReason(String name, String ip) {
		PreparedStatement pst = null;
		String ban = null;
		try {
			pst = connection.prepareStatement(
					"SELECT * FROM BannedPlayers WHERE name=? OR ip =?");

			pst.setString(1, name);

			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				ban = rs.getString("reason");
				if (name.equals(rs.getString("name"))) {
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return ban;
	}

	public void addBan(SblockUser target, String reason) {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement(
					"INSERT INTO BannedPlayers(name, ip, banDate, reason");

			pst.setString(1, target.getPlayerName());
			pst.setString(2, target.getUserIP());
			pst.setDate(3, new Date(new java.util.Date().getTime()));
			pst.setString(4, reason);

			pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

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
					// IP not saved properly or something
				}
				try {
					Bukkit.getOfflinePlayer(rs.getString("name")).setBanned(false);
				} catch (NullPointerException e) {
					// Name not saved properly or something
				}
			}

			pst.close();

			pst = connection.prepareStatement(
					"DELETE * FROM BannedPlayers WHERE name=? OR ip=?");

			pst.setString(1, target);
			pst.setString(2, target);

			pst.executeUpdate();

		} catch (SQLException e) {
			Sblogger.severe("SblockDatabase", "Error removing ban for " + target
					+ "! Please unban and unIPban manually. Also, make mySQL call:"
					+ "\nDELETE * FROM BannedPlayers WHERE name="
					+ target +" OR ip=" + target + ";");
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
