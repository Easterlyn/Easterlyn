package co.sblock.Sblock.Machines.Type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/**
 * Simulate a Sburb Alchemiter in Minecraft.
 * 
 * @author Jikoo
 */
public class Alchemiter extends Machine {

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#Machine(Location, String, Direction)
	 */
	@SuppressWarnings("deprecation")
	public Alchemiter(Location l, String data, Direction d) {
		super(l, data, d);
		MaterialData m = new MaterialData(Material.QUARTZ_BLOCK, (byte) 1);
		shape.addBlock(new Vector(0, 0, 0), m);
		shape.addBlock(new Vector(0, 0, 1), m);
		shape.addBlock(new Vector(1, 0, 1), m);
		shape.addBlock(new Vector(1, 0, 0), m);
		m = new MaterialData(Material.QUARTZ_BLOCK, (byte) 2);
		shape.addBlock(new Vector(0, 0, 2), m);
		m = new MaterialData(Material.NETHER_FENCE);
		shape.addBlock(new Vector(0, 1, 2), m);
		shape.addBlock(new Vector(0, 2, 2), m);
		shape.addBlock(new Vector(0, 3, 2), m);
		shape.addBlock(new Vector(0, 3, 1), m);
		m = new MaterialData(Material.QUARTZ_STAIRS, d.getStairByte());
		shape.addBlock(new Vector(-1, 0, -1), m);
		shape.addBlock(new Vector(0, 0, -1), m);
		shape.addBlock(new Vector(1, 0, -1), m);
		shape.addBlock(new Vector(2, 0, -1), m);
		m = new MaterialData(Material.QUARTZ_STAIRS,
				d.getRelativeDirection(Direction.SOUTH).getStairByte());
		shape.addBlock(new Vector(-1, 0, 2), m);
		shape.addBlock(new Vector(1, 0, 2), m);
		shape.addBlock(new Vector(2, 0, 2), m);
		m = new MaterialData(Material.QUARTZ_STAIRS,
				d.getRelativeDirection(Direction.WEST).getStairByte());
		shape.addBlock(new Vector(-1, 0, 1), m);
		shape.addBlock(new Vector(-1, 0, 0), m);
		m = new MaterialData(Material.QUARTZ_STAIRS,
				d.getRelativeDirection(Direction.EAST).getStairByte());
		shape.addBlock(new Vector(2, 0, 1), m);
		shape.addBlock(new Vector(2, 0, 0), m);
		blocks = shape.getBuildLocations(getFacingDirection());
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.ALCHEMITER;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
	 */
	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}
