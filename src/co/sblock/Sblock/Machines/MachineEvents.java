/**
 * 
 */
package co.sblock.Sblock.Machines;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock.Machines.Type.Machine;
import co.sblock.Sblock.Machines.Type.MachineType;

/**
 * @author Jikoo
 *
 */
public class MachineEvents implements Listener {

	private MachineManager m = MachineModule.getInstance().getManager();

	@EventHandler(ignoreCancelled=true)
	public void build(BlockPlaceEvent event) {
		for (MachineType mt : MachineType.values()) {
			ItemStack is = mt.getUniqueDrop();
			is.setAmount(event.getItemInHand().getAmount());
			if (is.equals(event.getItemInHand())) {
				m.addMachine(event.getBlock().getLocation(), mt, mt.getData(event)).assemble(event);
				break;
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void handleBreak(BlockBreakEvent event) {
		Machine machine = m.getMachineByBlock(event.getBlock());
		if (machine != null) {
			event.setCancelled(machine.handleBreak(event));
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void handleGrow(BlockGrowEvent event) {
		Machine machine = m.getMachineByBlock(event.getBlock());
		if (machine != null) {
			event.setCancelled(machine.handleGrow(event));
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void handleFade(BlockFadeEvent event) {
		Machine machine = m.getMachineByBlock(event.getBlock());
		if (machine != null) {
			event.setCancelled(machine.handleFade(event));
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void handleIgnite(BlockIgniteEvent event) {
		Machine machine = m.getMachineByBlock(event.getBlock());
		if (machine != null) {
			event.setCancelled(machine.handleIgnite(event));
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void handlePhysics(BlockPhysicsEvent event) {
		Machine machine = m.getMachineByBlock(event.getBlock());
		if (machine != null) {
			event.setCancelled(machine.handlePhysics(event));
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void handlePush(BlockPistonExtendEvent event) {
		Machine machine = m.getMachineByBlock(event.getBlock());
		if (machine != null) {
			event.setCancelled(machine.handlePush(event));
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void handlePull(BlockPistonRetractEvent event) {
		Machine machine = m.getMachineByBlock(event.getBlock());
		if (machine != null) {
			event.setCancelled(machine.handlePull(event));
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void handleSpread(BlockSpreadEvent event) {
		Machine machine = m.getMachineByBlock(event.getBlock());
		if (machine != null) {
			event.setCancelled(machine.handleSpread(event));
		}
	}
}
