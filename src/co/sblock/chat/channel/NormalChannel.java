package co.sblock.chat.channel;

import java.util.UUID;

import co.sblock.chat.ChatMsgs;
import co.sblock.users.User;

/**
 * Defines normal channel behavior.
 * 
 * @author Dublek
 */
public class NormalChannel extends Channel {
	/**
	 * @see co.sblock.Chat.Channel.Channel#Channel(String, AccessLevel, String)
	 */
	public NormalChannel(String name, AccessLevel a, UUID creator) {
		super(name, a, creator);
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
	public void setNick(User sender, String nick) {
		sender.sendMessage(ChatMsgs.unsupportedOperation(this.name));
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#removeNick(ChatUser)
	 */
	@Override
	public void removeNick(User sender, boolean warn) {
		if (warn) {
			sender.sendMessage(ChatMsgs.unsupportedOperation(this.name));
		}
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#getNick(ChatUser)
	 */
	@Override
	public String getNick(User sender) {
		return sender.getPlayer().getDisplayName();
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#hasNick(ChatUser)
	 */
	@Override
	public boolean hasNick(User sender) {
		return false;
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#getNickOwner(String)
	 */
	@Override
	public User getNickOwner(String nick) {
		return null;
	}
}
