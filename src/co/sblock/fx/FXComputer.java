package co.sblock.fx;

import org.bukkit.event.Event;

import co.sblock.users.OnlineUser;

public class FXComputer extends SblockFX {

	@SuppressWarnings("unchecked")
	public FXComputer() {
		super("COMPUTER", false, 0, 0);
	}

	@Override
	protected void getEffect(OnlineUser u, Class<? extends Event> e) {
		return;
	}

	@Override
	public void removeEffect(OnlineUser u) {
		return;
	}
}
