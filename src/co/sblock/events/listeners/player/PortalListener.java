package co.sblock.events.listeners.player;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPortalEvent;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.events.listeners.SblockListener;
import co.sblock.events.region.SblockTravelAgent;
import co.sblock.micromodules.Protections;
import co.sblock.micromodules.protectionhooks.ProtectionHook;

/**
 * Listener for PlayerPortalEvents.
 * 
 * @author Jikoo
 */
public class PortalListener extends SblockListener {

	private final Protections protections;
	private final SblockTravelAgent agent;

	public PortalListener(Sblock plugin) {
		super(plugin);
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
			return;
		}
		if (event.getTo().getWorld().getEnvironment() == Environment.THE_END) {
			// future Medium End?
			return;
		}
		Environment fromEnvironment = event.getFrom().getWorld().getEnvironment();
		agent.reset();
		Block fromPortal = agent.getAdjacentPortalBlock(event.getFrom().getBlock());
		Location fromCenter = agent.findCenter(fromPortal);
		if (fromPortal == null && fromEnvironment != Environment.THE_END) {
			event.setCancelled(true);
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
					System.out.println(String.format("%s preventing portalling for %s from %s %s, %s, %s to %s %s, %s, %s",
							hook.getPluginName(), event.getPlayer().getName(), fromCenter.getWorld().getName(),
							fromCenter.getBlockX(), fromCenter.getBlockY(), fromCenter.getBlockZ(),
							to.getWorld().getName(), to.getBlockX(), to.getBlockY(), to.getBlockZ()));
					event.getPlayer().sendMessage(Color.BAD + "Your destination is inside a protected area!"
							+ "\nYou'll have to build your portal elsewhere.");
					return;
				}
			}
		}
		event.setTo(toPortal != null ? toPortal : to);
	}

}
