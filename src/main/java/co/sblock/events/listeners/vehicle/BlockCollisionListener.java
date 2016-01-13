package co.sblock.events.listeners.vehicle;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.micromodules.FreeCart;

/**
 * Listener for VehicleBlockCollisionEvents.
 * 
 * @author Jikoo
 */
public class BlockCollisionListener extends SblockListener {

	private final FreeCart carts;

	public BlockCollisionListener(Sblock plugin) {
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
