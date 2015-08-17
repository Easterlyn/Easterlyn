package co.sblock.events.listeners.vehicle;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import co.sblock.micromodules.FreeCart;

/**
 * Listener for VehicleEntityCollisionEvents.
 * 
 * @author Jikoo
 */
public class EntityCollisionListener implements Listener {

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
		if (FreeCart.getInstance().isFreeCart((Minecart) event.getVehicle())) {
			event.setCancelled(true);
			if (event.getEntity().getType() == EntityType.MINECART && event.getEntity().getPassenger() == null) {
				event.getEntity().remove();
			}
		}
	}
}
