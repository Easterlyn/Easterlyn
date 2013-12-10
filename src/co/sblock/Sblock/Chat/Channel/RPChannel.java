package co.sblock.Sblock.Chat.Channel;

import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.Chat.ChatUser;
/**
 * Defines nick channel behavior
 * 
 * @author Dublek
 */
public class RPChannel extends NickChannel {
	
	/**
	 * @param name
	 * @param a
	 * @param creator
	 */
	public RPChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
	}

	@Override
	public ChannelType getType() {
		return ChannelType.RP;
	}

	@Override
	public void setNick(ChatUser sender, String nick) {
		CanonNicks name = CanonNicks.getNick(nick);
		if(name == null)	{
			sender.sendMessage(ChatMsgs.errorNickNotCanon(nick));
			return;
		}
		else if(this.getNickOwner(nick) == null)	{
			super.setNick(sender, nick);
		}
		else	{
			sender.sendMessage(ChatMsgs.errorNickInUse(nick));
		}
	}
}
