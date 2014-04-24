package co.sblock.machines.type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import co.sblock.users.User;
import co.sblock.utilities.captcha.CruxiteDowel;
import co.sblock.utilities.progression.Entry;

/**
 * Simulate a Sburb Cruxtender in Minecraft.
 * 
 * @author Jikoo
 */
public class Cruxtruder extends Machine {

	/**
	 * @see co.sblock.Machines.Type.Machine#Machine(Location, String)
	 */
	@SuppressWarnings("deprecation")
	public Cruxtruder(Location l, String data) {
		super(l, data);
		MaterialData m = new MaterialData(Material.DIAMOND_BLOCK);
		shape.addBlock(new Vector(0, 0, 0), m);
		shape.addBlock(new Vector(0, 1, 0), m);
		m = new MaterialData(Material.QUARTZ_STAIRS, Direction.NORTH.getStairByte());
		shape.addBlock(new Vector(1, 0, -1), m);
		shape.addBlock(new Vector(0, 0, -1), m);
		shape.addBlock(new Vector(-1, 0, -1), m);
		m = new MaterialData(Material.QUARTZ_STAIRS, Direction.EAST.getStairByte());
		shape.addBlock(new Vector(1, 0, 0), m);
		m = new MaterialData(Material.QUARTZ_STAIRS, Direction.WEST.getStairByte());
		shape.addBlock(new Vector(-1, 0, 0), m);
		m = new MaterialData(Material.QUARTZ_STAIRS, Direction.SOUTH.getStairByte());
		shape.addBlock(new Vector(1, 0, 1), m);
		shape.addBlock(new Vector(0, 0, 1), m);
		shape.addBlock(new Vector(-1, 0, 1), m);
		blocks = shape.getBuildLocations(Direction.NORTH);
	}

	/**
	 * @see co.sblock.Machines.Type.Machine#handleBreak(BlockBreakEvent)
	 */
	public boolean handleBreak(BlockBreakEvent event) {
		if ((event.getPlayer().hasPermission("group.denizen")
				|| getData().equals(event.getPlayer().getUniqueId().toString()))
				&& this.l.clone().add(new Vector(0, 1, 0)).equals(event.getBlock().getLocation())) {
			if (event.getBlock().getType().equals(Material.DIAMOND_BLOCK)) {
				if (Entry.getEntry().canStart(User.getUser(event.getPlayer().getUniqueId()))) {
					event.getBlock().setType(Material.GLASS);
					Entry.getEntry().startEntry(User.getUser(event.getPlayer().getUniqueId()), event.getBlock().getLocation());
				} else {
					return true;
				}
			}
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), CruxiteDowel.getDowel());
		} else {
			super.handleBreak(event);
		}
		return true;
	}

	/**
	 * @see co.sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.CRUXTRUDER;
	}

	/**
	 * @see co.sblock.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
	 */
	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		return false;
	}
}
