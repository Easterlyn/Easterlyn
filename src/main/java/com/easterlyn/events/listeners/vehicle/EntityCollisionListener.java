package com.easterlyn.events.listeners.vehicle;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.SblockListener;
import com.easterlyn.micromodules.FreeCart;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

/**
 * Listener for VehicleEntityCollisionEvents.
 * 
 * @author Jikoo
 */
public class EntityCollisionListener extends SblockListener {

	private final FreeCart carts;

	public EntityCollisionListener(Easterlyn plugin) {
		super(plugin);
		carts = plugin.getModule(FreeCart.class);
	}

	/**
	 * EventHandler for VehicleEntityCollisionEvents.
	 * 
	 * @param event the VehicleEntityCollisionEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
		if (event.getVehicle().getType() != EntityType.MINECART) {
			return;
		}
		if (carts.isFreeCart((Minecart) event.getVehicle())) {
			event.setCancelled(true);
			if (event.getEntity().getType() == EntityType.MINECART && event.getEntity().getPassenger() == null) {
				event.getEntity().remove();
			}
		}
	}

}
