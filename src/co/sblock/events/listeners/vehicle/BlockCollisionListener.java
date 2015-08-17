package co.sblock.events.listeners.vehicle;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import co.sblock.micromodules.FreeCart;

/**
 * Listener for VehicleBlockCollisionEvents.
 * 
 * @author Jikoo
 */
public class BlockCollisionListener implements Listener {

	/**
	 * Minecarts are automatically placed in dispensers upon collision.
	 * 
	 * @param event the VehicleBlockCollisionEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onVehicleBlockCollision(VehicleBlockCollisionEvent event) {
		if (event.getVehicle().getType() != EntityType.MINECART) {
			return;
		}
		FreeCart.getInstance().remove((Minecart) event.getVehicle());
		if (event.getVehicle().isDead()) {
			// Was a FreeCart cart.
			return;
		}
		if (event.getBlock().getType() == Material.DISPENSER || event.getBlock().getType() == Material.DROPPER) {
			BlockState b = event.getBlock().getState();
			if (((InventoryHolder) b).getInventory().firstEmpty() == -1) {
				return;
			}
			((InventoryHolder) b).getInventory().addItem(new ItemStack(Material.MINECART));
			b.update(true);
			event.getVehicle().eject();
			event.getVehicle().remove();
			return;
		}
	}
}
