/**
 * 
 */
package co.sblock.Sblock.Database;

import java.sql.ResultSet;

/**
 * @author Jikoo
 *
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
	BAN_SAVE("INSERT INTO BannedPlayers(name, ip, banDate, reason) VALUES(?, ?, ?, ?) ON DUPLICATE KEY "
			+ "UPDATE name=VALUES(name), ip=VALUES(ip), banDate=VALUES(banDate), reason=VALUES(reason)"),
	BAN_LOAD("SELECT * FROM BannedPlayers WHERE name=? OR ip =?"),
	BAN_DELETE("DELETE FROM BannedPlayers WHERE name=? OR ip=?"),
	TOWER_SAVE("INSERT INTO TowerLocs(towerID, location) VALUES (?, ?) "
			+ "ON DUPLICATE KEY UPDATE location=VALUES(location)"),
	TOWER_LOAD("SELECT * FROM TowerLocs");

	private final String call;
	private Call(String call) {
		this.call = call;
	}

	public String toString() {
		return this.call;
	}

	public void result(ResultSet rs) {
		switch (this) {
		case PLAYER_LOAD:
			PlayerData.loadPlayer(rs);
			break;
		case PLAYER_SAVE:
			PlayerData.verifyUserOffline(rs);
			break;
		case BAN_LOAD:
			BannedPlayers.removeBan(rs);
			break;
		default:
			break;
		}
	}
}
