package co.sblock.events.listeners.vehicle;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import co.sblock.micromodules.FreeCart;

/**
 * Listener for VehicleDestroyEvents.
 * 
 * @author Jikoo
 */
public class DestroyListener implements Listener {

	/**
	 * EventHandler for VehicleDestroyEvents.
	 * 
	 * @param event the VehicleDestroyEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		if (event.getVehicle().getType() == EntityType.MINECART && FreeCart.getInstance().isFreeCart((Minecart) event.getVehicle())) {
			if (event.getAttacker() == null) {
				FreeCart.getInstance().remove((Minecart) event.getVehicle());
			} else {
				event.setCancelled(true);
			}
		}
	}
}
