package co.sblock.Sblock.Machines.Type;

import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import co.sblock.Sblock.Machines.MachineModule;

/**
 * 
 * @author Jikoo
 */
public class Cruxtender extends Machine {

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#Machine(Location, String)
	 */
	public Cruxtender(Location l, String data) {
		super(l, data);
		ItemStack is = new ItemStack(Material.DIAMOND_BLOCK);
		shape.addBlock(new Vector(0, 1, 0), is);
		is = new ItemStack(Material.QUARTZ_STAIRS);
		is.setDurability(Direction.SOUTH.getStairByte());
		shape.addBlock(new Vector(1, 0, -1), is);
		shape.addBlock(new Vector(0, 0, -1), is);
		shape.addBlock(new Vector(-1, 0, -1), is);
		is = new ItemStack(Material.QUARTZ_STAIRS);
		is.setDurability(Direction.EAST.getStairByte());
		shape.addBlock(new Vector(1, 0, 0), is);
		is = new ItemStack(Material.QUARTZ_STAIRS);
		is.setDurability(Direction.WEST.getStairByte());
		shape.addBlock(new Vector(-1, 0, 0), is);
		is = new ItemStack(Material.QUARTZ_STAIRS);
		is.setDurability(Direction.NORTH.getStairByte());
		shape.addBlock(new Vector(1, 0, 1), is);
		shape.addBlock(new Vector(0, 0, 1), is);
		shape.addBlock(new Vector(-1, 0, 1), is);
		blocks = shape.getBuildLocations(Direction.NORTH);
	}

	/**
	 * Handles <code>Machine</code> deconstruction.
	 * 
	 * @param event
	 *            the <code>BlockBreakEvent</code>
	 * @return true if event should be cancelled
	 */
	public boolean handleBreak(BlockBreakEvent event) {
		if (this.meetsAdditionalBreakConditions(event) || event.getPlayer().hasPermission("group.denizen")) {
			if (this.getKey().add(new Vector(0, 1, 0)).equals(event.getBlock().getLocation())) {
				if (event.getBlock().getType().equals(Material.DIAMOND_BLOCK)) {
					event.getBlock().setType(Material.BEACON);
				}
				event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), Icon.DOWEL.getIcon());
				return true;
			} else {
				if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
					getKey().getWorld().dropItemNaturally(getKey(), getType().getUniqueDrop());
				}
				for (Location l : this.getLocations()) {
					l.getBlock().setType(Material.AIR);
				}
				getKey().getBlock().setType(Material.AIR);
				MachineModule.getInstance().getManager().removeMachineListing(getKey());
			}
		}
		return true;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#assemble(BlockPlaceEvent)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void assemble(BlockPlaceEvent event) {
		for (Location l : blocks.keySet()) {
			if (!l.getBlock().isEmpty()) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "There isn't enough space to build this Machine here.");
				this.assemblyFailed();
				return;
			}
		}
		for (Entry<Location, ItemStack> e : blocks.entrySet()) {
			Block b = e.getKey().getBlock();
			b.setType(e.getValue().getType());
			b.setData(e.getValue().getData().getData());
		}
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.CRUXTRUDER;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
	 */
	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		return false;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#postAssemble()
	 */
	@Override
	protected void postAssemble() {
		this.l.getBlock().setType(Material.DIAMOND_BLOCK);
	}
}
