package co.sblock.Sblock.Chat2.Channel;

import co.sblock.Sblock.Chat2.ChatMsgs;
import co.sblock.Sblock.Chat2.ChatUser;

public class RegionChannel extends Channel {

	/**
	 * @param name
	 * @param a
	 * @param creator
	 */
	public RegionChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
	}
	
	@Override
	public ChannelType getType() {
		return ChannelType.REGION;
	}
	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat2.Channel.Channel#setNick(co.sblock.Sblock.Chat2.ChatUser, java.lang.String)
	 */
	@Override
	public void setNick(ChatUser sender, String nick) {
		sender.sendMessage(ChatMsgs.unsupportedOperation(this));		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat2.Channel.Channel#removeNick(co.sblock.Sblock.Chat2.ChatUser)
	 */
	@Override
	public void removeNick(ChatUser sender) {
		sender.sendMessage(ChatMsgs.unsupportedOperation(this));		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat2.Channel.Channel#getNick(co.sblock.Sblock.Chat2.ChatUser)
	 */
	@Override
	public String getNick(ChatUser sender) {
		sender.sendMessage(ChatMsgs.unsupportedOperation(this));
		return null;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat2.Channel.Channel#hasNick(co.sblock.Sblock.Chat2.ChatUser)
	 */
	@Override
	public boolean hasNick(ChatUser sender) {
		return false;
	}

	@Override
	public ChatUser getNickOwner(String nick) {
		return null;
	}

}
