package co.sblock.effects.fx;

import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.users.OnlineUser;

public class FXSpeed extends SblockFX {

	@SuppressWarnings("unchecked")
	public FXSpeed() {
		super("SPEED", true, 500, 0, PlayerPickupItemEvent.class, PlayerDropItemEvent.class,
				InventoryCloseEvent.class, PlayerDeathEvent.class);
	}

	@Override
	protected void getEffect(OnlineUser user, Event event) {
		PotionEffect potEffect = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, getMultiplier());
		user.getPlayer().addPotionEffect(potEffect, true);
	}

	@Override
	public void removeEffect(OnlineUser user) {
		user.getPlayer().removePotionEffect(PotionEffectType.SPEED);
	}
}
