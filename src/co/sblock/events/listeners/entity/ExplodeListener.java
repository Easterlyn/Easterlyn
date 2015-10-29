package co.sblock.events.listeners.entity;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.nitnelave.CreeperHeal.config.CreeperConfig;

import co.sblock.machines.Machines;

/**
 * Listener for EntityExplodeEvents.
 * 
 * @author Jikoo
 */
public class ExplodeListener implements Listener {

	/**
	 * EventHandler for EntityExplodeEvents.
	 * 
	 * @param event the EntityExplodeEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (Bukkit.getPluginManager().isPluginEnabled("CreeperHeal")
				&& CreeperConfig.getWorld(event.getLocation().getWorld().getName()).shouldReplace(
						event.getEntity())) {

			ArrayList<Block> affected = new ArrayList<>();
			for (Block block : event.blockList()) {
				if (Machines.getInstance().getMachineByBlock(block) != null) {
					affected.add(block);
				}
			}

			Machines.getInstance().addExplodedBlock(affected);
			return;
		}

		// CreeperHeal is not set to heal whatever destroyed this machine. Prevent damage.
		Iterator<Block> iterator = event.blockList().iterator();
		while (iterator.hasNext()) {
			Block block = iterator.next();
			if (Machines.getInstance().getMachineByBlock(block) != null) {
				iterator.remove();
			}
		}
	}
}
