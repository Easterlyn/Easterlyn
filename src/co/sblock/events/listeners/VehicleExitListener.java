package co.sblock.events.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;

import co.sblock.events.packets.ParticleUtils;
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
	public void onVehicleExit(VehicleExitEvent event) {
		Entity entity = event.getVehicle();
		if (entity.getType() == EntityType.HORSE) {
			ParticleUtils.getInstance().removeAllEffects(entity);
			return;
		}
		if (entity.getType() == EntityType.MINECART) {
			FreeCart.getInstance().remove((Minecart) event.getVehicle());
			return;
		}
	}
}
