package com.easterlyn.events.listeners.entity;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.SblockListener;
import com.easterlyn.events.region.SblockTravelAgent;
import com.easterlyn.micromodules.Protections;
import com.easterlyn.micromodules.protectionhooks.ProtectionHook;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPortalEvent;

/**
 * Listener for EntityPortalEvents.
 * 
 * @author Jikoo
 */
public class PortalListener extends SblockListener {

	private final Protections protections;
	private final SblockTravelAgent agent;

	public PortalListener(Easterlyn plugin) {
		super(plugin);
		this.protections = plugin.getModule(Protections.class);
		agent = new SblockTravelAgent();
	}

	/**
	 * EventHandler for EntityPortalEvents.
	 * 
	 * @param event the EntityPortalEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityPortal(EntityPortalEvent event) {
		if (!event.useTravelAgent()) {
			return;
		}
		Environment fromEnvironment = event.getFrom().getWorld().getEnvironment();
		if (fromEnvironment == Environment.THE_END) {
			event.setCancelled(true);
			return;
		}
		agent.reset();
		Block fromPortal = agent.getAdjacentPortalBlock(event.getFrom().getBlock());
		if (fromPortal == null) {
			event.setCancelled(true);
			return;
		}
		if (fromEnvironment == Environment.NETHER) {
			agent.setSearchRadius(9);
		} else {
			agent.setSearchRadius(1);
		}
		event.setPortalTravelAgent(agent);
		Location fromCenter = agent.findCenter(fromPortal);
		fromCenter.setPitch(event.getFrom().getPitch());
		fromCenter.setYaw(event.getFrom().getYaw());
		event.setFrom(fromCenter);
		agent.setFrom(fromCenter.getBlock());
		Location to = agent.getTo(event.getFrom());
		if (to == null) {
			event.setCancelled(true);
			return;
		}
		Location toPortal = agent.findPortal(to);
		if (toPortal == null) {
			for (ProtectionHook hook : protections.getHooks()) {
				if (hook.isProtected(to)) {
					event.setCancelled(true);
					return;
				}
			}
		}
		event.setTo(to);
	}

}
