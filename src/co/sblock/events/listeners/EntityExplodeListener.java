package co.sblock.events.listeners;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.nitnelave.CreeperHeal.config.CreeperConfig;

import co.sblock.machines.MachineManager;
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
		if (Bukkit.getPluginManager().isPluginEnabled("CreeperHeal")
				&& CreeperConfig.getWorld(event.getLocation().getWorld().getName()).shouldReplace(
						event.getEntity())) {

			HashSet<Block> affected = new HashSet<>();
			for (Block b : event.blockList().toArray(new Block[0])) {
				Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(b);
				if (m != null) {
					affected.add(b);
				}
			}

			MachineManager.getManager().addBlock(affected.toArray(new Block[0]));

			return;
		}

		// CreeperHeal is not set to heal whatever destroyed this machine. Prevent damage.
		for (Block b : event.blockList().toArray(new Block[0])) {
			Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(b);
			if (m != null) {
				event.blockList().remove(b);
			}
		}
	}
}
