package co.sblock.chat.channel;

import java.util.UUID;

import co.sblock.chat.ChatMsgs;
import co.sblock.users.User;

/**
 * Defines RP channel behavior
 * 
 * @author Dublek
 */
public class RPChannel extends NickChannel {
	
	/**
	 * @see co.sblock.Chat.Channel.Channel#Channel(String, AccessLevel, UUID)
	 */
	public RPChannel(String name, AccessLevel a, UUID creator) {
		super(name, a, creator);
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#getType()
	 */
	@Override
	public ChannelType getType() {
		return ChannelType.RP;
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#setNick(ChatUser, String)
	 */
	@Override
	public void setNick(User sender, String nick) {
		CanonNicks name = CanonNicks.getNick(nick);
		if (name == null) {
			sender.sendMessage(ChatMsgs.errorNickNotCanon(nick), false);
			return;
		} else if (this.getNickOwner(name.getName()) == null) {
			super.setNick(sender, name.getName());
		} else {
			sender.sendMessage(ChatMsgs.errorNickInUse(name.getName()), false);
		}
	}
}
