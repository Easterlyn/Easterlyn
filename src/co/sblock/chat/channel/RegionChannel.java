package co.sblock.chat.channel;

import java.util.UUID;

public class RegionChannel extends NormalChannel {

	/**
	 * @see co.sblock.Chat.Channel.Channel#Channel(String, AccessLevel, UUID)
	 */
	public RegionChannel(String name, AccessLevel a, UUID creator) {
		super(name, a, creator);
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#getType()
	 */
	@Override
	public ChannelType getType() {
		return ChannelType.REGION;
	}
}
