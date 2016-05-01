package co.sblock.events.listeners.inventory;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;

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
		Location location;
		try {
			location = event.getInventory().getLocation();
		} catch (AbstractMethodError e) {
			System.err.println("Caught AbstractMethodError calling Inventory#getLocation on class " + event.getInventory().getClass().getName());
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
