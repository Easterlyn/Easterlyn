package co.sblock.events.listeners.inventory;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for InventoryOpenEvents.
 * 
 * @author Jikoo
 */
public class InventoryOpenListener extends SblockListener {

	private final Machines machines;

	public InventoryOpenListener(Sblock plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		Inventory inv = event.getInventory();
		if (!(inv.getHolder() instanceof BlockState)) {
			return;
		}

		Location location;
		try {
			location = inv.getLocation();
		} catch (AbstractMethodError e) {
			// reported bug, SPIGOT-2248
			return;
		}
		if (location == null) {
			return;
		}
		Pair<Machine, ConfigurationSection> machine = machines.getMachineByLocation(location);
		if (machine != null) {
			event.setCancelled(machine.getLeft().handleOpen(event, machine.getRight()));
		}
	}
}
