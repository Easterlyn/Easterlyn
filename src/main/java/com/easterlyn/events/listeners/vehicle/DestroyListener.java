package com.easterlyn.events.listeners.vehicle;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.FreeCart;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

/**
 * Listener for VehicleDestroyEvents.
 * 
 * @author Jikoo
 */
public class DestroyListener extends EasterlynListener {

	private final FreeCart carts;

	public DestroyListener(Easterlyn plugin) {
		super(plugin);
		carts = plugin.getModule(FreeCart.class);
	}

	/**
	 * EventHandler for VehicleDestroyEvents.
	 * 
	 * @param event the VehicleDestroyEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		if (event.getVehicle().getType() == EntityType.MINECART && carts.isFreeCart((Minecart) event.getVehicle())) {
			if (event.getAttacker() == null) {
				carts.remove((Minecart) event.getVehicle());
			} else {
				event.setCancelled(true);
			}
		}
	}

}
