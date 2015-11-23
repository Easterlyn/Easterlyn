package co.sblock.chat.channel;

import java.util.UUID;
import java.util.logging.Logger;

import co.sblock.Sblock;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

public class RegionChannel extends Channel {

	/**
	 * Constructor for a RegionChannel. Regional channels all are hardcoded and may not be created
	 * or owned by a player.
	 */
	public RegionChannel(Sblock plugin, String name) {
		super(plugin, name, null);
	}

	/**
	 * Allows chat suppression for global channels.
	 * 
	 * @see co.sblock.chat.channel.Channel#sendMessage(User, String, boolean)
	 */
	@Override
	public void sendMessage(String message) {
		for (UUID userID : this.listening.toArray(new UUID[0])) {
			OfflineUser u = Users.getGuaranteedUser(getPlugin(), userID);
			if (u == null) {
				listening.remove(userID);
				continue;
			}
			if (!u.getSuppression()) {
				u.sendMessage(message);
			}
		}
		Logger.getLogger("Minecraft").info(message);
	}

	/**
	 * Regional channels do not need last access updated; they cannot be deleted.
	 * 
	 * @see co.sblock.chat.channel.Channel#updateLastAccess()
	 */
	@Override
	public void updateLastAccess() {}

	/**
	 * Regional channels are never eligible for deletion.
	 * 
	 * @see co.sblock.chat.channel.Channel#isRecentlyAccessed()
	 */
	@Override
	public boolean isRecentlyAccessed() {
		return true;
	}

	/**
	 * Regional channels are public, but require moderator status to actually join.
	 * 
	 * @see co.sblock.chat.channel.Channel#getAccess()
	 */
	@Override
	public AccessLevel getAccess() {
		return AccessLevel.PUBLIC;
	}

	/**
	 * Owners and moderators are permission-based for regional channels.
	 * 
	 * @see co.sblock.chat.channel.Channel#isOwner(co.sblock.users.OfflineUser)
	 */
	@Override
	public boolean isOwner(OfflineUser user) {
		return user instanceof OnlineUser && ((OnlineUser) user).getPlayer().hasPermission("sblock.denizen");
	}

	/**
	 * Owners and moderators are permission-based for regional channels.
	 * 
	 * @see co.sblock.chat.channel.Channel#isModerator(co.sblock.users.OfflineUser)
	 */
	@Override
	public boolean isModerator(OfflineUser user) {
		return user instanceof OnlineUser && ((OnlineUser) user).getPlayer().hasPermission("sblock.felt");
	}

	/**
	 * All users are approved to focus on regional channels.
	 * 
	 * @see co.sblock.chat.channel.Channel#isApproved(co.sblock.users.OfflineUser)
	 */
	@Override
	public boolean isApproved(OfflineUser user) {
		return true;
	}

	/**
	 * Users cannot be banned from regional channels; they should be muted if they are that much of
	 * an issue.
	 * 
	 * @see co.sblock.chat.channel.Channel#isBanned(co.sblock.users.OfflineUser)
	 */
	@Override
	public boolean isBanned(OfflineUser user) {
		return false;
	}
}
