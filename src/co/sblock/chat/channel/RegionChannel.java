package co.sblock.chat.channel;

import java.util.UUID;

import co.sblock.users.User;
import co.sblock.utilities.Log;

public class RegionChannel extends NormalChannel {

	/**
	 * @see co.sblock.chat.channel.Channel#Channel(String, AccessLevel, UUID)
	 */
	public RegionChannel(String name, AccessLevel a, UUID creator) {
		super(name, a, creator);
	}

	/**
	 * @see co.sblock.chat.channel.Channel#getType()
	 */
	@Override
	public ChannelType getType() {
		return ChannelType.REGION;
	}

	/**
	 * Allows null senders and chat suppression for global channels.
	 * 
	 * @see co.sblock.chat.channel.Channel#sendToAll(User, String, boolean)
	 */
	@Override
	public void sendToAll(User sender, String message, boolean format) {
		if (format) {
			message = this.formatMessage(sender, message);
		}
		for (UUID userID : this.listening.toArray(new UUID[0])) {
			User u = User.getUser(userID);
			if (u == null) {
				listening.remove(userID);
				continue;
			}
			if (!u.isSuppressing()) {
				u.sendMessage(message, sender != null && !userID.equals(sender.getUUID()),
						u.getPlayer().getDisplayName(), this.getNick(u));
			}
		}
		if (sender != null && !this.name.equals("#")) {
			// Chester logs even if events are cancelled, chat appears in console.
			Log.anonymousInfo(message);
		}
	}
}
