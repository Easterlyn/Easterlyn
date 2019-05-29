package com.easterlyn.machines.type;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.events.packets.ParticleEffectWrapper;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.micromodules.Holograms;
import com.easterlyn.micromodules.ParticleUtils;
import com.easterlyn.utilities.InventoryUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Automatic Saros fun times.
 *
 * @author Jikoo
 */
public class RepairShrine extends Machine {

	private final Holograms holograms;
	private final ParticleUtils particles;
	private final ItemStack drop;

	public RepairShrine(Easterlyn plugin, Machines machines) {
		super(plugin, machines, new Shape(), "RepairShrine");
		this.holograms = plugin.getModule(Holograms.class);
		this.particles = plugin.getModule(ParticleUtils.class);

		getShape().setVectorData(new Vector(0, 0, 0), Material.HOPPER);
		getShape().setVectorData(new Vector(0, 1, 0), Material.RED_CARPET);
		getShape().setVectorData(new Vector(0, 3, 0), Material.RED_STAINED_GLASS);

		drop = new ItemStack(Material.RED_STAINED_GLASS);
		InventoryUtils.consumeAs(ItemMeta.class, drop.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.WHITE + "RepairShrine");
			drop.setItemMeta(itemMeta);
		});
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

	private void setFuel(ConfigurationSection storage, long fuel) {
		ArmorStand hologram = holograms.getOrCreateHologram(this.getHoloLocation(storage));
		if (hologram != null) {
			hologram.setCustomName(String.valueOf(fuel));
		}
		storage.set("fuel", fuel);
	}

	private long getFuel(ConfigurationSection storage) {
		return storage.getLong("fuel", 0);
	}

	@Override
	public boolean assemble(Player player, ConfigurationSection storage) {
		this.setFuel(storage, this.getFuel(storage));
		return super.assemble(player, storage);
	}

	@Override
	public boolean handleBreak(BlockBreakEvent event, ConfigurationSection storage) {
		// Prevent accidental breakage - must break part of bottom layer
		return event.getBlock().getY() > this.getKey(storage).getBlockY() &&  super.handleBreak(event, storage);
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
		ItemMeta meta = inserted.getItemMeta();
		Damageable damageable = null;
		Repairable repairable = null;
		if (inserted.hasItemMeta()) {
			if (meta instanceof Damageable) {
				damageable = (Damageable) meta;
				cost += damageable.getDamage() / 10 + 1;
			}
			if (meta instanceof Repairable) {
				repairable = (Repairable) meta;
				if (repairable.getRepairCost() > 0) {
					cost += repairable.getRepairCost() * 9;
				}
			}
		}

		if (cost == 0) {
			return true;
		}

		if (cost > fuel) {
			Location holo = this.getHoloLocation(storage);
			if (holo.getWorld() != null) {
				holo.getWorld().playSound(holo, Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 1F, 1F);
			}
			ArmorStand hologram = holograms.getOrCreateHologram(holo);
			if (hologram != null) {
				hologram.setCustomName(Language.getColor("emphasis.bad") + String.valueOf(fuel - cost));
			}

			new BukkitRunnable() {
				@Override
				public void run() {
					setFuel(storage, getFuel(storage));
				}
			}.runTaskLater(this.getPlugin(), 50L);
			return true;
		}

		this.setFuel(storage, fuel - cost);

		if (damageable != null) {
			damageable.setDamage(0);
			inserted.setItemMeta(meta);
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
		if (hologram != null) {
			hologram.setCustomName(String.valueOf(getFuel(storage)));
		}
	}

	@Override
	public void disable(ConfigurationSection storage) {
		ArmorStand hologram = holograms.getHologram(getHoloLocation(storage));
		if (hologram != null) {
			hologram.remove();
		}
	}

}
