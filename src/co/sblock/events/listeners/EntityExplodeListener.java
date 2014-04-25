package co.sblock.events.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Machine;

/**
 * Listener for EntityExplodeEvents.
 * 
 * @author Jikoo
 */
public class EntityExplodeListener implements Listener {

	/**
	 * EventHandler for EntityExplodeEvents.
	 * 
	 * @param event the EntityExplodeEvent
	 */
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if (Bukkit.getPluginManager().isPluginEnabled("CreeperHeal")) {
			// Machine protection will be handled in CreeperHeal's custom events.
			return;
		}
		for (Block b : event.blockList().toArray(new Block[0])) {
			Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(b);
			if (m != null) {
				event.blockList().remove(b);
			}
		}
	}
}
