package co.sblock.Sblock.Chat.Channel;

import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.Chat.ChatUser;
/**
 * Defines normal channel behavior.
 * 
 * @author Dublek
 */
public class NormalChannel extends Channel {
	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#Channel(String, AccessLevel, String)
	 */
	public NormalChannel(String name, AccessLevel a, String creator) {
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
	public void setNick(ChatUser sender, String nick) {
		sender.sendMessage(ChatMsgs.unsupportedOperation(this.name));
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#removeNick(ChatUser)
	 */
	@Override
	public void removeNick(ChatUser sender) {
		sender.sendMessage(ChatMsgs.unsupportedOperation(this.name));
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getNick(ChatUser)
	 */
	@Override
	public String getNick(ChatUser sender) {
		sender.sendMessage(ChatMsgs.unsupportedOperation(this.name));
		return null;
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#hasNick(ChatUser)
	 */
	@Override
	public boolean hasNick(ChatUser sender) {
		return false;
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getNickOwner(String)
	 */
	@Override
	public ChatUser getNickOwner(String nick) {
		return null;
	}
}
