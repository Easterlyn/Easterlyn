package co.sblock.events.region;

import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.Shape;
import co.sblock.machines.utilities.Shape.MaterialDataValue;

public class SblockTravelAgent implements TravelAgent {

	private int searchRadius = 0, creationRadius = 0;
	private boolean canCreatePortal = true;
	private final Shape shape;

	public SblockTravelAgent() {
		shape = new Shape();

		MaterialDataValue value = shape.new MaterialDataValue(Material.OBSIDIAN);

		// Platform
		shape.setVectorData(new Vector(1, -1, -2), value);
		shape.setVectorData(new Vector(1, -1, -1), value);
		shape.setVectorData(new Vector(1, -1, 0), value);
		shape.setVectorData(new Vector(1, -1, 1), value);
		shape.setVectorData(new Vector(-1, -1, -2), value);
		shape.setVectorData(new Vector(-1, -1, -1), value);
		shape.setVectorData(new Vector(-1, -1, 0), value);
		shape.setVectorData(new Vector(-1, -1, 1), value);

		// Frame
		shape.setVectorData(new Vector(0, -1, -2), value);
		shape.setVectorData(new Vector(0, -1, -1), value);
		shape.setVectorData(new Vector(0, -1, 0), value);
		shape.setVectorData(new Vector(0, -1, 1), value);
		shape.setVectorData(new Vector(0, 0, -2), value);
		shape.setVectorData(new Vector(0, 0, 1), value);
		shape.setVectorData(new Vector(0, 1, -2), value);
		shape.setVectorData(new Vector(0, 1, 1), value);
		shape.setVectorData(new Vector(0, 2, -2), value);
		shape.setVectorData(new Vector(0, 2, 1), value);
		shape.setVectorData(new Vector(0, 3, -2), value);
		shape.setVectorData(new Vector(0, 3, -1), value);
		shape.setVectorData(new Vector(0, 3, 0), value);
		shape.setVectorData(new Vector(0, 3, 1), value);

		value = shape.new MaterialDataValue(Material.AIR);

		// Surrounding air
		shape.setVectorData(new Vector(1, 0, -2), value);
		shape.setVectorData(new Vector(1, 0, -1), value);
		shape.setVectorData(new Vector(1, 0, 0), value);
		shape.setVectorData(new Vector(1, 0, 1), value);
		shape.setVectorData(new Vector(-1, 0, -2), value);
		shape.setVectorData(new Vector(-1, 0, -1), value);
		shape.setVectorData(new Vector(-1, 0, 0), value);
		shape.setVectorData(new Vector(-1, 0, 1), value);
		shape.setVectorData(new Vector(1, 1, -2), value);
		shape.setVectorData(new Vector(1, 1, -1), value);
		shape.setVectorData(new Vector(1, 1, 0), value);
		shape.setVectorData(new Vector(1, 1, 1), value);
		shape.setVectorData(new Vector(-1, 1, -2), value);
		shape.setVectorData(new Vector(-1, 1, -1), value);
		shape.setVectorData(new Vector(-1, 1, 0), value);
		shape.setVectorData(new Vector(-1, 1, 1), value);
		shape.setVectorData(new Vector(1, 2, -2), value);
		shape.setVectorData(new Vector(1, 2, -1), value);
		shape.setVectorData(new Vector(1, 2, 0), value);
		shape.setVectorData(new Vector(1, 2, 1), value);
		shape.setVectorData(new Vector(-1, 2, -2), value);
		shape.setVectorData(new Vector(-1, 2, -1), value);
		shape.setVectorData(new Vector(-1, 2, 0), value);
		shape.setVectorData(new Vector(-1, 2, 1), value);
		shape.setVectorData(new Vector(1, 3, -2), value);
		shape.setVectorData(new Vector(1, 3, -1), value);
		shape.setVectorData(new Vector(1, 3, 0), value);
		shape.setVectorData(new Vector(1, 3, 1), value);
		shape.setVectorData(new Vector(-1, 3, -2), value);
		shape.setVectorData(new Vector(-1, 3, -1), value);
		shape.setVectorData(new Vector(-1, 3, 0), value);
		shape.setVectorData(new Vector(-1, 3, 1), value);

		value = shape.new MaterialDataValue(Material.PORTAL, Direction.NORTH, "anvil");

		// Portal
		shape.setVectorData(new Vector(0, 0, -1), value);
		shape.setVectorData(new Vector(0, 0, 0), value);
		shape.setVectorData(new Vector(0, 1, -1), value);
		shape.setVectorData(new Vector(0, 1, 0), value);
		shape.setVectorData(new Vector(0, 2, -1), value);
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

	@Override
	public Location findOrCreate(Location location) {
		Location destination = findPortal(location);
		if (destination == null) {
			destination = location.clone();
			createPortal(destination);
		}
		return destination;
	}

	@Override
	public Location findPortal(Location location) {
		Block block = location.getBlock();
		for (int dx = -this.searchRadius; dx <= this.searchRadius; dx++) {
			for (int dz = -this.searchRadius; dz <= this.searchRadius; dz++) {
				Block portal = block.getRelative(dx, 0, dz);
				if (portal.getType() == Material.PORTAL) {
					Location center = findCenter(portal);
					center.setYaw(location.getYaw());
					center.setYaw(location.getPitch());
					return center;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean createPortal(Location location) {
		Direction direction = Direction.getFacingDirection(location);
		for (Entry<Location, MaterialData> entry : shape.getBuildLocations(location, direction).entrySet()) {
			location.getBlock().setTypeIdAndData(entry.getValue().getItemTypeId(), entry.getValue().getData(), false);
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
		searchRadius = 0;
		creationRadius = 0;
		canCreatePortal = true;
		return this;
	}

}
