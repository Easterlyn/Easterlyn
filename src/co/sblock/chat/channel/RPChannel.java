package co.sblock.chat.channel;

import java.util.UUID;

import co.sblock.chat.ChatMsgs;
import co.sblock.users.OfflineUser;

/**
 * Defines RP channel behavior
 * 
 * @author Dublek
 */
public class RPChannel extends NickChannel {
	
	/**
	 * @see co.sblock.chat.channel.Channel#Channel(String, AccessLevel, UUID)
	 */
	public RPChannel(String name, AccessLevel a, UUID creator) {
		super(name, a, creator);
	}

	/**
	 * @see co.sblock.chat.channel.Channel#getType()
	 */
	@Override
	public ChannelType getType() {
		return ChannelType.RP;
	}

	/**
	 * @see co.sblock.chat.channel.Channel#setNick(ChatUser, String)
	 */
	@Override
	public void setNick(OfflineUser sender, String nick) {
		CanonNicks name = CanonNicks.getNick(nick);
		if (name == null) {
			sender.sendMessage(ChatMsgs.errorNickNotCanon(nick));
			return;
		} else if (this.getNickOwner(name.getColor() + name.getName()) == null) {
			super.setNick(sender, name.getColor() + name.getName());
		} else {
			sender.sendMessage(ChatMsgs.errorNickInUse(name.getName()));
		}
	}
}
