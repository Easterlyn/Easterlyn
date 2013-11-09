package co.sblock.Sblock.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import co.sblock.Sblock.Chat.ChatModule;
import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Chat.Channel.ChannelType;
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * A collection of all the Runnables used to fetch from and modify the
 * ChatChannels table.
 * <p>
 * The ChatChannels table is created by the following call:
 * CREATE TABLE ChatChannels (name varchar(16) UNIQUE KEY, channelType varchar(6),
 * access varchar(7), owner varchar(16), modList text, banList text, approvedList text);
 * 
 * @author Jikoo
 */
public class ChatChannels {
	/**
	 * Save <code>Channel</code> data to database.
	 * 
	 * @param c
	 *            the <code>Channel</code> to save data for
	 */
	public static void saveChannelData(Channel c) {
		PreparedStatement pst = null;
		try {
			pst = DBManager.getDBM().connection().prepareStatement(Call.CHANNEL_SAVE.toString());

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
	public static void loadAllChannelData() {
		PreparedStatement pst = null;
		try {
			pst = DBManager.getDBM().connection().prepareStatement(Call.CHANNEL_LOADALL.toString());

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
	 * Create a <code>PreparedStatement</code> with which to query the SQL database.
	 * Delete a <code>Channel</code> by name.
	 * 
	 * @param channelName
	 *            the name of the <code>Channel</code> to delete
	 */
	public static void deleteChannel(String channelName) {
		try {
			PreparedStatement pst = DBManager.getDBM().connection()
					.prepareStatement(Call.CHANNEL_DELETE.toString());
			pst.setString(1, channelName);

			new AsyncCall(pst).schedule();
		} catch (SQLException e) {
			Sblogger.err(e);
		}
	}
}
