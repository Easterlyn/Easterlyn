package com.easterlyn.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * Enum managing worlds and resource packs.
 *
 * @author Jikoo
 */
public class RegionUtils {

	public static boolean regionsMatch(String worldName, String otherWorldName) {
		return stripToBaseWorldName(worldName.toLowerCase())
				.equals(stripToBaseWorldName(otherWorldName.toLowerCase()));
	}

	private static String stripToBaseWorldName(String worldName) {
		return worldName.replaceAll("(.*)_(the_end|nether)", "$1");
	}

	public static Location calculatePortalDestination(Location from, Material portalType) {
		if (portalType != Material.NETHER_PORTAL && portalType != Material.END_PORTAL) {
			return null;
		}

		World world;
		double x, y, z;
		switch (from.getWorld().getName()) {
		case "Earth":
			if (portalType == Material.NETHER_PORTAL) {
				world = Bukkit.getWorld("Earth_nether");
				x = from.getX() / 8;
				y = from.getY();
				z = from.getZ() / 8;
				break;
			}
		case "Earth_nether":
			if (portalType == Material.NETHER_PORTAL) {
				world = Bukkit.getWorld("Earth");
				x = from.getX() * 8;
				y = from.getY();
				z = from.getZ() * 8;
				break;
			}
		default:
			String baseWorldName = stripToBaseWorldName(from.getWorld().getName());
			switch (from.getWorld().getEnvironment()) {
			case NETHER:
				if (portalType == Material.END_PORTAL) {
					return null;
				}
				world = Bukkit.getWorld(baseWorldName);
				if (world == null || world.equals(from.getWorld())) {
					return null;
				}
				x = from.getX() * 8;
				y = Math.max(2, Math.min(251, Math.floor(from.getY() * 2.05)));
				z = from.getZ() * 8;
				break;
			case NORMAL:
				if (portalType == Material.END_PORTAL) {
					world = Bukkit.getWorld(baseWorldName + "_the_end");
					return world != null ? world.getSpawnLocation().clone().add(new Vector(0.5, 0.1, 0.5)) : null;
				}
				world = Bukkit.getWorld(from.getWorld().getName() + "_nether");
				if (world == null || world.equals(from.getWorld())) {
					return null;
				}
				x = from.getX() / 8;
				y = Math.max(2, Math.min(123, Math.ceil(from.getY() / 2.05)));
				z = from.getZ() / 8;
				break;
			case THE_END:
				if (portalType == Material.NETHER_PORTAL) {
					return null;
				}
				world = Bukkit.getWorld(baseWorldName);
				return world != null ? world.getSpawnLocation().add(new Vector(0.5, 0.1, 0.5)) : null;
			default:
				return null;
			}
		}
		if (world == null) {
			return null;
		}
		return new Location(world, x, y, z, from.getYaw(), from.getPitch());
	}

	public static Block getAdjacentPortalBlock(Block block) {
		if (block.getType() == Material.NETHER_PORTAL || block.getType() == Material.END_PORTAL) {
			return block;
		}
		// Player isn't standing inside the portal block, they're next to it or below it.
		for (int dX = -1; dX < 2; dX++) {
			for (int dY = -1; dY < 4; dY++) {
				// Search higher in case of end portals, falling through at speed can lead to portal usage from a position well beyond
				for (int dZ = -1; dZ < 2; dZ++) {
					if (dX == 0 && dY == 0 && dZ == 0) {
						continue;
					}
					Block maybePortal = block.getRelative(dX, dY, dZ);
					if (maybePortal.getType() == Material.NETHER_PORTAL || maybePortal.getType() == Material.END_PORTAL) {
						return maybePortal;
					}
				}
			}
		}
		return null;
	}

	public static Location findNetherPortalCenter(Block portal) {
		if (portal == null) {
			return null;
		}
		double minX = 0;
		while (portal.getRelative((int) minX - 1, 0, 0).getType() == Material.NETHER_PORTAL) {
			minX -= 1;
		}
		double maxX = 0;
		while (portal.getRelative((int) maxX + 1, 0, 0).getType() == Material.NETHER_PORTAL) {
			maxX += 1;
		}
		double minY = 0;
		while (portal.getRelative(0, (int) minY - 1, 0).getType() == Material.NETHER_PORTAL) {
			minY -= 1;
		}
		double minZ = 0;
		while (portal.getRelative(0, 0, (int) minZ - 1).getType() == Material.NETHER_PORTAL) {
			minZ -= 1;
		}
		double maxZ = 0;
		while (portal.getRelative(0, 0, (int) maxZ + 1).getType() == Material.NETHER_PORTAL) {
			maxZ += 1;
		}
		double x = portal.getX() + (maxX + 1 + minX) / 2.0;
		double y = portal.getY() + minY + 0.1;
		double z = portal.getZ() + (maxZ + 1 + minZ) / 2.0;
		return new Location(portal.getWorld(), x, y, z);
	}

}
