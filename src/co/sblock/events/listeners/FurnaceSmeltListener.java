package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Machine;

/**
 * Listener for FurnaceSmeltEvents.
 * 
 * @author Jikoo
 */
public class FurnaceSmeltListener implements Listener {

	/**
	 * EventHandler for FurnaceSmeltEvents.
	 * 
	 * @param event the FurnaceSmeltEvent
	 */
	@EventHandler
	public void onFurnaceSmelt(FurnaceSmeltEvent event) {

		Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handleFurnaceSmelt(event));
		}
	}
}
