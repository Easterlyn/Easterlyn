package co.sblock.Sblock.Events.Listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for VehicleBlockCollisionEvents.
 * 
 * @author Jikoo
 */
public class VehicleBlockCollisionListener implements Listener {

	/**
	 * Minecarts are automatically placed in dispensers upon collision.
	 * 
	 * @param event the VehicleBlockCollisionEvent
	 */
	@EventHandler
	public void onVehicleBlockCollisionEvent(VehicleBlockCollisionEvent event) {
		if (event.getVehicle().getType() != EntityType.MINECART) {
			return;
		}
		if ( event.getBlock().getType() == Material.DISPENSER) {
			Block b = event.getBlock();
			Dispenser disp = (Dispenser)b.getState();
			disp.getInventory().addItem(new ItemStack(Material.MINECART));
			event.getVehicle().eject();
			event.getVehicle().remove();
			return;
		}
		if ( event.getBlock().getType() == Material.DROPPER) {
			Block b = event.getBlock();
			Dropper drop = (Dropper)b.getState();
			if (drop.getInventory().firstEmpty() == -1) {
				return; // We only use infinite dispensers, not hoppers.
			}
			drop.getInventory().addItem(new ItemStack(Material.MINECART));
			event.getVehicle().eject();
			event.getVehicle().remove();
			return;
		}
	}
}
