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
	 * @see co.sblock.chat.channel.Channel#sendMessage(User, String, boolean)
	 */
	@Override
	public void sendMessage(String message) {
		for (UUID userID : this.listening.toArray(new UUID[0])) {
			User u = User.getUser(userID);
			if (u == null) {
				listening.remove(userID);
				continue;
			}
			if (!u.isSuppressing()) {
				u.sendMessage(message);
			}
		}
		Log.anonymousInfo(message);
	}
}
