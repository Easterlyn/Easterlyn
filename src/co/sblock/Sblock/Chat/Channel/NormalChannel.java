package co.sblock.Sblock.Chat.Channel;

import java.util.UUID;

import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.UserData.User;
/**
 * Defines normal channel behavior.
 * 
 * @author Dublek
 */
public class NormalChannel extends Channel {
	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#Channel(String, AccessLevel, String)
	 */
	public NormalChannel(String name, AccessLevel a, UUID creator) {
		super(name, a, creator);
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getType()
	 */
	@Override
	public ChannelType getType() {
		return ChannelType.NORMAL;
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#setNick(ChatUser, String)
	 */
	@Override
	public void setNick(User sender, String nick) {
		sender.sendMessage(ChatMsgs.unsupportedOperation(this.name), false);
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#removeNick(ChatUser)
	 */
	@Override
	public void removeNick(User sender) {
		sender.sendMessage(ChatMsgs.unsupportedOperation(this.name), false);
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getNick(ChatUser)
	 */
	@Override
	public String getNick(User sender) {
		return sender.getPlayer().getDisplayName();
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#hasNick(ChatUser)
	 */
	@Override
	public boolean hasNick(User sender) {
		return false;
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getNickOwner(String)
	 */
	@Override
	public User getNickOwner(String nick) {
		return null;
	}
}
