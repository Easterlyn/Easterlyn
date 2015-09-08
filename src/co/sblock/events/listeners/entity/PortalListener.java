package co.sblock.events.listeners.entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;

import co.sblock.events.region.SblockTravelAgent;

/**
 * Listener for EntityPortalEvents.
 * 
 * @author Jikoo
 */
public class PortalListener implements Listener {

	private final SblockTravelAgent agent;
	private final int mediumNetherOffset;

	public PortalListener() {
		agent = new SblockTravelAgent();
		mediumNetherOffset = 329; 
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
		Block fromBlock = event.getFrom().getBlock();
		// Player isn't standing inside the portal block, they're next to it.
		if (fromBlock.getType() != Material.PORTAL) {
			boolean portal = false;
			portal: for (int dX = -1; dX < 2; dX++) {
				for (int dZ = -1; dZ < 2; dZ++) {
					if (dX == 0 && dZ == 0) {
						continue;
					}
					Block maybePortal = fromBlock.getRelative(dX, 0, dZ);
					if ((portal = maybePortal.getType() == Material.PORTAL)) {
						fromBlock = maybePortal;
						break portal;
					}
				}
			}
			if (!portal) {
				event.setCancelled(true);
				return;
			}
		}
		agent.reset();
		event.setFrom(agent.findCenter(fromBlock));
		Location to = getTo(event.getFrom());
		if (to == null) {
			event.setCancelled(true);
			return;
		}
		event.setTo(to);
	}

	public Location getTo(Location from) {
		World world = null;
		double x, y, z;
		switch (from.getWorld().getName()) {
		case "Earth":
			world = Bukkit.getWorld("Earth_nether");
			x = from.getX() * 8;
			y = from.getY();
			z = from.getZ() * 8;
			break;
		case "Earth_nether":
			world = Bukkit.getWorld("Earth");
			x = from.getX() / 8;
			y = from.getY();
			z = from.getZ() / 8;
			break;
		case "LOFAF":
			world = Bukkit.getWorld("Medium_nether");
			x = from.getX() * 8 + mediumNetherOffset;
			y = from.getY() / 2.05;
			z = from.getZ() * 8 + mediumNetherOffset;
			break;
		case "LOHAC":
			world = Bukkit.getWorld("Medium_nether");
			x = from.getX() * 8 + mediumNetherOffset;
			y = from.getY() / 2.05;
			z = from.getZ() * 8 - mediumNetherOffset;
			break;
		case "LOLAR":
			world = Bukkit.getWorld("Medium_nether");
			x = from.getX() * 8 - mediumNetherOffset;
			y = from.getY() / 2.05;
			z = from.getZ() * 8 - mediumNetherOffset;
			break;
		case "LOWAS":
			world = Bukkit.getWorld("Medium_nether");
			x = from.getX() * 8 - mediumNetherOffset;
			y = from.getY() / 2.05;
			z = from.getZ() * 8 + mediumNetherOffset;
			break;
		case "Medium_nether":
			String worldName;
			if (from.getX() < 0) {
				x = from.getX() + mediumNetherOffset;
				if (from.getZ() < 0) {
					// -x -z: LOLAR (Northwest)
					z = from.getZ() + mediumNetherOffset;
					worldName = "LOLAR";
				} else {
					// -x +z: LOWAS (Southwest)
					z = from.getZ() - mediumNetherOffset;
					worldName = "LOWAS";
				}
			} else {
				x = from.getX() - mediumNetherOffset;
				if (from.getZ() < 0) {
					// +x -z: LOHAC (Northeast)
					z = from.getZ() + mediumNetherOffset;
					worldName = "LOHAC";
				} else {
					// +x +z: LOFAF (Southeast)
					z = from.getZ() - mediumNetherOffset;
					worldName = "LOFAF";
				}
			}
			world = Bukkit.getWorld(worldName);
			x *= 8;
			y = from.getY() * 2.05;
			z *= 8;
			break;
		default:
			x = y = z = 0;
		}
		if (world == null) {
			return null;
		}
		if (y < 2) {
			y = 2;
		}
		return new Location(world, x, y, z, from.getYaw(), from.getPitch());
	}

}
