package co.sblock.events.region;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.Shape;
import co.sblock.machines.utilities.Shape.MaterialDataValue;

public class SblockTravelAgent implements TravelAgent {

	private int searchRadius = 0, creationRadius = 0;
	private final int mediumNetherOffset = 329;
	private boolean canCreatePortal = true;
	private final Shape shape;
	private Block from;

	public SblockTravelAgent() {
		shape = new Shape();

		MaterialDataValue value = shape.new MaterialDataValue(Material.OBSIDIAN);

		// Platform
		shape.setVectorData(new Vector(-2, -1, 1), value);
		shape.setVectorData(new Vector(-1, -1, 1), value);
		shape.setVectorData(new Vector(0, -1, 1), value);
		shape.setVectorData(new Vector(1, -1, 1), value);
		shape.setVectorData(new Vector(-2, -1, -1), value);
		shape.setVectorData(new Vector(-1, -1, -1), value);
		shape.setVectorData(new Vector(0, -1, -1), value);
		shape.setVectorData(new Vector(1, -1, -1), value);

		// Frame
		shape.setVectorData(new Vector(-2, -1, 0), value);
		shape.setVectorData(new Vector(-1, -1, 0), value);
		shape.setVectorData(new Vector(0, -1, 0), value);
		shape.setVectorData(new Vector(1, -1, 0), value);
		shape.setVectorData(new Vector(-2, 0, 0), value);
		shape.setVectorData(new Vector(1, 0, 0), value);
		shape.setVectorData(new Vector(-2, 1, 0), value);
		shape.setVectorData(new Vector(1, 1, 0), value);
		shape.setVectorData(new Vector(-2, 2, 0), value);
		shape.setVectorData(new Vector(1, 2, 0), value);
		shape.setVectorData(new Vector(-2, 3, 0), value);
		shape.setVectorData(new Vector(-1, 3, 0), value);
		shape.setVectorData(new Vector(0, 3, 0), value);
		shape.setVectorData(new Vector(1, 3, 0), value);

		value = shape.new MaterialDataValue(Material.AIR);

		// Surrounding air
		shape.setVectorData(new Vector(-2, 0, 1), value);
		shape.setVectorData(new Vector(-1, 0, 1), value);
		shape.setVectorData(new Vector(0, 0, 1), value);
		shape.setVectorData(new Vector(1, 0, 1), value);
		shape.setVectorData(new Vector(-2, 0, -1), value);
		shape.setVectorData(new Vector(-1, 0, -1), value);
		shape.setVectorData(new Vector(0, 0, -1), value);
		shape.setVectorData(new Vector(1, 0, -1), value);
		shape.setVectorData(new Vector(-2, 1, 1), value);
		shape.setVectorData(new Vector(-1, 1, 1), value);
		shape.setVectorData(new Vector(0, 1, 1), value);
		shape.setVectorData(new Vector(1, 1, 1), value);
		shape.setVectorData(new Vector(-2, 1, -1), value);
		shape.setVectorData(new Vector(-1, 1, -1), value);
		shape.setVectorData(new Vector(0, 1, -1), value);
		shape.setVectorData(new Vector(1, 1, -1), value);
		shape.setVectorData(new Vector(-2, 2, 1), value);
		shape.setVectorData(new Vector(-1, 2, 1), value);
		shape.setVectorData(new Vector(0, 2, 1), value);
		shape.setVectorData(new Vector(1, 2, 1), value);
		shape.setVectorData(new Vector(-2, 2, -1), value);
		shape.setVectorData(new Vector(-1, 2, -1), value);
		shape.setVectorData(new Vector(0, 2, -1), value);
		shape.setVectorData(new Vector(1, 2, -1), value);
		shape.setVectorData(new Vector(-2, 3, 1), value);
		shape.setVectorData(new Vector(-1, 3, 1), value);
		shape.setVectorData(new Vector(0, 3, 1), value);
		shape.setVectorData(new Vector(1, 3, 1), value);
		shape.setVectorData(new Vector(-2, 3, -1), value);
		shape.setVectorData(new Vector(-1, 3, -1), value);
		shape.setVectorData(new Vector(0, 3, -1), value);
		shape.setVectorData(new Vector(1, 3, -1), value);

		value = shape.new MaterialDataValue(Material.PORTAL, Direction.NORTH, "portal");

		// Portal
		shape.setVectorData(new Vector(-1, 0, 0), value);
		shape.setVectorData(new Vector(0, 0, 0), value);
		shape.setVectorData(new Vector(-1, 1, 0), value);
		shape.setVectorData(new Vector(0, 1, 0), value);
		shape.setVectorData(new Vector(-1, 2, 0), value);
		shape.setVectorData(new Vector(0, 2, 0), value);
	}

	@Override
	public TravelAgent setSearchRadius(int radius) {
		this.searchRadius = radius;
		return this;
	}

	@Override
	public int getSearchRadius() {
		return this.searchRadius;
	}

	@Override
	public TravelAgent setCreationRadius(int radius) {
		creationRadius = radius;
		return this;
	}

	@Override
	public int getCreationRadius() {
		return creationRadius;
	}

	@Override
	public boolean getCanCreatePortal() {
		return canCreatePortal;
	}

	public void setFrom(Block block) {
		from = block;
	}

	@Override
	public Location findOrCreate(Location location) {
		Location destination = findPortal(location);
		if (destination == null || destination.equals(destination.getWorld().getSpawnLocation())) {
			destination = location.clone();
			createPortal(destination);
		}
		return destination;
	}

	@Override
	public Location findPortal(Location location) {
		Block block = location.getBlock();
		for (int dx = -searchRadius; dx <= searchRadius; dx++) {
			for (int dz = -searchRadius; dz <= searchRadius; dz++) {
				Block portal = block.getRelative(dx, 0, dz);
				if (portal.getType() == Material.PORTAL) {
					Location center = findCenter(portal);
					center.setYaw(location.getYaw());
					center.setPitch(location.getPitch());
					return center;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean createPortal(Location location) {
		if (from == null) {
			return false;
		}
		Direction direction = from.getData() % 2 == 0 ? Direction.EAST : Direction.NORTH;
		for (Entry<Location, MaterialData> entry : shape.getBuildLocations(location, direction).entrySet()) {
			Block block = entry.getKey().getBlock();
			block.setTypeIdAndData(entry.getValue().getItemTypeId(), entry.getValue().getData(), false);
		}
		return true;
	}

	public Location findCenter(Block portal) {
		double minX = 0;
		while (portal.getRelative((int) minX - 1, 0, 0).getType() == Material.PORTAL) {
			minX -= 1;
		}
		double maxX = 0;
		while (portal.getRelative((int) maxX + 1, 0, 0).getType() == Material.PORTAL) {
			maxX += 1;
		}
		double minY = 0;
		while (portal.getRelative(0, (int) minY - 1, 0).getType() == Material.PORTAL) {
			minY -= 1;
		}
		double minZ = 0;
		while (portal.getRelative(0, 0, (int) minZ - 1).getType() == Material.PORTAL) {
			minZ -= 1;
		}
		double maxZ = 0;
		while (portal.getRelative(0, 0, (int) maxZ + 1).getType() == Material.PORTAL) {
			maxZ += 1;
		}
		double x = portal.getX() + (maxX + 1 + minX) / 2.0;
		double y = portal.getY() + minY + 0.1;
		double z = portal.getZ() + (maxZ + 1 + minZ) / 2.0;
		return new Location(portal.getWorld(), x, y, z);
	}

	@Override
	public void setCanCreatePortal(boolean create) {
		canCreatePortal = create;
	}

	public TravelAgent reset() {
		searchRadius = 1;
		creationRadius = 0;
		canCreatePortal = true;
		from = null;
		return this;
	}

	public Block getAdjacentPortalBlock(Block block) {
		// Player isn't standing inside the portal block, they're next to it.
		if (block.getType() == Material.PORTAL) {
			return block;
		}
		for (int dX = -1; dX < 2; dX++) {
			for (int dZ = -1; dZ < 2; dZ++) {
				if (dX == 0 && dZ == 0) {
					continue;
				}
				Block maybePortal = block.getRelative(dX, 0, dZ);
				if (maybePortal.getType() == Material.PORTAL) {
					return maybePortal;
				}
			}
		}
		return null;
	}

	public Location getTo(Location from) {
		World world = null;
		double x, y, z;
		switch (from.getWorld().getName()) {
		case "Earth":
			world = Bukkit.getWorld("Earth_nether");
			x = from.getX() / 8;
			y = from.getY();
			z = from.getZ() / 8;
			break;
		case "Earth_nether":
			world = Bukkit.getWorld("Earth");
			x = from.getX() * 8;
			y = from.getY();
			z = from.getZ() * 8;
			break;
		case "LOFAF":
			world = Bukkit.getWorld("Medium_nether");
			x = from.getX() / 8 + mediumNetherOffset;
			y = from.getY() / 2.05;
			z = from.getZ() / 8 + mediumNetherOffset;
			break;
		case "LOHAC":
			world = Bukkit.getWorld("Medium_nether");
			x = from.getX() / 8 + mediumNetherOffset;
			y = from.getY() / 2.05;
			z = from.getZ() / 8 - mediumNetherOffset;
			break;
		case "LOLAR":
			world = Bukkit.getWorld("Medium_nether");
			x = from.getX() / 8 - mediumNetherOffset;
			y = from.getY() / 2.05;
			z = from.getZ() / 8 - mediumNetherOffset;
			break;
		case "LOWAS":
			world = Bukkit.getWorld("Medium_nether");
			x = from.getX() / 8 - mediumNetherOffset;
			y = from.getY() / 2.05;
			z = from.getZ() / 8 + mediumNetherOffset;
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
