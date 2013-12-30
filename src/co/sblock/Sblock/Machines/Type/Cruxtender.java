package co.sblock.Sblock.Machines.Type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

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
		is.setDurability(Direction.NORTH.getStairByte());
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
		is.setDurability(Direction.SOUTH.getStairByte());
		shape.addBlock(new Vector(1, 0, 1), is);
		shape.addBlock(new Vector(0, 0, 1), is);
		shape.addBlock(new Vector(-1, 0, 1), is);
		blocks = shape.getBuildLocations(Direction.NORTH);
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleBreak(BlockBreakEvent)
	 */
	public boolean handleBreak(BlockBreakEvent event) {
		if (this.meetsAdditionalBreakConditions(event)
				&& this.l.clone().add(new Vector(0, 1, 0)).equals(event.getBlock().getLocation())) {
			if (event.getBlock().getType().equals(Material.DIAMOND_BLOCK)) {
				event.getBlock().setType(Material.BEACON);
			}
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), Icon.DOWEL.getIcon());
		} else {
			super.handleBreak(event);
		}
		return true;
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
