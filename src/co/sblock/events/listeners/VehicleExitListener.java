package co.sblock.events.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;

import co.sblock.utilities.minecarts.FreeCart;

/**
 * Listener for VehicleExitEvents.
 * 
 * @author Jikoo
 */
public class VehicleExitListener implements Listener {

	/**
	 * EventHandler for VehicleExitEvents.
	 * 
	 * @param event the VehicleExitEvent
	 */
	@EventHandler
	public void onVehicleDestroy(VehicleExitEvent event) {
		if (event.getVehicle().getType() == EntityType.MINECART) {
			FreeCart.getInstance().remove((Minecart) event.getVehicle());
		}
	}
}
