package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.events.listeners.SblockListener;
import com.easterlyn.events.region.SblockTravelAgent;
import com.easterlyn.micromodules.Protections;
import com.easterlyn.micromodules.protectionhooks.ProtectionHook;

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
public class PortalListener extends SblockListener {

	private final Language lang;
	private final Protections protections;
	private final SblockTravelAgent agent;

	public PortalListener(Easterlyn plugin) {
		super(plugin);
		this.lang = plugin.getModule(Language.class);
		this.protections = plugin.getModule(Protections.class);
		this.agent = new SblockTravelAgent();
	}

	/**
	 * EventHandler for PlayerPortalEvents.
	 * 
	 * @param event the PlayerPortalEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerPortal(PlayerPortalEvent event) {
		if (!event.useTravelAgent()) {
			// Generally, end portals do not use travel agents, nether portals do.
			return;
		}
		Environment fromEnvironment = event.getFrom().getWorld().getEnvironment();
		agent.reset();
		Block fromPortal = agent.getAdjacentPortalBlock(event.getFrom().getBlock());
		Location fromCenter = agent.findCenter(fromPortal);
		if (fromPortal == null) {
			if (fromEnvironment != Environment.THE_END
					&& event.getTo().getWorld().getEnvironment() != Environment.THE_END) {
				event.setCancelled(true);
			}
			return;
		}
		if (fromEnvironment == Environment.THE_END) {
			if (fromCenter.getBlock().getType() == Material.PORTAL) {
				// No using nether portals in the End
				event.setCancelled(true);
			}
			// If we do another End implementation (Medium, etc.), it belongs here.
			return;
		}
		if (fromEnvironment == Environment.NETHER) {
			agent.setSearchRadius(8);
		} else {
			agent.setSearchRadius(1);
		}
		fromCenter.setPitch(event.getFrom().getPitch());
		fromCenter.setYaw(event.getFrom().getYaw());
		event.setPortalTravelAgent(agent);
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
				if (!hook.canBuildAt(event.getPlayer(), to)) {
					event.setCancelled(true);
					event.getPlayer().sendMessage(lang.getValue("events.portal.protected"));
					return;
				}
			}
		}
		event.setTo(toPortal != null ? toPortal : to);
	}

}
