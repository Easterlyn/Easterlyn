package com.easterlyn;

import com.easterlyn.netherportals.listener.TeleportListener;
import com.easterlyn.util.Direction;
import com.easterlyn.util.Shape;
import com.easterlyn.util.event.SimpleListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Entity;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EasterlynNetherPortals extends JavaPlugin {

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new TeleportListener(this), this);

		RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (registration != null) {
			registration.getProvider().getLocaleManager().addLocaleSupplier(this);
		}

		PluginEnableEvent.getHandlerList().register(new SimpleListener<>(PluginEnableEvent.class,
				pluginEnableEvent -> {
					if (pluginEnableEvent.getPlugin() instanceof EasterlynCore) {
						((EasterlynCore) pluginEnableEvent.getPlugin()).getLocaleManager().addLocaleSupplier(this);
					}
				}, this));
	}

	@Nullable
	public Location getPortalDestination(@Nullable Entity entity, @NotNull Location from) {
		Block portal = getAdjacentPortal(entity, from.getBlock());

		if (portal == null) {
			return null;
		}

		from = findPortalCenter(portal);
		Location to = calculateDestination(from);

		if (to == null) {
			return null;
		}

		Location destination = findPortal(to);
		if (destination == null) {
			destination = to.clone();
			createPortal(entity, from, to);
		}

		return destination;
	}

	public @Nullable Block getAdjacentPortal(@Nullable Entity entity, @NotNull Block block) {
		return entity != null ? getAdjacentPortal(entity) : getAdjacentPortal(block);
	}

	private @Nullable Block getAdjacentPortal(@NotNull Entity entity) {
		Block exact = entity.getLocation().getBlock();
		if (exact.getType() == Material.NETHER_PORTAL) {
			return exact;
		}
		// Bounding box must be inside portal.
		BoundingBox boundingBox = entity.getBoundingBox();
		int dXMax = (int) Math.ceil(boundingBox.getWidthX() / 2D);
		int dZMax = (int) Math.ceil(boundingBox.getWidthZ() / 2D);
		return spiralRectangle(dXMax, dZMax, (x, z) -> {
			// Feet should always be in portal due to portal block shape unless portal is busted.
			Block potential = exact.getRelative(x, 0, z);
			if (potential.getType() == Material.NETHER_PORTAL) {
				return potential;
			}
			return null;
		});
	}

	private <T> T spiralRectangle(int side1Max, int side2Max, BiFunction<Integer, Integer, T> coordConsumer) {
		boolean swap = side1Max > side2Max;

		if (swap) {
			int temp = side1Max;
			side1Max = side2Max;
			side2Max = temp;
		}

		for (int radius = 1; radius <= side1Max || radius <= side2Max; ++radius) {
			int side1 = -Math.min(side1Max, radius);
			int side2 = Math.min(side2Max, radius);
			while (side1 <= radius && side2 > -radius) {
				T value = coordConsumer.apply(swap ? side2 : side1, swap ? side1 : side2);
				if (value != null) {
					return value;
				}
				value = coordConsumer.apply(swap ? -side2 : -side1, swap ? -side1 : -side2);
				if (value != null) {
					return value;
				}

				if (side1 < radius) {
					if (side1 < side1Max) {
						++side1;
					} else {
						break;
					}
				} else {
					--side2;
				}
			}
		}
		return null;
	}

	private @Nullable Block getAdjacentPortal(@NotNull Block block) {
		if (block.getType() == Material.NETHER_PORTAL) {
			return block;
		}
		// Player isn't standing inside the portal block, they're next to it or below it.
		for (int dX = -1; dX <= 1; ++dX) {
			for (int dY = -1; dY <= 1; ++dY) {
				for (int dZ = -1; dZ <= 1; ++dZ) {
					if (dX == 0 && dY == 0 && dZ == 0) {
						continue;
					}
					Block maybePortal = block.getRelative(dX, dY, dZ);
					if (maybePortal.getType() == Material.NETHER_PORTAL) {
						return maybePortal;
					}
				}
			}
		}
		return null;
	}

	public @Nullable Location calculateDestination(@NotNull Location from) {
		World world;
		double x, y, z;
		String baseWorldName = Objects.requireNonNull(from.getWorld()).getName().replaceAll("(.*)_(the_end|nether)", "$1");
		switch (from.getWorld().getEnvironment()) {
			case NETHER:
				world = getServer().getWorld(baseWorldName);
				if (world == null || world.equals(from.getWorld())) {
					return null;
				}
				x = from.getX() * 8;
				y = Math.max(2, Math.min(251, Math.floor(from.getY() * 2.05)));
				z = from.getZ() * 8;
				break;
			case NORMAL:
				world = getServer().getWorld(from.getWorld().getName() + "_nether");
				if (world == null || world.equals(from.getWorld())) {
					return null;
				}
				x = from.getX() / 8;
				y = Math.max(2, Math.min(123, Math.ceil(from.getY() / 2.05)));
				z = from.getZ() / 8;
				break;
			case THE_END:
				world = getServer().getWorld(baseWorldName);
				return world != null ? world.getSpawnLocation().add(new Vector(0.5, 0.1, 0.5)) : null;
			default:
				return null;
		}
		return new Location(world, x, y, z, from.getYaw(), from.getPitch());
	}

	@Nullable
	private Location findPortal(@NotNull Location location) {
		Block block = location.getBlock();
		int searchRadius = block.getWorld().getEnvironment() == World.Environment.NORMAL ? 18 : 2;
		// Set up to search outwards in radius from center.
		List<Integer> searchCoordinates = new ArrayList<>(searchRadius * 2 + 1);
		searchCoordinates.add(0);
		for (int i = 1; i <= searchRadius; ++i) {
			searchCoordinates.add(i);
			searchCoordinates.add(-i);
		}
		/*
		 * When travelling to the overworld, we allow 2 additional blocks of y-forgiveness
		 * in case of rounding errors. Since portals have to be 3 tall and the portal center
		 * system seeks to the bottom of the portal, there's no need to search downwards.
		 */
		int searchY = block.getWorld().getEnvironment() == World.Environment.NORMAL ? 4 : 2;
		for (int dX : searchCoordinates) {
			for (int dZ : searchCoordinates) {
				for (int dY = 0; dY < searchY; ++dY) {
					Block portal = block.getRelative(dX, dY, dZ);
					if (portal.getType() == Material.NETHER_PORTAL) {
						Location center = findPortalCenter(portal);
						center.setYaw(location.getYaw());
						center.setPitch(location.getPitch());
						return center;
					}
				}
			}
		}
		return null;
	}

	@NotNull
	private Location findPortalCenter(@NotNull Block portal) {
		int minX = 0;
		while (portal.getRelative(minX - 1, 0, 0).getType() == Material.NETHER_PORTAL) {
			minX -= 1;
		}
		int maxX = 0;
		while (portal.getRelative(maxX + 1, 0, 0).getType() == Material.NETHER_PORTAL) {
			maxX += 1;
		}
		int minY = 0;
		while (portal.getRelative(0, minY - 1, 0).getType() == Material.NETHER_PORTAL) {
			minY -= 1;
		}
		int minZ = 0;
		while (portal.getRelative(0, 0, minZ - 1).getType() == Material.NETHER_PORTAL) {
			minZ -= 1;
		}
		int maxZ = 0;
		while (portal.getRelative(0, 0, maxZ + 1).getType() == Material.NETHER_PORTAL) {
			maxZ += 1;
		}

		double y = portal.getY() + minY + 0.1;

		return BoundingBox.of(new Vector(portal.getX() + maxX, y, portal.getZ() + maxZ),
				new Vector(portal.getX() + minX, y, portal.getZ() + minZ)).getCenter().toLocation(portal.getWorld());
	}

	private void createPortal(@Nullable Entity entity, @NotNull Location from, @NotNull Location to) {
		Direction direction;
		BlockData fromData = from.getBlock().getBlockData();
		if (fromData instanceof Orientable) {
			direction = ((Orientable)fromData).getAxis() == Axis.X ? Direction.EAST : Direction.NORTH;
		} else {
			direction = Direction.getFacingDirection(to);
		}
		PortalCreateEvent createEvent = new PortalCreateEvent(
				getNetherPortal().getBuildLocations(to.getBlock(), direction).keySet().stream().map(Block::getState).collect(Collectors.toList()),
				Objects.requireNonNull(to.getWorld()), entity, PortalCreateEvent.CreateReason.NETHER_PAIR);
		getServer().getPluginManager().callEvent(createEvent);
		if (!createEvent.isCancelled()) {
			getNetherPortal().build(to.getBlock(), direction);
		}
	}

	@NotNull
	private Shape getNetherPortal() {
		Shape shape = new Shape();

		Shape.MaterialDataValue value = new Shape.MaterialDataValue(Material.OBSIDIAN);

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

		value = new Shape.MaterialDataValue(Material.AIR);

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

		value = new Shape.MaterialDataValue(Material.NETHER_PORTAL).withBlockData(Orientable.class, Direction.WEST);

		// Portal
		shape.setVectorData(new Vector(-1, 0, 0), value);
		shape.setVectorData(new Vector(0, 0, 0), value);
		shape.setVectorData(new Vector(-1, 1, 0), value);
		shape.setVectorData(new Vector(0, 1, 0), value);
		shape.setVectorData(new Vector(-1, 2, 0), value);
		shape.setVectorData(new Vector(0, 2, 0), value);

		return shape;
	}

}
