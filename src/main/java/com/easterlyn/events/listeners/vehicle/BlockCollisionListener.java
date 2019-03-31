package com.easterlyn.events.listeners.vehicle;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.FreeCart;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for VehicleBlockCollisionEvents.
 *
 * @author Jikoo
 */
public class BlockCollisionListener extends EasterlynListener {

	private final FreeCart carts;

	public BlockCollisionListener(Easterlyn plugin) {
		super(plugin);
		carts = plugin.getModule(FreeCart.class);
	}

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
		carts.remove((Minecart) event.getVehicle());
		if (event.getVehicle().isDead()) {
			// Was a FreeCart cart.
			return;
		}
		if (event.getBlock().getType() == Material.DISPENSER || event.getBlock().getType() == Material.DROPPER) {
			BlockState blockState = event.getBlock().getState();
			if (!(blockState instanceof Container)) {
				return;
			}
			Container dispenser = (Container) blockState;
			if (dispenser.getSnapshotInventory().firstEmpty() == -1) {
				return;
			}
			dispenser.getSnapshotInventory().addItem(new ItemStack(Material.MINECART));
			dispenser.update(true);
			event.getVehicle().eject();
			event.getVehicle().remove();
		}
	}

}
