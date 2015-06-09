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
		CanonNick name = CanonNick.getNick(nick);
		if (name == null) {
			sender.sendMessage(ChatMsgs.errorNickNotCanon(nick));
			return;
		}
		for (String nickname : nickList.values()) {
			if (CanonNick.getNick(nickname).getDisplayName().equals(name.getDisplayName())) {
				sender.sendMessage(ChatMsgs.errorNickInUse(name.getDisplayName()));
				return;
			}
		}
		super.setNick(sender, name.getColor() + name.getId());
	}
}
