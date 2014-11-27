package co.sblock.fx;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import co.sblock.users.User;

public class FXPshoooot extends SblockFX {
	
	private String name;
	private Class<? extends Event> eventTrigger;
	
	private Integer multiplier;
	
	private long lastTriggered;
	private long cooldown;
	
	private FXManager manager;
	
	public FXPshoooot() {
		name = "PSHOOOOT";
		eventTrigger = PlayerInteractEvent.class;
		multiplier = 1;
		lastTriggered = 0;
		cooldown = 0;
	}

	@Override
	protected void getEffect(User u, Event e) {
		Player p = u.getPlayer();
		Vector v = p.getLocation().getDirection();
		p.setVelocity(v.multiply(multiplier + 2));
	}

}
