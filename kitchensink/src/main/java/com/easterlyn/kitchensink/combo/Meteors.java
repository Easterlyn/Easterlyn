package com.easterlyn.kitchensink.combo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Meteors extends BaseCommand implements Listener {

	private final Plugin plugin;
	private final NamespacedKey keyStrength, keyIgnite, keyDamageTerrain;

	public Meteors(Plugin plugin) {
		this.plugin = plugin;
		keyStrength = new NamespacedKey(plugin, "meteorPower");
		keyIgnite = new NamespacedKey(plugin, "meteorIgnite");
		keyDamageTerrain = new NamespacedKey(plugin, "meteorTerrainDamage");
	}

	@CommandAlias("meteor")
	@Description("Summon a meteor.")
	@CommandPermission("easterlyn.command.meteor")
	private void meteorite(BukkitCommandIssuer issuer) {
		if (!issuer.isPlayer()) {
			issuer.sendMessage("Please specify a location.");
			return;
		}

		Block targetBlock = issuer.getPlayer().getTargetBlock(100);
		Location target;
		if (targetBlock != null) {
			target = targetBlock.getLocation();
		} else {
			target = issuer.getPlayer().getLocation();
		}
		createMeteorite(target, Material.NETHERRACK, 3, false, false);
	}

	@CommandAlias("meteor")
	@Description("Summon a meteor.")
	@CommandPermission("easterlyn.command.meteor")
	private void meteorite(BukkitCommandIssuer issuer, Player target, @Default("3") int radius,
			@Default("false") boolean ignite, @Default("false") boolean damageTerrain
			, @Default("NETHERRACK") Material material) {
		if (target == null) {
			issuer.sendMessage("Invalid target player!");
			return;
		}

		createMeteorite(target.getLocation(), material, radius, ignite, damageTerrain);
	}

	@CommandAlias("meteor")
	@Description("Summon a meteor.")
	@CommandPermission("easterlyn.command.meteor")
	private void meteorite(BukkitCommandIssuer issuer, Location target, @Default("3") int radius,
			@Default("false") boolean ignite, @Default("false") boolean damageTerrain
			, @Default("NETHERRACK") Material material) {
		if (target.getWorld() == null) {
			issuer.sendMessage("Please specify a world.");
			return;
		}
		createMeteorite(target, material, radius, ignite, damageTerrain);
	}

	private void createMeteorite(@NotNull Location target, @Nullable Material type, int radius, boolean ignite, boolean damageTerrain) {
		final BlockData finalType = (type == null || !type.isBlock() ? Material.NETHERRACK : type).createBlockData();
		final int finalRadius = radius < 0 ? 3 : Math.min(radius, 50);
		int desired = target.getWorld().getHighestBlockYAt(target.getBlockX(), target.getBlockZ()) + 40 + finalRadius;
		int highestPossible = 255 - finalRadius;
		desired = Math.min(desired, highestPossible);
		target.add(0, desired - target.getY(), 0);

		new BukkitRunnable() {
			@Override
			public void run() {
				Set<Location> locations = genSphereCoords(target, finalRadius);
				new BukkitRunnable() {
					@Override
					public void run() {
						locations.forEach(location -> {
							FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location, finalType);
							// Being struck by a meteorite hurts.
							fallingBlock.setHurtEntities(true);
							fallingBlock.setDropItem(false);
							PersistentDataContainer dataContainer = fallingBlock.getPersistentDataContainer();
							dataContainer.set(keyStrength, PersistentDataType.FLOAT, 4.0F);
							dataContainer.set(keyIgnite, PersistentDataType.BYTE, (byte) (ignite ? 1 : 0));
							dataContainer.set(keyDamageTerrain, PersistentDataType.BYTE, (byte) (damageTerrain ? 1 : 0));
							// TODO Particle.LAVA
						});
					}
				}.runTask(plugin);
			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Generates a set of coordinates in a hollow sphere.
	 *
	 * @param radius the radius of the sphere
	 * @return the generated locations
	 */
	private Set<Location> genSphereCoords(@NotNull Location target, int radius) {
		if (radius < 0) {
			return new HashSet<>();
		}
		if (radius == 0) {
			return Collections.singleton(target);
		}
		HashSet<Location> coords = new HashSet<>();
		double radiusSquared = Math.pow(radius, 2);
		double x = target.getX();
		double y = target.getY();
		double z = target.getZ();

		for (int dZ = 0; dZ <= radius; dZ++) {
			double dZSquared = Math.pow(dZ, 2);
			for (int dX = 0; dX <= radius; dX++) {
				double dXSquared = Math.pow(dX, 2);
				for (int dY = 0; dY <= radius; dY++) {
					if ((dXSquared + Math.pow(dY, 2) + dZSquared) <= radiusSquared) {
						coords.add(new Location(target.getWorld(), x + dX, y + dY, z + dZ));
						coords.add(new Location(target.getWorld(), x + dX, y + dY, z - dZ));
						coords.add(new Location(target.getWorld(), x - dX, y + dY, z + dZ));
						coords.add(new Location(target.getWorld(), x - dX, y + dY, z - dZ));
						coords.add(new Location(target.getWorld(), x + dX, y - dY, z + dZ));
						coords.add(new Location(target.getWorld(), x + dX, y - dY, z - dZ));
						coords.add(new Location(target.getWorld(), x - dX, y - dY, z + dZ));
						coords.add(new Location(target.getWorld(), x - dX, y - dY, z - dZ));
					}
				}
			}
		}



		radius -= 1;

		for (int dZ = 0; dZ <= radius; dZ++) {
			double dZSquared = Math.pow(dZ, 2);
			for (int dX = 0; dX <= radius; dX++) {
				double dXSquared = Math.pow(dX, 2);
				for (int dY = 0; dY <= radius; dY++) {
					if ((dXSquared + Math.pow(dY, 2) + dZSquared) <= radiusSquared) {
						coords.remove(new Location(target.getWorld(), x + dX, y + dY, z + dZ));
						coords.remove(new Location(target.getWorld(), x + dX, y + dY, z - dZ));
						coords.remove(new Location(target.getWorld(), x - dX, y + dY, z + dZ));
						coords.remove(new Location(target.getWorld(), x - dX, y + dY, z - dZ));
						coords.remove(new Location(target.getWorld(), x + dX, y - dY, z + dZ));
						coords.remove(new Location(target.getWorld(), x + dX, y - dY, z - dZ));
						coords.remove(new Location(target.getWorld(), x - dX, y - dY, z + dZ));
						coords.remove(new Location(target.getWorld(), x - dX, y - dY, z - dZ));
					}
				}
			}
		}

		return coords;
	}



	@EventHandler(ignoreCancelled = true)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		PersistentDataContainer dataContainer = event.getEntity().getPersistentDataContainer();
		if (dataContainer.has(keyStrength, PersistentDataType.FLOAT)) {
			event.setCancelled(true);
			event.getEntity().remove();
			float strength = dataContainer.getOrDefault(keyStrength, PersistentDataType.FLOAT, 0F);
			boolean ignite = dataContainer.getOrDefault(keyIgnite, PersistentDataType.BYTE, (byte) 0) > 0;
			boolean damageTerrain = dataContainer.getOrDefault(keyDamageTerrain, PersistentDataType.BYTE, (byte) 0) > 0;

			event.getBlock().getWorld().createExplosion(event.getEntity(), strength, ignite, damageTerrain);
		}
	}

}
