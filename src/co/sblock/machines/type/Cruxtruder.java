package co.sblock.machines.type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import co.sblock.machines.utilities.MachineType;
import co.sblock.machines.utilities.Direction;
import co.sblock.users.ProgressionState;
import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.captcha.CruxiteDowel;
import co.sblock.utilities.progression.Entry;

/**
 * Simulate a Sburb Cruxtender in Minecraft.
 * 
 * @author Jikoo
 */
public class Cruxtruder extends Machine {

	/**
	 * @see co.sblock.machines.type.Machine#Machine(Location, String)
	 */
	@SuppressWarnings("deprecation")
	public Cruxtruder(Location l, String owner) {
		super(l, owner);
		MaterialData m = new MaterialData(169); // Sea Lantern
		shape.addBlock(new Vector(0, 0, 0), m);
		shape.addBlock(new Vector(0, 1, 0), m);
		m = new MaterialData(Material.QUARTZ_STAIRS, Direction.NORTH.getStairByte());
		shape.addBlock(new Vector(1, 0, -1), m);
		shape.addBlock(new Vector(0, 0, -1), m);
		shape.addBlock(new Vector(-1, 0, -1), m);
		m = new MaterialData(Material.QUARTZ_STAIRS, Direction.WEST.getStairByte());
		shape.addBlock(new Vector(1, 0, 0), m);
		m = new MaterialData(Material.QUARTZ_STAIRS, Direction.EAST.getStairByte());
		shape.addBlock(new Vector(-1, 0, 0), m);
		m = new MaterialData(Material.QUARTZ_STAIRS, Direction.SOUTH.getStairByte());
		shape.addBlock(new Vector(1, 0, 1), m);
		shape.addBlock(new Vector(0, 0, 1), m);
		shape.addBlock(new Vector(-1, 0, 1), m);
		blocks = shape.getBuildLocations(Direction.NORTH);
	}

	/**
	 * @see co.sblock.machines.type.Machine#handleBreak(BlockBreakEvent)
	 */
	public boolean handleBreak(BlockBreakEvent event) {
		if (this.key.clone().add(new Vector(0, 1, 0)).equals(event.getBlock().getLocation())) {
			User user = UserManager.getUser(event.getPlayer().getUniqueId());
			if (Entry.getEntry().canStart(user)) {
				Entry.getEntry().startEntry(user, event.getBlock().getLocation());
			}
			if (user.getProgression() != ProgressionState.NONE || Entry.getEntry().isEntering(user)) {
				event.getBlock().setType(Material.GLASS);
			} else {
				return true;
			}
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), CruxiteDowel.getDowel());
		} else {
			super.handleBreak(event);
		}
		return true;
	}

	/**
	 * @see co.sblock.machines.type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.CRUXTRUDER;
	}

	/**
	 * @see co.sblock.machines.type.Machine#handleInteract(PlayerInteractEvent)
	 */
	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		return false;
	}
}
