package com.easterlyn.events.listeners.vehicle;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.events.packets.ParticleEffectWrapper;
import com.easterlyn.micromodules.ParticleUtils;
import com.easterlyn.users.UserRank;

import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

/**
 * Listener for VehicleEnterEvents.
 * 
 * @author Jikoo
 */
public class EnterListener extends EasterlynListener {

	private final ParticleUtils particles;

	public EnterListener(Easterlyn plugin) {
		super(plugin);
		new Permission("easterlyn.blaze").addParent(UserRank.DONATOR.getPermission(), true).recalculatePermissibles();
		this.particles = plugin.getModule(ParticleUtils.class);
	}

	/**
	 * EventHandler for VehicleEnterEvents.
	 * 
	 * @param event the VehicleEnterEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onVehicleEnter(VehicleEnterEvent event) {
		if (event.getVehicle().getType() != EntityType.HORSE || event.getEntered().getType() != EntityType.PLAYER
				|| !((Player) event.getEntered()).hasPermission("easterlyn.blaze")) {
			return;
		}
		Horse horse = (Horse) event.getVehicle();
		ItemStack saddle = horse.getInventory().getSaddle();
		if (saddle != null && saddle.containsEnchantment(Enchantment.ARROW_FIRE)) {
			particles.addEntity(horse, new ParticleEffectWrapper(Particle.FLAME, 16));
		}
	}

}
