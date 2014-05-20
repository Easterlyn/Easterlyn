package co.sblock.events.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import co.sblock.utilities.minecarts.FreeCart;

/**
 * Listener for VehicleDestroyEvents.
 * 
 * @author Jikoo
 */
public class VehicleDestroyListener implements Listener {

	/**
	 * EventHandler for VehicleDestroyEvents.
	 * 
	 * @param event the VehicleDestroyEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		if (event.getVehicle().getType() == EntityType.MINECART) {
			FreeCart.getInstance().remove((Minecart) event.getVehicle());
		}
	}
}
