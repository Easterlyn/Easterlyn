package co.sblock.Sblock.Events.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;

import co.sblock.Sblock.Machines.SblockMachines;
import co.sblock.Sblock.Machines.Type.Machine;

/**
 * Listener for FurnaceBurnEvents.
 * 
 * @author Jikoo
 */
public class FurnaceBurnListener implements Listener {

	/**
	 * EventHandler for when fuel is consumed by a furnace.
	 * 
	 * @param event the FurnaceBurnEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onFurnaceBurn(FurnaceBurnEvent event) {
		Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handleBurnFuel(event));
		}
	}
}
