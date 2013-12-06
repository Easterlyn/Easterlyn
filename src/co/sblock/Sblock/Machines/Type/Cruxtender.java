package co.sblock.Sblock.Machines.Type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @author Jikoo
 *
 */
public class Cruxtender extends Machine {

	/**
	 * @param l
	 * @param data
	 */
	Cruxtender(Location l, String data) {
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
