package co.sblock.events.listeners.inventory;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.utilities.InventoryUtils;

/**
 * 
 * 
 * @author Jikoo
 */
public class InventoryDragListener extends SblockListener {

	private final Machines machines;

	public InventoryDragListener(Sblock plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	public void onInventoryDrag(InventoryDragEvent event) {
		InventoryHolder ih = event.getView().getTopInventory().getHolder();

		// Finds inventories of physical blocks opened by Machines
		if (ih != null && ih instanceof BlockState) {
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
		Machine m;
		if (ih != null && ih instanceof Machine) {
			m = (Machine) ih;
			if (m != null) {
				event.setCancelled(m.handleClick(event, null));
				return;
			}
		}

		// No putting special Sblock items into anvils, it'll ruin them.
		if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
				&& InventoryUtils.isUniqueItem(getPlugin(), event.getCursor())) {
			event.setCancelled(true);
			return;
		}
	}
}
