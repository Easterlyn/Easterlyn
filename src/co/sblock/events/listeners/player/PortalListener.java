package co.sblock.events.listeners.player;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPortalEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.events.region.SblockTravelAgent;

/**
 * Listener for PlayerPortalEvents.
 * 
 * @author Jikoo
 */
public class PortalListener extends SblockListener {

	private final SblockTravelAgent agent;

	public PortalListener(Sblock plugin) {
		super(plugin);
		agent = new SblockTravelAgent();
	}

	/**
	 * EventHandler for PlayerPortalEvents.
	 * 
	 * @param event the PlayerPortalEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerPortal(PlayerPortalEvent event) {
		if (!event.useTravelAgent()) {
			return;
		}
		if (event.getTo().getWorld().getEnvironment() == Environment.THE_END) {
			// TODO Medium End?
			return;
		}
		Environment fromEnvironment = event.getFrom().getWorld().getEnvironment();
		agent.reset();
		Block portal = agent.getAdjacentPortalBlock(event.getFrom().getBlock());
		Location center = agent.findCenter(portal);
		if (portal == null && fromEnvironment != Environment.THE_END) {
			event.setCancelled(true);
			return;
		}
		if (fromEnvironment == Environment.THE_END) {
			if (center.getBlock().getType() == Material.PORTAL) {
				// No using nether portals in the End
				event.setCancelled(true);
			}
			// TODO Medium End return?
			return;
		}
		if (fromEnvironment == Environment.NETHER) {
			agent.setSearchRadius(8);
		}
		center.setPitch(event.getFrom().getPitch());
		center.setYaw(event.getFrom().getYaw());
		event.setPortalTravelAgent(agent);
		event.setFrom(center);
		agent.setFrom(center.getBlock());
		Location to = agent.getTo(event.getFrom());
		if (to == null) {
			event.setCancelled(true);
			return;
		}
		event.setTo(to);
	}

}
