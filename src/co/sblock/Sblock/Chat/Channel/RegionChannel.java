package co.sblock.Sblock.Chat.Channel;

import java.util.UUID;

public class RegionChannel extends NormalChannel {

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#Channel(String, AccessLevel, UUID)
	 */
	public RegionChannel(String name, AccessLevel a, UUID creator) {
		super(name, a, creator);
	}
}
