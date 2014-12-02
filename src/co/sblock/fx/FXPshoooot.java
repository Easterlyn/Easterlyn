package co.sblock.fx;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import co.sblock.users.User;

public class FXPshoooot extends SblockFX {
	
	@SuppressWarnings("unchecked")
	public FXPshoooot() {
		super(false, 0, PlayerInteractEvent.class);
		name = "PSHOOOOT";
	}

	@Override
	protected void getEffect(User u) {
		Player p = u.getPlayer();
		Vector v = p.getLocation().getDirection();
		p.setVelocity(v.multiply(multiplier + 2));
	}

	@Override
	public void removeEffect(User u) {
		return;		//N/A for this effect
	}
}
