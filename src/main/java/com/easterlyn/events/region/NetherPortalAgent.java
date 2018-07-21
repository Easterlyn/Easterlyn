package com.easterlyn.events.region;

import com.easterlyn.machines.utilities.Direction;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.machines.utilities.Shape.MaterialDataValue;
import com.easterlyn.utilities.RegionUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * TravelAgent for nether portals.
 *
 * @author Jikoo
 */
public class NetherPortalAgent implements TravelAgent {

	private int searchRadius = 0, creationRadius = 0;
	private boolean canCreatePortal = true;
	private final Shape shape;
	private Block from;

	public NetherPortalAgent() {
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

		value = shape.new MaterialDataValue(Material.NETHER_PORTAL, Direction.NORTH);

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
		for (int dX = -searchRadius; dX <= searchRadius; dX++) {
			// When travelling to the overworld, allow 2 blocks of y-forgiveness in case of rounding errors
			int searchY = location.getWorld().getEnvironment() == Environment.NORMAL ? 3 : 1;
			for (int dY = 0; dY < searchY; ++dY) {
				for (int dZ = -searchRadius; dZ <= searchRadius; dZ++) {
					Block portal = block.getRelative(dX, dY, dZ);
					if (portal.getType() == Material.NETHER_PORTAL) {
						Location center = RegionUtils.findNetherPortalCenter(portal);
						center.setYaw(location.getYaw());
						center.setPitch(location.getPitch());
						return center;
					}
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
		shape.build(location, direction);
		return true;
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

}
