package co.sblock.fx;

import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.users.User;

public class FXSpeed extends SblockFX {
	
	@SuppressWarnings("unchecked")
	public FXSpeed() {
		super(true, 500, 0, PlayerPickupItemEvent.class, PlayerDropItemEvent.class, 
				InventoryCloseEvent.class, PlayerDeathEvent.class);
		name = "SPEED";
	}

	@Override
	protected void getEffect(User u, Class<? extends Event> e) {
		PotionEffect potEffect = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, multiplier);
		u.getPlayer().addPotionEffect(potEffect, true);		
	}

	@Override
	public void removeEffect(User u) {
		u.getPlayer().removePotionEffect(PotionEffectType.SPEED);
	}
}
