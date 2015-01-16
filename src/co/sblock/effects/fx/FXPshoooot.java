package co.sblock.effects.fx;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import co.sblock.users.OnlineUser;

public class FXPshoooot extends SblockFX {

	@SuppressWarnings("unchecked")
	public FXPshoooot() {
		super("PSHOOOOT", false, 350, 1000 * 3, PlayerInteractEvent.class);
		addCommonName("PCHOOOOO");
	}

	@Override
	protected void getEffect(OnlineUser user, Event event) {
		Player player = user.getPlayer();
		Vector vector = player.getLocation().getDirection();
		player.setVelocity(vector.multiply(getMultiplier() + 2));
	}

	@Override
	public void removeEffect(OnlineUser user) {}
}
