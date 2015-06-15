package co.sblock.chat.channel;

import java.util.UUID;

import co.sblock.chat.ChatMsgs;
import co.sblock.users.OfflineUser;

/**
 * Defines normal channel behavior.
 * 
 * @author Dublek, tmathmeyer
 */
public class NormalChannel extends Channel {

	/**
	 * @see co.sblock.Chat.Channel.Channel#Channel(String, AccessLevel, String)
	 */
	public NormalChannel(String name, AccessLevel a, UUID creator, long lastAccessed) {
		super(name, a, creator, lastAccessed);
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#getType()
	 */
	@Override
	public ChannelType getType() {
		return ChannelType.NORMAL;
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#setNick(ChatUser, String)
	 */
	@Override
	public void setNick(OfflineUser sender, String nick) {
		sender.sendMessage(ChatMsgs.unsupportedOperation(this.name));
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#removeNick(ChatUser)
	 */
	@Override
	public void removeNick(OfflineUser sender, boolean warn) {
		if (warn) {
			sender.sendMessage(ChatMsgs.unsupportedOperation(this.name));
		}
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#getNick(ChatUser)
	 */
	@Override
	public String getNick(OfflineUser sender) {
		return sender.getDisplayName();
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#hasNick(ChatUser)
	 */
	@Override
	public boolean hasNick(OfflineUser sender) {
		return false;
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#getNickOwner(String)
	 */
	@Override
	public OfflineUser getNickOwner(String nick) {
		return null;
	}
}
