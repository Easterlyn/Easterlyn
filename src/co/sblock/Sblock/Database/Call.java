package co.sblock.Sblock.Database;

import java.sql.ResultSet;

import org.bukkit.command.CommandSender;

/**
 * Enum containing all SQL queries for easier editing.
 * 
 * @author Jikoo
 */
public enum Call {
	PLAYER_SAVE("INSERT INTO PlayerData(name, class, aspect, mPlanet, dPlanet, towerNum, "
			+ "sleepState, currentChannel, isMute, channels, ip, timePlayed, previousLocation, "
			+ "programs, uhc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
			+ "ON DUPLICATE KEY UPDATE class=VALUES(class), aspect=VALUES(aspect), "
			+ "mPlanet=VALUES(mPlanet), dPlanet=VALUES(dPlanet), towerNum=VALUES(towerNum), "
			+ "sleepState=VALUES(sleepState), currentChannel=VALUES(currentChannel), "
			+ "isMute=VALUES(isMute), channels=VALUES(channels), ip=VALUES(ip), "
			+ "timePlayed=VALUES(timePlayed), previousLocation=VALUES(previousLocation), "
			+ "programs=VALUES(programs), uhc=VALUES(uhc)"),
	PLAYER_LOAD("SELECT * FROM PlayerData WHERE name=?"),
	PLAYER_LOOKUP("SELECT * FROM PlayerData WHERE name=?"),
	PLAYER_DELETE("DELETE FROM PlayerData WHERE name = ?"),
	CHANNEL_SAVE("INSERT INTO ChatChannels(name, channelType, access, owner, modList, "
			+ "banList, approvedList) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
			+ "channelType=VALUES(channelType), access=VALUES(access), owner=VALUES(owner), "
			+ "modList=VALUES(modList), banList=VALUES(banList), approvedList=VALUES(approvedList)"),
	CHANNEL_LOADALL("SELECT * FROM ChatChannels"),
	CHANNEL_DELETE("DELETE FROM ChatChannels WHERE name = ?"),
	MACHINE_SAVE("INSERT INTO Machines(location, type, data, face) VALUES (?, ?, ?, ?) "
			+ "ON DUPLICATE KEY UPDATE type=VALUES(type), data=VALUES(data), face=VALUES(face)"),
	MACHINE_LOADALL("SELECT * FROM Machines"),
	MACHINE_DELETE("DELETE FROM Machines WHERE location = ?"),
	TOWER_SAVE("INSERT INTO TowerLocs(towerID, location) VALUES (?, ?) "
			+ "ON DUPLICATE KEY UPDATE location=VALUES(location)"),
	TOWER_LOAD("SELECT * FROM TowerLocs");

	/** The SQL query to make. */
	private final String call;

	/** The CommandSender triggering the query. */
	private CommandSender sender = null;

	/**
	 * Constructor for Call.
	 * 
	 * @param call the SQL query to make
	 */
	private Call(String call) {
		this.call = call;
	}

	/**
	 * Set the CommandSender.
	 * 
	 * @param s the new CommandSender
	 */
	public void setSender(CommandSender s) {
		this.sender = s;
	}

	/**
	 * Returns the SQL call with which to create a proper PreparedStatement for this Call.
	 * 
	 * @see java.lang.Object#toString()
	 * 
	 * @return the String SQL call for a PreparedStatement
	 */
	public String toString() {
		return this.call;
	}

	/**
	 * Handle the ResultSet in the event that one is returned.
	 * 
	 * @param rs the ResultSet
	 */
	public void result(ResultSet rs) {
		switch (this) {
		case PLAYER_LOAD:
			PlayerData.loadPlayer(rs);
			return;
		case PLAYER_LOOKUP:
			PlayerData.loadOfflineLookup(sender, rs);
			return;
		default:
			return;
		}
	}
}
