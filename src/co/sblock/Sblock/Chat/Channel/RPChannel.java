package co.sblock.Sblock.Chat.Channel;

import co.sblock.Sblock.UserData.SblockUser;

/**
 * @author Jikoo
 *
 */
public class RPChannel extends NickChannel {

	public RPChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
	}

	@Override
	public ChannelType getType() {
		return ChannelType.RP;
	}

	@Override
	public CanonNicks getNick(SblockUser user) {
		return CanonNicks.getNick(this.nickList.get(user.getPlayerName()));
	}
}