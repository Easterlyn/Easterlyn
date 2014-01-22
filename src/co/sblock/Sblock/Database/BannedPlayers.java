package co.sblock.Sblock.Database;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.Utilities.Log;

/**
 * A small helper class containing all methods that access the BannedPlayers
 * table.
 * <p>
 * The BannedPlayers table is created by the following call:
 * CREATE TABLE BannedPlayers (name varchar(16) UNIQUE KEY, ip varchar(16),
 * banDate Date, reason text);
 * 
 * @author Jikoo
 */
public class BannedPlayers {

	/**
	 * Create a PreparedStatement with which to query the SQL database. Adds a
	 * ban for the specified SblockUser.
	 * 
	 * @param target the SblockUser to add a ban for
	 * @param reason the reason the SblockUser was banned
	 */
	protected static void addBan(SblockUser target, String reason) {
		PreparedStatement pst;
		try {
			pst = SblockData.getDB().connection().prepareStatement(Call.BAN_SAVE.toString());
			pst.setString(1, target.getPlayerName());
			pst.setString(2, target.getUserIP());
			pst.setDate(3, new Date(new java.util.Date().getTime()));
			pst.setString(4, reason);

			new AsyncCall(pst).schedule();
		} catch (SQLException e) {
			Log.err(e);
		}
	}

	/**
	 * Create a PreparedStatement with which to query the SQL database. Fetch
	 * and remove all matching bans from the database.
	 * 
	 * @param target the name or IP to match
	 */
	protected static void loadAndDeleteBans(String target) {
		try {
			PreparedStatement pst = SblockData.getDB().connection()
					.prepareStatement(Call.BAN_LOAD.toString());
			pst.setString(1, target);
			pst.setString(2, target);

			new AsyncCall(pst, Call.BAN_LOAD).schedule();
		} catch (SQLException e) {
			Log.err(e);
		}
	}

	/**
	 * Remove any bans by name and IP.
	 * 
	 * @param rs the ResultSet containing all matching bans
	 */
	protected static void removeBan(ResultSet rs) {
		try {
			if (!rs.next()) {
				// We have no record of this player's ban, but remove ban them anyway.
				Bukkit.getOfflinePlayer(
						rs.getStatement().toString().replaceAll("com.*name='(.*)'", "$1"))
						.setBanned(false);
			}
			// Move cursor back to before first result set so while loop hits all
			rs.absolute(0);
			while (rs.next()) {
				try {
					Bukkit.unbanIP(rs.getString("ip"));
				} catch (Exception e) {
					SblockData.getDB().getLogger().fine("No IP saved for ban");
				}
				try {
					Bukkit.getOfflinePlayer(rs.getString("name")).setBanned(false);
				} catch (Exception e) {
					SblockData.getDB().getLogger().fine("No name saved for ban");
				}
				PreparedStatement pst = SblockData.getDB().connection()
						.prepareStatement(Call.BAN_DELETE.toString());
				pst.setString(1, rs.getString("name"));
				pst.setString(2, rs.getString("ip"));

				new AsyncCall(pst).schedule();
			}
		} catch (SQLException e) {
			Log.err(e);
		}
	}

	/**
	 * Get the reason a SblockUser was banned.
	 * 
	 * @param name the name of the banned SblockUser
	 * @param ip the IP of the banned SblockUser
	 * 
	 * @return the ban reason
	 */
	protected static String getBanReason(String name, String ip) {
		PreparedStatement pst = null;
		String ban = null;
		try {
			pst = SblockData.getDB().connection().prepareStatement(Call.BAN_LOAD.toString());

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
		return ban;
	}
}
