package co.sblock.Sblock.Machines.Type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/**
 * Machine for ItemStack teleportation.
 * 
 * @author Jikoo
 */
public class Transmaterializer extends Machine {

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#Machine(Location, String, Direction)
	 */
	@SuppressWarnings("deprecation")
	public Transmaterializer(Location l, String data, Direction d) {
		super(l, data, d);
		MaterialData m = new MaterialData(Material.CHEST, d.getChestByte());
		shape.addBlock(new Vector(0, 0, 0), m);
		m = new MaterialData(Material.QUARTZ_BLOCK);
		shape.addBlock(new Vector(-1, 0, 1), m);
		shape.addBlock(new Vector(0, 0, 1), m);
		shape.addBlock(new Vector(1, 0, 1), m);
		shape.addBlock(new Vector(-1, 2, 1), m);
		shape.addBlock(new Vector(0, 2, 1), m);
		shape.addBlock(new Vector(1, 2, 1), m);
		m = new MaterialData(Material.QUARTZ_BLOCK, (byte) 2);
		shape.addBlock(new Vector(0, 1, 1), m);
		m = new MaterialData(Material.WOOD_BUTTON, d.getButtonByte());
		shape.addBlock(new Vector(-1, 2, 0), m);
		shape.addBlock(new Vector(1, 2, 0), m);
		m = new MaterialData(Material.STEP, (byte) 7);
		shape.addBlock(new Vector(-1, 0, -1), m);
		shape.addBlock(new Vector(0, 0, -1), m);
		shape.addBlock(new Vector(1, 0, -1), m);
		m = new MaterialData(Material.NETHER_FENCE);
		shape.addBlock(new Vector(-1, 1, 1), m);
		shape.addBlock(new Vector(1, 1, 1), m);
		m = new MaterialData(Material.WOOL, (byte) 14);
		shape.addBlock(new Vector(-1, 0, 0), m);
		m = new MaterialData(Material.WOOL, (byte) 5);
		shape.addBlock(new Vector(1, 0, 0), m);
		m = new MaterialData(Material.CHEST, (d.getChestByte()));
		shape.addBlock(new Vector(-1, 1, 0), m);
		shape.addBlock(new Vector(1, 1, 0), m);
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
}
