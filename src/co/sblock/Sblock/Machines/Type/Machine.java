/**
 * 
 */
package co.sblock.Sblock.Machines.Type;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;

import co.sblock.Sblock.Machines.MachineModule;

/**
 * @author Jikoo
 *
 */
public abstract class Machine {

	private Location l;
	Machine(Location l) {
		this.l = l;
	}

	public Location getKey() {
		return l;
	}

	public String getLocationString() {
		return l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
	}

	public abstract String getData();
	public abstract boolean assemble();
	public abstract List<Location> getLocations();
	public abstract MachineType getType();

	public boolean handleBreak(BlockBreakEvent event) {
		if (event.getBlock().getLocation().equals(getKey())) {
			getKey().getWorld().dropItemNaturally(getKey(), getType().getUniqueDrop());
			MachineModule.getInstance().getManager().removeMachineListing(getKey());
		}
		return true;
	}
	public boolean handleGrow(BlockGrowEvent event) {
		return true;
	}
	public boolean handleFade(BlockFadeEvent event) {
		return true;
	}
	public boolean handleIgnite(BlockIgniteEvent event) {
		return true;
	}
	public boolean handlePhysics(BlockPhysicsEvent event) {
		return true;
	}
	public boolean handlePush(BlockPistonExtendEvent event) {
		return true;
	}
	public boolean handlePull(BlockPistonRetractEvent event) {
		return true;
	}
	public boolean handleSpread(BlockSpreadEvent event) {
		return true;
	}
}
