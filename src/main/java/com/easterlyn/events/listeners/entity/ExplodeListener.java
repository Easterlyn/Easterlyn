package com.easterlyn.events.listeners.entity;

import java.util.ArrayList;
import java.util.Iterator;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;
import com.nitnelave.CreeperHeal.config.CreeperConfig;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * Listener for EntityExplodeEvents.
 * 
 * @author Jikoo
 */
public class ExplodeListener extends EasterlynListener {

	private final Machines machines;

	public ExplodeListener(Easterlyn plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	/**
	 * EventHandler for EntityExplodeEvents.
	 * 
	 * @param event the EntityExplodeEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (Bukkit.getPluginManager().isPluginEnabled("CreeperHeal")
				&& CreeperConfig.getWorld(event.getLocation().getWorld().getName()).shouldReplace(event.getEntity())
						&& event.getEntityType() != EntityType.ENDER_DRAGON) {

			ArrayList<Block> affected = new ArrayList<>();
			for (Block block : event.blockList()) {
				if (machines.getMachineByBlock(block) != null) {
					affected.add(block);
				}
			}

			machines.addExplodedBlock(affected);
			return;
		}

		// CreeperHeal is not set to heal whatever destroyed this machine. Prevent damage.
		Iterator<Block> iterator = event.blockList().iterator();
		while (iterator.hasNext()) {
			Block block = iterator.next();
			if (machines.getMachineByBlock(block) != null) {
				iterator.remove();
			}
		}
	}

}
