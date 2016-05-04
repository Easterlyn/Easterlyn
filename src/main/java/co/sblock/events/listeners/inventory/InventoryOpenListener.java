package co.sblock.events.listeners.inventory;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Location;
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
		Location location;
		try {
			location = inv.getLocation();
		} catch (AbstractMethodError e) {
			System.err.println(inv.getClass().getName());
			System.err.println(new StringBuilder("Caught AbstractMethodError calling Inventory#getLocation on class ")
					.append(inv.getClass().getName()).append("\nType: ").append(inv.getType())
					.append("\nTitle: ").append(inv.getTitle()).append("\nHolder: ").append(inv.getHolder()));
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
