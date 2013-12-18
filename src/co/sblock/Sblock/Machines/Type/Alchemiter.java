package co.sblock.Sblock.Machines.Type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * 
 * @author Jikoo
 */
public class Alchemiter extends Machine {

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#Machine(Location, String, Direction)
	 */
	public Alchemiter(Location l, String data, Direction d) {
		super(l, data, d);
		ItemStack is = new ItemStack(Material.QUARTZ_BLOCK);
		is.setDurability((short) 1);
		shape.addBlock(new Vector(0, 0, 1), is);
		shape.addBlock(new Vector(1, 0, 1), is);
		shape.addBlock(new Vector(1, 0, 0), is);
		is = new ItemStack(Material.QUARTZ_BLOCK);
		is.setDurability((short) 2);
		shape.addBlock(new Vector(0, 0, 2), is);
		is = new ItemStack(Material.NETHER_FENCE);
		shape.addBlock(new Vector(0, 1, 2), is);
		shape.addBlock(new Vector(0, 2, 2), is);
		shape.addBlock(new Vector(0, 3, 2), is);
		shape.addBlock(new Vector(0, 3, 1), is);
		is = new ItemStack(Material.QUARTZ_STAIRS);
		is.setDurability(Direction.NORTH.getStairByte());
		shape.addBlock(new Vector(-1, 0, -1), is);
		shape.addBlock(new Vector(0, 0, -1), is);
		shape.addBlock(new Vector(1, 0, -1), is);
		shape.addBlock(new Vector(2, 0, -1), is);
		is = new ItemStack(Material.QUARTZ_STAIRS);
		is.setDurability(Direction.SOUTH.getStairByte());
		shape.addBlock(new Vector(-1, 0, 2), is);
		shape.addBlock(new Vector(1, 0, 2), is);
		shape.addBlock(new Vector(2, 0, 2), is);
		is = new ItemStack(Material.QUARTZ_STAIRS);
		is.setDurability(Direction.WEST.getStairByte());
		shape.addBlock(new Vector(-1, 0, 1), is);
		shape.addBlock(new Vector(-1, 0, 0), is);
		is = new ItemStack(Material.QUARTZ_STAIRS);
		is.setDurability(Direction.EAST.getStairByte());
		shape.addBlock(new Vector(2, 0, 1), is);
		shape.addBlock(new Vector(2, 0, 0), is);
		blocks = shape.getBuildLocations(getFacingDirection());
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.ALCHEMITER;
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
