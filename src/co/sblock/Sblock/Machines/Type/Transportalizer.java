package co.sblock.Sblock.Machines.Type;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock.Machines.MachineModule;
import co.sblock.Sblock.Machines.Type.Shape.Direction;

/**
 * @author Jikoo
 *
 */
public class Transportalizer extends Machine {

	/**
	 * @param l
	 * @param data
	 */
	public Transportalizer(Location l, String data, Direction d) {
		super(l, data, d);
		ItemStack is = new ItemStack(Material.STEP);
		is.setDurability((short) 7);
		shape.addBlock(new Location(this.l.getWorld(), -1, 0, -1), is);
		shape.addBlock(new Location(l.getWorld(), 0, 0, -1), is);
		shape.addBlock(new Location(this.l.getWorld(), 1, 0, -1), is);
		is = new ItemStack(Material.QUARTZ_BLOCK);
		shape.addBlock(new Location(this.l.getWorld(), -1, 0, 0), is);
		shape.addBlock(new Location(this.l.getWorld(), 1, 0, 0), is);
		shape.addBlock(new Location(this.l.getWorld(), -1, 0, 1), is);
		shape.addBlock(new Location(this.l.getWorld(), 0, 0, 1), is);
		shape.addBlock(new Location(this.l.getWorld(), 1, 0, 1), is);
		shape.addBlock(new Location(this.l.getWorld(), -1, 2, 1), is);
		shape.addBlock(new Location(this.l.getWorld(), 0, 2, 1), is);
		shape.addBlock(new Location(this.l.getWorld(), 1, 2, 1), is);
		is.setDurability((short) 2);
		shape.addBlock(new Location(this.l.getWorld(), 0, 1, 1), is);
		is = new ItemStack(Material.NETHER_FENCE);
		shape.addBlock(new Location(this.l.getWorld(), -1, 1, 1), is);
		shape.addBlock(new Location(this.l.getWorld(), 1, 1, 1), is);
		is.setType(Material.CARPET);
		is.setDurability((short) 5);
		shape.addBlock(new Location(this.l.getWorld(), -1, 1, 0), is);
		is.setDurability((short) 14);
		shape.addBlock(new Location(this.l.getWorld(), 1, 1, 0), is);
		is = new ItemStack(Material.WOOD_BUTTON);
		shape.addBlock(new Location(this.l.getWorld(), -1, 2, 0), is);
		shape.addBlock(new Location(this.l.getWorld(), 1, 2, 0), is);
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#meetsAdditionalBreakConditions(org.bukkit.event.block.BlockBreakEvent)
	 */
	@Override
	public boolean meetsAdditionalBreakConditions(BlockBreakEvent event) {
		return getData().equals(event.getPlayer().getName());
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#assemble(org.bukkit.event.block.BlockPlaceEvent)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void assemble(BlockPlaceEvent event) {
		HashMap<Location, ItemStack> assembly = shape.getBuildLocations(getFacingDirection());
		blocks = assembly.keySet();
		for (Location l : blocks) {
			if (!l.getBlock().isEmpty()) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "There isn't enough space to build this Machine here.");
				MachineModule.getInstance().getManager().removeMachineListing(l);
				return;
			}
		}
		for (Entry<Location, ItemStack> e : assembly.entrySet()) {
			Block b = e.getKey().getBlock();
			b.setType(e.getValue().getType());
			b.setData(e.getValue().getData().getData(), false);
		}
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.TRANSPORTALIZER;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleInteract(org.bukkit.event.player.PlayerInteractEvent)
	 */
	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

}
