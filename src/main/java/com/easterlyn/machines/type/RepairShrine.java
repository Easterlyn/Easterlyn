package com.easterlyn.machines.type;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.events.packets.ParticleEffectWrapper;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.utilities.Direction;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.machines.utilities.Shape.MaterialDataValue;
import com.easterlyn.micromodules.Holograms;
import com.easterlyn.micromodules.ParticleUtils;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

/**
 * Automatic Saros fun times.
 * 
 * @author Jikoo
 */
public class RepairShrine extends Machine {

	private final Holograms holograms;
	private final ParticleUtils particles;
	private final ItemStack drop;

	@SuppressWarnings("deprecation")
	public RepairShrine(Easterlyn plugin, Machines machines) {
		super(plugin, machines, new Shape(), "RepairShrine");
		this.holograms = plugin.getModule(Holograms.class);
		this.particles = plugin.getModule(ParticleUtils.class);

		Shape shape = getShape();
		MaterialDataValue matData = shape.new MaterialDataValue(Material.RED_SANDSTONE);

		// Bottom base layer
		shape.setVectorData(new Vector(-3, 0, -3), matData);
		shape.setVectorData(new Vector(-2, 0, -3), matData);
		shape.setVectorData(new Vector(-1, 0, -3), matData);
		shape.setVectorData(new Vector(0, 0, -3), matData);
		shape.setVectorData(new Vector(1, 0, -3), matData);
		shape.setVectorData(new Vector(2, 0, -3), matData);
		shape.setVectorData(new Vector(3, 0, -3), matData);
		shape.setVectorData(new Vector(-3, 0, -2), matData);
		shape.setVectorData(new Vector(-2, 0, -2), matData);
		shape.setVectorData(new Vector(-1, 0, -2), matData);
		shape.setVectorData(new Vector(0, 0, -2), matData);
		shape.setVectorData(new Vector(1, 0, -2), matData);
		shape.setVectorData(new Vector(2, 0, -2), matData);
		shape.setVectorData(new Vector(3, 0, -2), matData);
		shape.setVectorData(new Vector(-3, 0, -1), matData);
		shape.setVectorData(new Vector(-2, 0, -1), matData);
		shape.setVectorData(new Vector(-1, 0, -1), matData);
		shape.setVectorData(new Vector(0, 0, -1), matData);
		shape.setVectorData(new Vector(1, 0, -1), matData);
		shape.setVectorData(new Vector(2, 0, -1), matData);
		shape.setVectorData(new Vector(3, 0, -1), matData);
		shape.setVectorData(new Vector(-3, 0, 0), matData);
		shape.setVectorData(new Vector(-2, 0, 0), matData);
		shape.setVectorData(new Vector(-1, 0, 0), matData);
		shape.setVectorData(new Vector(1, 0, 0), matData);
		shape.setVectorData(new Vector(2, 0, 0), matData);
		shape.setVectorData(new Vector(3, 0, 0), matData);
		shape.setVectorData(new Vector(-3, 0, 1), matData);
		shape.setVectorData(new Vector(-2, 0, 1), matData);
		shape.setVectorData(new Vector(-1, 0, 1), matData);
		shape.setVectorData(new Vector(0, 0, 1), matData);
		shape.setVectorData(new Vector(1, 0, 1), matData);
		shape.setVectorData(new Vector(2, 0, 1), matData);
		shape.setVectorData(new Vector(3, 0, 1), matData);
		shape.setVectorData(new Vector(-3, 0, 2), matData);
		shape.setVectorData(new Vector(-2, 0, 2), matData);
		shape.setVectorData(new Vector(-1, 0, 2), matData);
		shape.setVectorData(new Vector(0, 0, 2), matData);
		shape.setVectorData(new Vector(1, 0, 2), matData);
		shape.setVectorData(new Vector(2, 0, 2), matData);
		shape.setVectorData(new Vector(3, 0, 2), matData);
		shape.setVectorData(new Vector(-3, 0, 3), matData);
		shape.setVectorData(new Vector(-2, 0, 3), matData);
		shape.setVectorData(new Vector(-1, 0, 3), matData);
		shape.setVectorData(new Vector(0, 0, 3), matData);
		shape.setVectorData(new Vector(1, 0, 3), matData);
		shape.setVectorData(new Vector(2, 0, 3), matData);
		shape.setVectorData(new Vector(3, 0, 3), matData);

		// Top base layer
		shape.setVectorData(new Vector(-2, 1, -3), matData);
		shape.setVectorData(new Vector(-1, 1, -3), matData);
		shape.setVectorData(new Vector(1, 1, -3), matData);
		shape.setVectorData(new Vector(2, 1, -3), matData);
		shape.setVectorData(new Vector(-3, 1, -2), matData);
		shape.setVectorData(new Vector(3, 1, -2), matData);
		shape.setVectorData(new Vector(-3, 1, -1), matData);
		shape.setVectorData(new Vector(3, 1, -1), matData);
		shape.setVectorData(new Vector(-3, 1, 1), matData);
		shape.setVectorData(new Vector(3, 1, 1), matData);
		shape.setVectorData(new Vector(-3, 1, 2), matData);
		shape.setVectorData(new Vector(3, 1, 2), matData);
		shape.setVectorData(new Vector(-2, 1, 3), matData);
		shape.setVectorData(new Vector(-1, 1, 3), matData);
		shape.setVectorData(new Vector(1, 1, 3), matData);
		shape.setVectorData(new Vector(2, 1, 3), matData);

		// Decorative arch keystone
		shape.setVectorData(new Vector(0, 6, 0), matData);

		// Top base redstone cog
		matData = shape.new MaterialDataValue(Material.REDSTONE_BLOCK);
		shape.setVectorData(new Vector(-3, 1, -3), matData);
		shape.setVectorData(new Vector(0, 1, -3), matData);
		shape.setVectorData(new Vector(3, 1, -3), matData);
		shape.setVectorData(new Vector(-2, 1, -2), matData);
		shape.setVectorData(new Vector(-1, 1, -2), matData);
		shape.setVectorData(new Vector(0, 1, -2), matData);
		shape.setVectorData(new Vector(1, 1, -2), matData);
		shape.setVectorData(new Vector(2, 1, -2), matData);
		shape.setVectorData(new Vector(-2, 1, -1), matData);
		shape.setVectorData(new Vector(-1, 1, -1), matData);
		shape.setVectorData(new Vector(0, 1, -1), matData);
		shape.setVectorData(new Vector(1, 1, -1), matData);
		shape.setVectorData(new Vector(2, 1, -1), matData);
		shape.setVectorData(new Vector(-3, 1, 0), matData);
		shape.setVectorData(new Vector(-2, 1, 0), matData);
		shape.setVectorData(new Vector(-1, 1, 0), matData);
		shape.setVectorData(new Vector(1, 1, 0), matData);
		shape.setVectorData(new Vector(2, 1, 0), matData);
		shape.setVectorData(new Vector(3, 1, 0), matData);
		shape.setVectorData(new Vector(-2, 1, 1), matData);
		shape.setVectorData(new Vector(-1, 1, 1), matData);
		shape.setVectorData(new Vector(0, 1, 1), matData);
		shape.setVectorData(new Vector(1, 1, 1), matData);
		shape.setVectorData(new Vector(2, 1, 1), matData);
		shape.setVectorData(new Vector(-2, 1, 2), matData);
		shape.setVectorData(new Vector(-1, 1, 2), matData);
		shape.setVectorData(new Vector(0, 1, 2), matData);
		shape.setVectorData(new Vector(1, 1, 2), matData);
		shape.setVectorData(new Vector(2, 1, 2), matData);
		shape.setVectorData(new Vector(-3, 1, 3), matData);
		shape.setVectorData(new Vector(0, 1, 3), matData);
		shape.setVectorData(new Vector(3, 1, 3), matData);

		// Central hopper
		matData = shape.new MaterialDataValue(Material.HOPPER);
		shape.setVectorData(new Vector(0, 0, 0), matData);

		// Central carpet
		matData = shape.new MaterialDataValue(Material.CARPET, DyeColor.RED.getWoolData());
		shape.setVectorData(new Vector(0, 1, 0), matData);

		// Central glass
		matData = shape.new MaterialDataValue(Material.GLASS);
		shape.setVectorData(new Vector(0, 3, 0), matData);

		// Decorative arch
		matData = shape.new MaterialDataValue(Material.RED_SANDSTONE_STAIRS, Direction.NORTH, "upperstair");
		shape.setVectorData(new Vector(0, 4, 2), matData);
		shape.setVectorData(new Vector(0, 5, 1), matData);

		matData = shape.new MaterialDataValue(Material.RED_SANDSTONE_STAIRS, Direction.EAST, "upperstair");
		shape.setVectorData(new Vector(2, 4, 0), matData);
		shape.setVectorData(new Vector(1, 5, 0), matData);

		matData = shape.new MaterialDataValue(Material.RED_SANDSTONE_STAIRS, Direction.SOUTH, "upperstair");
		shape.setVectorData(new Vector(0, 4, -2), matData);
		shape.setVectorData(new Vector(0, 5, -1), matData);

		matData = shape.new MaterialDataValue(Material.RED_SANDSTONE_STAIRS, Direction.WEST, "upperstair");
		shape.setVectorData(new Vector(-2, 4, 0), matData);
		shape.setVectorData(new Vector(-1, 5, 0), matData);

		matData = shape.new MaterialDataValue(Material.RED_SANDSTONE_STAIRS, Direction.SOUTH, "stair");
		shape.setVectorData(new Vector(0, 5, 2), matData);
		matData = shape.new MaterialDataValue(Material.RED_SANDSTONE_STAIRS, Direction.WEST, "stair");
		shape.setVectorData(new Vector(2, 5, 0), matData);
		matData = shape.new MaterialDataValue(Material.RED_SANDSTONE_STAIRS, Direction.NORTH, "stair");
		shape.setVectorData(new Vector(0, 5, -2), matData);
		matData = shape.new MaterialDataValue(Material.RED_SANDSTONE_STAIRS, Direction.EAST, "stair");
		shape.setVectorData(new Vector(-2, 5, 0), matData);

		matData = shape.new MaterialDataValue(Material.STONE_SLAB2);
		shape.setVectorData(new Vector(0, 6, 1), matData);
		shape.setVectorData(new Vector(1, 6, 0), matData);
		shape.setVectorData(new Vector(0, 6, -1), matData);
		shape.setVectorData(new Vector(-1, 6, 0), matData);

		drop = new ItemStack(Material.STAINED_GLASS, 1, DyeColor.RED.getWoolData());
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "RepairShrine");
		drop.setItemMeta(meta);
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}

	private Location getHoloLocation(ConfigurationSection storage) {
		return this.getKey(storage).add(new Vector(0.5, 3, 0.5));
	}

	private Location getDropLocation(ConfigurationSection storage) {
		return this.getKey(storage).add(new Vector(0.5, 4.1, 0.5));
	}

	public void setFuel(ConfigurationSection storage, long fuel) {
		ArmorStand hologram = holograms.getOrCreateHologram(this.getHoloLocation(storage));
		hologram.setCustomName(String.valueOf(fuel));
		storage.set("fuel", fuel);
	}

	public long getFuel(ConfigurationSection storage) {
		return storage.getLong("fuel", 0);
	}

	@Override
	public void assemble(BlockPlaceEvent event, ConfigurationSection storage) {
		super.assemble(event, storage);
		this.setFuel(storage, this.getFuel(storage));
	}

	@Override
	public boolean handleBreak(BlockBreakEvent event, ConfigurationSection storage) {
		if (event.getBlock().getY() > this.getKey(storage).getBlockY()) {
			// Prevent accidental breakage - must break part of bottom layer
			return true;
		}
		return super.handleBreak(event, storage);
	}

	@Override
	public boolean handleHopperPickupItem(InventoryPickupItemEvent event, ConfigurationSection storage) {
		ItemStack inserted = event.getItem().getItemStack();
		long fuel = this.getFuel(storage);
		switch (inserted.getType()) {
		case REDSTONE_BLOCK:
			fuel += 8 * inserted.getAmount();
		case REDSTONE:
			fuel += inserted.getAmount();
			setFuel(storage, fuel);
			event.getItem().getWorld().playSound(this.getHoloLocation(storage), Sound.BLOCK_LAVA_AMBIENT, 1F, 1F);
			event.getItem().remove();
			return true;
		default:
			break;
		}

		event.getItem().teleport(this.getDropLocation(storage));
		event.getItem().setVelocity(new Vector());
		event.getItem().setFireTicks(0);

		int cost = 0;
		if (inserted.getType().getMaxDurability() > 0 && inserted.getDurability() > 0) {
			cost += inserted.getDurability() / 10 + 1;
		}
		ItemMeta meta = null;
		Repairable repairable = null;
		if (inserted.hasItemMeta()) {
			meta = inserted.getItemMeta();
			if (meta instanceof Repairable) {
				repairable = (Repairable) meta;
				if (repairable.getRepairCost() > 0) {
					cost += repairable.getRepairCost() * 9;
				} else {
					repairable = null;
				}
			}
		}

		if (cost == 0) {
			return true;
		}

		if (cost > fuel) {
			Location holo = this.getHoloLocation(storage);
			holo.getWorld().playSound(holo, Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 1F, 1F);
			ArmorStand hologram = holograms.getOrCreateHologram(holo);
			hologram.setCustomName(Language.getColor("emphasis.bad") + String.valueOf(fuel - cost));

			new BukkitRunnable() {
				@Override
				public void run() {
					setFuel(storage, getFuel(storage));
				}
			}.runTaskLater(this.getPlugin(), 50L);
			return true;
		}

		this.setFuel(storage, fuel - cost);

		if (inserted.getType().getMaxDurability() > 0) {
			inserted.setDurability((short) 0);
		}
		if (repairable != null) {
			repairable.setRepairCost(0);
			inserted.setItemMeta(meta);
		}

		event.getItem().setItemStack(inserted);

		particles.addEntity(event.getItem(), new ParticleEffectWrapper(Particle.LAVA, null, 1F, 0F, 0F, 0F, 1, 32));

		new BukkitRunnable() {
			@Override
			public void run() {
				particles.removeAllEffects(event.getItem());
			}
		}.runTaskLater(this.getPlugin(), 50L);

		return true;
	}

	@Override
	public void enable(ConfigurationSection storage) {
		ArmorStand hologram = holograms.getOrCreateHologram(getHoloLocation(storage));
		hologram.setCustomName(String.valueOf(getFuel(storage)));
	}

	@Override
	public void disable(ConfigurationSection storage) {
		ArmorStand hologram = holograms.getHologram(getHoloLocation(storage));
		if (hologram != null) {
			hologram.remove();
		}
	}

}
