package com.easterlyn.machines.type;

import com.easterlyn.Easterlyn;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.utilities.Shape;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * For Zack, with love.
 *
 * @author Jikoo
 */
public class PBO extends Machine {

	private final ItemStack drop;

	public PBO(Easterlyn plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Perfect Building Object");
		getShape().setVectorData(new Vector(0, 0, 0), Material.DIAMOND_BLOCK);
		drop = new ItemStack(Material.DIAMOND_BLOCK);
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Perfect Building Object");
		drop.setItemMeta(meta);
	}

	@Override
	public void assemble(final BlockPlaceEvent event, ConfigurationSection storage) {
		new BukkitRunnable() {
			@Override
			public void run() {
				event.getBlock().setType(event.getBlockAgainst().getType(), false);
				BlockState against = event.getBlockAgainst().getState();
				BlockState state = event.getBlock().getState();
				state.setData(against.getData());
				state.update(true, false);
				getMachines().deleteMachine(event.getBlock().getLocation());
			}
		}.runTask(getPlugin());
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}

}
