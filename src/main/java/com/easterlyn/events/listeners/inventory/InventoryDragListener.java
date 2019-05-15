package com.easterlyn.events.listeners.inventory;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;
import com.easterlyn.utilities.InventoryUtils;

import com.easterlyn.utilities.tuple.Pair;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

/**
 * Listener for InventoryDragEvents.
 *
 * @author Jikoo
 */
public class InventoryDragListener extends EasterlynListener {

	private final Machines machines;

	public InventoryDragListener(Easterlyn plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent event) {
		InventoryHolder ih = event.getView().getTopInventory().getHolder();

		// Finds inventories of physical blocks opened by Machines
		if (ih instanceof BlockState) {
			Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(((BlockState) ih).getBlock());
			if (pair != null) {
				event.setCancelled(pair.getLeft().handleClick(event, pair.getRight()));
			}
		}

		// Finds inventories forcibly opened by Machines
		Pair<Machine, ConfigurationSection> pair = machines.getInventoryTracker().getOpenMachine((Player) event.getWhoClicked());
		if (pair != null) {
			event.setCancelled(pair.getLeft().handleClick(event, pair.getRight()));
			return;
		}

		// Lowest priority Machine check, one with no identifying block
		if (ih instanceof Machine) {
			event.setCancelled(((Machine) ih).handleClick(event, null));
			return;
		}

		// No putting special Easterlyn items into anvils, it'll ruin them.
		if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
				&& InventoryUtils.isUniqueItem(getPlugin(), event.getOldCursor())) {
			event.setCancelled(true);
		}
	}

}
