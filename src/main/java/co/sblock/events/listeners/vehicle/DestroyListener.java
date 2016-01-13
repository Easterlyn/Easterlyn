package co.sblock.events.listeners.vehicle;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.micromodules.FreeCart;

/**
 * Listener for VehicleDestroyEvents.
 * 
 * @author Jikoo
 */
public class DestroyListener extends SblockListener {

	private final FreeCart carts;

	public DestroyListener(Sblock plugin) {
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
