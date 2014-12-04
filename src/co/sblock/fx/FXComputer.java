package co.sblock.fx;

import org.bukkit.event.Event;

import co.sblock.users.User;

public class FXComputer extends SblockFX {

	@SuppressWarnings("unchecked")
	public FXComputer() {
		super("COMPUTER", false, 0, 0);
	}

	@Override
	protected void getEffect(User u, Class<? extends Event> e) {
		return;
	}

	@Override
	public void removeEffect(User u) {
		return;
	}

}
