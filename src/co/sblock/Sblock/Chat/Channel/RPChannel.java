package co.sblock.Sblock.Chat.Channel;

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
}