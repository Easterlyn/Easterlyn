package co.sblock.fx;

import org.bukkit.event.Event;

import co.sblock.users.OnlineUser;

public class FXComputer extends SblockFX {

	@SuppressWarnings("unchecked")
	public FXComputer() {
		super("COMPUTER", false, 0, 0);
	}

	@Override
	protected void getEffect(OnlineUser user, Event event) {
		return;
	}

	@Override
	public void removeEffect(OnlineUser user) {
		return;
	}
}
