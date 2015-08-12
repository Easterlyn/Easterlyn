package co.sblock.machines.type;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.machines.Machines;
import co.sblock.machines.utilities.Shape;

import net.md_5.bungee.api.ChatColor;

/**
 * For Zack, with love.
 * 
 * @author Jikoo
 */
public class PBO extends Machine {

	private final ItemStack drop;

	PBO() {
		super(new Shape());
		drop = new ItemStack(Material.DIAMOND_BLOCK);
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Perfect Building Object");
		drop.setItemMeta(meta);
	}

	@Override
	public boolean meetsAdditionalBreakConditions(BlockBreakEvent event, ConfigurationSection storage) {
		return true;
	}

	@Override
	public void assemble(final BlockPlaceEvent event, ConfigurationSection storage) {
		new BukkitRunnable() {
			@Override
			public void run() {
				BlockState against = event.getBlockAgainst().getState();
				BlockState state = event.getBlock().getState();
				state.setData(against.getData());
				Machines.getInstance().deleteMachine(event.getBlock().getLocation());
			}
		}.runTask(Sblock.getInstance());
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}
}
