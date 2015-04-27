package co.sblock.effects.fx;

import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.users.OnlineUser;

public class FXAbsorption extends SblockFX {
	
	

	@SuppressWarnings("unchecked")
	public FXAbsorption() {
		super("ABSORPTION", true, 500, 1000 * 58, PlayerPickupItemEvent.class, PlayerDropItemEvent.class,
				InventoryCloseEvent.class, PlayerDeathEvent.class);
		this.addCommonName("Hearts");
		this.addCommonName("Love");
	}

	@Override
	protected void getEffect(OnlineUser user, Event event) {
		// EFFECTS: Add scheduler after shift to global perspective
		PotionEffect potEffect = new PotionEffect(PotionEffectType.ABSORPTION, 1000 * 60, getMultiplier());
		user.getPlayer().addPotionEffect(potEffect, true);
	}

	@Override
	public void removeEffect(OnlineUser user) {
		user.getPlayer().removePotionEffect(PotionEffectType.ABSORPTION);
	}

}
