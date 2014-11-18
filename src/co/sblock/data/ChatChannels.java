package co.sblock.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import co.sblock.chat.SblockChat;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.channel.ChannelType;

/**
 * A small helper class containing all methods that access the ChatChannels table.
 * <p>
 * The ChatChannels table is created by the following call:
 * CREATE TABLE ChatChannels (name varchar(16) UNIQUE KEY, channelType varchar(6),
 * access varchar(7), owner varchar(255), modList text, banList text, approvedList text);
 * 
 * @author Jikoo
 */
public class ChatChannels {
	/**
	 * Save Channel data to database.
	 * 
	 * @param c the Channel to save data for
	 */
	public static void saveChannelData(Channel c) {
		PreparedStatement pst = null;
		try {
			pst = SblockData.getDB().connection().prepareStatement(Call.CHANNEL_SAVE.toString());

			pst.setString(1, c.getName());
			pst.setString(2, c.getType().name());
			pst.setString(3, c.getAccess().name());
			pst.setString(4, c.getOwner().toString());
			StringBuilder sb = new StringBuilder();
			for (UUID uuid : c.getModList()) {
				sb.append(uuid).append(",");
			}
			if (sb.length() > 0) {
				pst.setString(5, sb.substring(0, sb.length() - 1));
			} else {
				pst.setString(5, null);
			}
			sb = new StringBuilder();
			for (UUID uuid : c.getBanList()) {
				sb.append(uuid).append(",");
			}
			if (sb.length() > 0) {
				pst.setString(6, sb.substring(0, sb.length() - 1));
			} else {
				pst.setString(6, null);
			}
			sb = new StringBuilder();
			for (UUID uuid : c.getApprovedUsers()) {
				sb.append(uuid).append(",");
			}
			if (sb.length() > 0) {
				pst.setString(7, sb.substring(0, sb.length() - 1));
			} else {
				pst.setString(7, null);
			}

			pst.executeUpdate();
		} catch (SQLException e) {
			SblockData.getDB().getLogger().err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					SblockData.getDB().getLogger().err(e);
				}
			}
		}
	}

	/**
	 * Creates and loads all Channels from saved data.
	 */
	public static void loadAllChannelData() {
		PreparedStatement pst = null;
		try {
			pst = SblockData.getDB().connection().prepareStatement(Call.CHANNEL_LOADALL.toString());

			ResultSet rs = pst.executeQuery();

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
			}
		} catch (SQLException e) {
			SblockData.getDB().getLogger().err(e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					SblockData.getDB().getLogger().err(e);
				}
			}
		}
	}

	/**
	 * Create a PreparedStatement with which to query the SQL database. Delete a
	 * Channel by name.
	 * 
	 * @param channelName the name of the Channel to delete
	 */
	public static void deleteChannel(String channelName) {
		try {
			PreparedStatement pst = SblockData.getDB().connection()
					.prepareStatement(Call.CHANNEL_DELETE.toString());
			pst.setString(1, channelName);

			new AsyncCall(pst).schedule();
		} catch (SQLException e) {
			SblockData.getDB().getLogger().err(e);
		}
	}
}
