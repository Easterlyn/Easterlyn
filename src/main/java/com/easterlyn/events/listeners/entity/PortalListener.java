package com.easterlyn.events.listeners.entity;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.events.region.NetherPortalAgent;
import com.easterlyn.micromodules.Protections;
import com.easterlyn.micromodules.protectionhooks.ProtectionHook;
import com.easterlyn.utilities.RegionUtils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPortalEvent;

/**
 * Listener for EntityPortalEvents.
 *
 * @author Jikoo
 */
public class PortalListener extends EasterlynListener {

	private final Protections protections;
	private final NetherPortalAgent agent;

	public PortalListener(Easterlyn plugin) {
		super(plugin);
		this.protections = plugin.getModule(Protections.class);
		agent = new NetherPortalAgent();
	}

	/**
	 * EventHandler for EntityPortalEvents.
	 *
	 * @param event the EntityPortalEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityPortal(EntityPortalEvent event) {
		Environment fromEnvironment = event.getFrom().getWorld().getEnvironment();
		Block fromPortal = RegionUtils.getAdjacentPortalBlock(event.getEntity().getLocation().getBlock());

		if (fromPortal == null || fromPortal.getType() == Material.END_PORTAL) {
			event.setCancelled(true);
			return;
		}


		if (event.useTravelAgent()) {
			agent.reset();
			event.setPortalTravelAgent(agent);

			if (fromEnvironment == Environment.NETHER) {
				agent.setSearchRadius(9);
			} else {
				agent.setSearchRadius(1);
			}

			Location fromCenter = RegionUtils.findNetherPortalCenter(fromPortal);
			if (fromCenter != null) {
				fromCenter.setPitch(event.getFrom().getPitch());
				fromCenter.setYaw(event.getFrom().getYaw() - 180);
				event.setFrom(fromCenter);
			}

		}

		Location to = RegionUtils.calculatePortalDestination(event.getFrom(), fromPortal.getType());

		if (to == null) {
			event.setCancelled(true);
			return;
		}

		event.setTo(to);

		if (!event.useTravelAgent()) {
			return;
		}

		agent.setFrom(event.getFrom().getBlock());
		Location toPortal = agent.findPortal(to);
		if (toPortal == null) {
			for (ProtectionHook hook : protections.getHooks()) {
				if (hook.isProtected(to)) {
					event.setCancelled(true);
					return;
				}
			}
		} else {
			event.setTo(toPortal);
		}
	}

}
