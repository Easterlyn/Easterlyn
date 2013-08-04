package co.sblock.Sblock.Chat.Channel;

public class RegionChannel extends NormalChannel {

	/**
	 * @param name
	 * @param a
	 * @param creator
	 */
	public RegionChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
	}

	// TODO This may just end up being a couple NormalChannels
	// We could have something that ticks every player every xtime
	// (similar to effects) instead of using onPlayerMove like we used to.
}
