package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
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
import org.bukkit.event.player.PlayerPortalEvent;

/**
 * Listener for PlayerPortalEvents.
 *
 * @author Jikoo
 */
public class PortalListener extends EasterlynListener {

	private final Language lang;
	private final Protections protections;
	private final NetherPortalAgent agent;

	public PortalListener(Easterlyn plugin) {
		super(plugin);
		this.lang = plugin.getModule(Language.class);
		this.protections = plugin.getModule(Protections.class);
		this.agent = new NetherPortalAgent();
	}

	/**
	 * EventHandler for PlayerPortalEvents.
	 *
	 * @param event the PlayerPortalEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerPortal(PlayerPortalEvent event) {

		Environment fromEnvironment = event.getFrom().getWorld().getEnvironment();
		// Find the portal block used.
		Block fromPortal = RegionUtils.getAdjacentPortalBlock(event.getFrom().getBlock());

		if (fromPortal == null) {
			// If the portal block could not be found, cancel portal usage.
			event.setCancelled(true);
			return;
		}

		event.useTravelAgent(fromPortal.getType() == Material.NETHER_PORTAL);

		if (event.useTravelAgent()) {
			// Reset agent for reuse in case other plugins changed values.
			agent.reset();
			event.setPortalTravelAgent(agent);

			if (fromEnvironment == Environment.NETHER) {
				// Nether is 1/8 of normal so portals can exist in a much wider radius
				agent.setSearchRadius(8);
			} else {
				// Normal or end
				agent.setSearchRadius(0);
			}

			// If a travel agent is in use, portal is likely a nether portal.
			// For simplifying creating portals that link successfully, from location should be the center of the portal.
			Location fromCenter = RegionUtils.findNetherPortalCenter(fromPortal);
			if (fromCenter != null) {
				fromCenter.setPitch(event.getFrom().getPitch());
				// For some reason facing seems to be inverted in the nether.
				fromCenter.setYaw(event.getFrom().getYaw() - 180);
				event.setFrom(fromCenter);
			}
		}

		// Calculate destination based on portal location and type.
		Location to = RegionUtils.calculatePortalDestination(event.getFrom(), fromPortal.getType());

		if (to == null) {
			// If the destination location cannot be calculated, cancel portal usage.
			event.setCancelled(true);
			return;
		}

		// Set the destination to the exact calculated output location.
		event.setTo(to);

		if (!event.useTravelAgent()) {
			// All remaining handling is for nether portals using our travel agent.
			return;
		}

		// Set the block for our custom travel agent - used in new portal creation.
		agent.setFrom(event.getFrom().getBlock());
		// Attempt to find a portal to link to.
		Location toPortal = agent.findPortal(to);
		if (toPortal == null) {
			// Portal cannot be found, a new one will be created. Check protections.
			for (ProtectionHook hook : protections.getHooks()) {
				if (hook.isProtected(to)) {
					event.setCancelled(true);
					event.getPlayer().sendMessage(lang.getValue("events.portal.protected"));
					return;
				}
			}
		} else {
			// Set destination to located portal.
			event.setTo(toPortal);
		}
	}

}
