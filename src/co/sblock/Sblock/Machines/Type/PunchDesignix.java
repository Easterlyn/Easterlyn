/**
 * 
 */
package co.sblock.Sblock.Machines.Type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @author Maryanne
 *
 */
public class PunchDesignix extends Machine {

	/**
	 * @param l
	 * @param data
	 * @param d
	 */
	PunchDesignix(Location l, String data, Direction d) {
		super(l, data, d);
		ItemStack is = new ItemStack(Material.STEP);
		is.setDurability((short) 15);
		shape.addBlock(new Vector(0, 0, 0), is);
		shape.addBlock(new Vector(1, 0, 0), is);
		is = new ItemStack(Material.CARPET);
		is.setDurability((short) 8);
		shape.addBlock(new Vector(0, 1, 0), is);
		shape.addBlock(new Vector(1, 1, 0), is);
		is = new ItemStack(Material.QUARTZ_STAIRS);
		is.setDurability(Direction.WEST.getUpperStairByte());
		shape.addBlock(new Vector(0, 1, 0), is);
		is = new ItemStack(Material.QUARTZ_STAIRS);
		is.setDurability(Direction.EAST.getUpperStairByte());
		shape.addBlock(new Vector(1, 1, 0), is);
		is = new ItemStack(Material.QUARTZ_STAIRS);
		is.setDurability(Direction.NORTH.getStairByte());
		shape.addBlock(new Vector(0, 1, 1), is);
		shape.addBlock(new Vector(1, 1, 1), is);
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Machines.Type.Machine#meetsAdditionalBreakConditions(org.bukkit.event.block.BlockBreakEvent)
	 */
	@Override
	public boolean meetsAdditionalBreakConditions(BlockBreakEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Machines.Type.Machine#assemble(org.bukkit.event.block.BlockPlaceEvent)
	 */
	@Override
	public void assemble(BlockPlaceEvent event) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		// TODO Auto-generated method stub
		return null;
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
