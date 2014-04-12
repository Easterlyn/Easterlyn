package co.sblock.Sblock.Chat.Channel;

import java.util.UUID;

import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.UserData.User;
/**
 * Defines nick channel behavior
 * 
 * @author Dublek
 */
public class RPChannel extends NickChannel {
	
	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#Channel(String, AccessLevel, UUID)
	 */
	public RPChannel(String name, AccessLevel a, UUID creator) {
		super(name, a, creator);
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getType()
	 */
	@Override
	public ChannelType getType() {
		return ChannelType.RP;
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#setNick(ChatUser, String)
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
