package co.sblock.data.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.SblockChat;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.machines.SblockMachines;
import co.sblock.users.Region;
import co.sblock.users.User;
import co.sblock.users.UserAspect;
import co.sblock.users.User.UserBuilder;
import co.sblock.users.UserClass;
import co.sblock.users.UserManager;
import co.sblock.utilities.Log;

/**
 * SQL Database implementation
 * 
 * @author Jikoo, FireNG, tmathmeyer
 */
public class SQLClient {

	/* The SQL Connection used by the SblockData. */
	private Connection connection;
	private final Log logger = Log.getLog("SblockData-SQL");

	/*
	 * @see co.sblock.data.SblockData#enable()
	 */
	public boolean enable() {
		logger.info("Enabling SblockData");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://"
					+ Sblock.getInstance().getConfig().getString("host", "localhost") + ":"
					+ Sblock.getInstance().getConfig().getString("port", "3306") + "/"
					+ Sblock.getInstance().getConfig().getString("database", "sblock")
					+ "?autoReconnect=true",
					Sblock.getInstance().getConfig().getString("username", "root"),
					Sblock.getInstance().getConfig().getString("password", ""));
			logger.fine("Connection established.");
		} catch (ClassNotFoundException e) {
			logger.severe("Database driver not found. Plugin functionality will be limited.");
			return false;
		} catch (SQLException e) {
			logger.severe("Connection error. Plugin functionality will be limited.");
			logger.criticalErr(e);
			return false;
		} catch (NullPointerException e) {
			logger.severe("Invalid config! Required strings: host, port, database, username, password.");
		}

		logger.fine("Database enabled");
		return true;
	}

	public void disable() {
		try {
			connection.close();
		} catch (Exception e) {
			logger.err(e);
		}
		connection = null;
	}

	protected Connection connection() {
		try {
			if (connection == null || connection.isClosed()) {
				enable();
			}
		} catch (SQLException e) {
			// Yep, we're screwed.
			enable();
		}
		return connection;
	}

	/**
	 * Creates and loads all Channels from saved data.
	 */
	public void loadAllChannelData() {
		try (PreparedStatement pst = connection().prepareStatement(Call.CHANNEL_LOADALL.toString());
				ResultSet rs = pst.executeQuery()) {

			ChannelManager cm = SblockChat.getChat().getChannelManager();

			while (rs.next()) {
				Channel channel = cm.loadChannel(rs.getString("name"),
						AccessLevel.valueOf(rs.getString("access")),
						UUID.fromString(rs.getString("owner")),
						ChannelType.valueOf(rs.getString("channelType")));
				String list = rs.getString("modList");
				if (list != null) {
					String[] modList = list.split(",");
					for (int i = 0; i < modList.length; i++) {
						channel.addModerator(UUID.fromString(modList[i]));
					}
				}
				list = rs.getString("banList");
				if (list != null) {
					String[] banList = list.split(",");
					for (int i = 0; i < banList.length; i++) {
						channel.addBan(UUID.fromString(banList[i]));
					}
				}
				list = rs.getString("approvedList");
				if (list != null) {
					String[] approvedList = list.split(",");
					for (int i = 0; i < approvedList.length; i++) {
						channel.addApproved(UUID.fromString(approvedList[i]));
					}
				}
				getLogger().info("Loaded " + channel.toString());
			}
		} catch (SQLException e) {
			getLogger().err(e);
		}
	}

	/**
	 * Creates and loads all Machines from saved data.
	 */
	public void loadAllMachines() {
		try (PreparedStatement pst = connection().prepareStatement(Call.MACHINE_LOADALL.toString())) {

			ResultSet rs = pst.executeQuery();
			SblockMachines machines = SblockMachines.getInstance();

			while (rs.next()) {
				machines.loadMachine(rs.getString("location"), rs.getString("type"),
						rs.getString("owner"), rs.getByte("face"), rs.getString("data"));
				System.out.println("Loaded machine " + rs.getString("location") + " " + rs.getString("type")
						+ " " + rs.getString("owner") + " " + rs.getByte("face") + " " + rs.getString("data"));
			}
		} catch (SQLException e) {
			SblockMachines.getInstance().getLogger().err(e);
		}
	}

	/**
	 * Load a Player's data from a ResultSet.
	 * 
	 * @param rs the ResultSet to load from
	 */
	public void loadAllPlayers() {
		try (PreparedStatement pst = connection().prepareStatement(Call.PLAYER_LOAD_ALL.toString());
			ResultSet rs = pst.executeQuery()) {
			while (rs.next()) {
				UserBuilder builder = new UserBuilder();
				builder.setAspect(UserAspect.getAspect(rs.getString("aspect"))).setUserClass(UserClass.getClass(rs.getString("class")));
				builder.setMediumPlanet(Region.getRegion(rs.getString("mPlanet"))).setDreamPlanet(Region.getRegion(rs.getString("dPlanet")));
				builder.setGlobalMute(new AtomicBoolean(rs.getBoolean("isMute")));
				builder.setListening(new HashSet<String>(Arrays.asList(rs.getString("channels").split(","))));
				builder.setPreviousLocationFromString(rs.getString("previousLocation"));
				if (rs.getString("currentChannel") != null) {
					builder.setCurrentChannel(rs.getString("currentChannel"));
				} else {
					builder.setCurrentChannel("#");
				}
				User user = builder.build(UUID.fromString(rs.getString("uuid")));
				System.out.println("Loaded " + user.getUUID().toString());
				UserManager.addUser(user);
				UserManager.unloadUser(user.getUUID());
			}
		} catch (SQLException e) {
			getLogger().err(e);
		}
	}

	public Log getLogger() {
		return logger;
	}
	public enum Call {
		PLAYER_LOAD_ALL("SELECT * FROM PlayerData"),
		CHANNEL_LOADALL("SELECT * FROM ChatChannels"),
		MACHINE_LOADALL("SELECT * FROM Machines");

		private final String call;
		private Call(String call) {
			this.call = call;
		}
		public String toString() {
			return this.call;
		}
	}
}
