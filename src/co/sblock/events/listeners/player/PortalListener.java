package co.sblock.events.listeners.player;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

import co.sblock.events.region.SblockTravelAgent;

/**
 * Listener for PlayerPortalEvents.
 * 
 * @author Jikoo
 */
public class PortalListener implements Listener {

	private final SblockTravelAgent agent;

	public PortalListener() {
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
		Environment fromEnvironment = event.getFrom().getWorld().getEnvironment();
		if (fromEnvironment == Environment.THE_END) {
			event.setCancelled(true);
			return;
		}
		agent.reset();
		Block portal = agent.getAdjacentPortalBlock(event.getFrom().getBlock());
		if (portal == null) {
			event.setCancelled(true);
			return;
		}
		if (fromEnvironment == Environment.NETHER) {
			agent.setSearchRadius(8);
		}
		event.setPortalTravelAgent(agent);
		Location center = agent.findCenter(portal);
		center.setPitch(event.getFrom().getPitch());
		center.setYaw(event.getFrom().getYaw());
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
