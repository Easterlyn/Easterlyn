package co.sblock.effects.fx;

import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.users.OnlineUser;

/**
 * Jump boost passive effect
 * 
 * @author Jikoo
 */
public class FXJump extends SblockFX {

	@SuppressWarnings("unchecked")
	public FXJump() {
		super("JUMP", true, 500, 0, PlayerPickupItemEvent.class, PlayerDropItemEvent.class,
				InventoryCloseEvent.class, PlayerDeathEvent.class);
		this.addCommonName("Boing");
	}

	@Override
	protected void getEffect(OnlineUser user, Event event) {
		PotionEffect potEffect = new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, getMultiplier());
		user.getPlayer().addPotionEffect(potEffect, true);
	}

	@Override
	public void removeEffect(OnlineUser user) {
		user.getPlayer().removePotionEffect(PotionEffectType.JUMP);
	}
}
