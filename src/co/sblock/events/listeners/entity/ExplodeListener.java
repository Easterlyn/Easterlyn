package co.sblock.events.listeners.entity;

import java.util.HashSet;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.nitnelave.CreeperHeal.config.CreeperConfig;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

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

			HashSet<Block> affected = new HashSet<>();
			for (Block block : event.blockList()) {
				Pair<Machine, ConfigurationSection> pair = Machines.getInstance().getMachineByBlock(block);
				if (pair != null) {
					affected.add(block);
				}
			}

			Machines.getInstance().addExplodedBlock(affected.toArray(new Block[0]));
			return;
		}

		// CreeperHeal is not set to heal whatever destroyed this machine. Prevent damage.
		for (Block block : event.blockList().toArray(new Block[0])) {
			Pair<Machine, ConfigurationSection> pair = Machines.getInstance().getMachineByBlock(block);
			if (pair != null) {
				event.blockList().remove(block);
			}
		}
	}
}
