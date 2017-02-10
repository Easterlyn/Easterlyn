package com.easterlyn.events.listeners.vehicle;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.FreeCart;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

/**
 * Listener for VehicleEntityCollisionEvents.
 * 
 * @author Jikoo
 */
public class EntityCollisionListener extends EasterlynListener {

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
		if (!(event.getVehicle() instanceof Minecart)) {
			return;
		}
		if (carts.isFreeCart((Minecart) event.getVehicle())) {
			event.setCancelled(true);
			if (event.getVehicle().getPassengers().isEmpty()) {
				event.getVehicle().remove();
			}
		}
	}

}
