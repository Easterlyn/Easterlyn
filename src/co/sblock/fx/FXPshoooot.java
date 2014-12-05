package co.sblock.fx;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import co.sblock.users.OnlineUser;

public class FXPshoooot extends SblockFX {

	@SuppressWarnings("unchecked")
	public FXPshoooot() {
		super("PSHOOOOT", false, 350, 0, PlayerInteractEvent.class);
		addCommonName("PCHOOOOO");
	}

	@Override
	protected void getEffect(OnlineUser u, Class<? extends Event> e) {
		Player p = u.getPlayer();
		Vector v = p.getLocation().getDirection();
		p.setVelocity(v.multiply(multiplier + 2));
	}

	@Override
	public void removeEffect(OnlineUser u) {
		return;
	}
}
