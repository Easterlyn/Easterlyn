package co.sblock.Sblock.Machines.Type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @author Jikoo
 */
public class Transmaterializer extends Machine {

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#Machine(Location, String, Direction)
	 */
	public Transmaterializer(Location l, String data, Direction d) {
		super(l, data, d);
		ItemStack is = new ItemStack(Material.QUARTZ_BLOCK);
		shape.addBlock(new Vector(-1, 0, 1), is);
		shape.addBlock(new Vector(0, 0, 1), is);
		shape.addBlock(new Vector(1, 0, 1), is);
		shape.addBlock(new Vector(-1, 2, 1), is);
		shape.addBlock(new Vector(0, 2, 1), is);
		shape.addBlock(new Vector(1, 2, 1), is);
		is = new ItemStack(Material.QUARTZ_BLOCK);
		is.setDurability((short) 2);
		shape.addBlock(new Vector(0, 1, 1), is);
		is = new ItemStack(Material.WOOD_BUTTON);
		is.setDurability(d.getButtonByte());
		shape.addBlock(new Vector(-1, 2, 0), is);
		shape.addBlock(new Vector(1, 2, 0), is);
		is = new ItemStack(Material.STEP);
		is.setDurability((short) 7);
		shape.addBlock(new Vector(-1, 0, -1), is);
		shape.addBlock(new Vector(0, 0, -1), is);
		shape.addBlock(new Vector(1, 0, -1), is);
		is = new ItemStack(Material.NETHER_FENCE);
		shape.addBlock(new Vector(-1, 1, 1), is);
		shape.addBlock(new Vector(1, 1, 1), is);
		is = new ItemStack(Material.WOOL);
		is.setDurability((short) 14);
		shape.addBlock(new Vector(-1, 0, 0), is);
		is = new ItemStack(Material.WOOL);
		is.setDurability((short) 5);
		shape.addBlock(new Vector(1, 0, 0), is);
		is = new ItemStack(Material.CHEST);
		is.setDurability((short) (d.getChestByte()));
		shape.addBlock(new Vector(-1, 1, 0), is);
		shape.addBlock(new Vector(1, 1, 0), is);
		blocks = shape.getBuildLocations(getFacingDirection());
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.TRANSMATERIALIZER;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
	 */
	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#postAssemble()
	 */
	@Override
	protected void postAssemble() {
	}

}
