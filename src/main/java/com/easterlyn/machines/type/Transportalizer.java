package com.easterlyn.machines.type;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.utilities.Direction;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.machines.utilities.Shape.MaterialDataValue;
import com.easterlyn.micromodules.Holograms;
import com.easterlyn.micromodules.Protections;
import com.easterlyn.micromodules.protectionhooks.ProtectionHook;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Machine for Entity teleportation.
 * <p>
 * Costs fuel based on distance: 1 unit of fuel per 50 blocks of direct line
 * travel rounded up. Gunpowder = 1 fuel, redstone = 2, blaze powder = 3,
 * glowstone = 4, blaze rod = 6, glowstone block = 16, redstone block = 18.
 *
 * @author Jikoo
 */
public class Transportalizer extends Machine {

	private final Holograms holograms;
	private final Protections protections;
	private final ItemStack drop;
	private final Map<UUID, TransportalizationRequest> requests;

	public Transportalizer(Easterlyn plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Transportalizer");
		this.holograms = plugin.getModule(Holograms.class);
		this.protections = plugin.getModule(Protections.class);
		Shape shape = getShape();
		shape.setVectorData(new Vector(0, 0, 0),
				shape.new MaterialDataValue(Material.HOPPER).withBlockData(Directional.class, Direction.NORTH));
		MaterialDataValue m = shape.new MaterialDataValue(Material.QUARTZ_BLOCK);
		shape.setVectorData(new Vector(-1, 0, 0), m);
		shape.setVectorData(new Vector(1, 0, 0), m);
		m = shape.new MaterialDataValue(Material.CHISELED_QUARTZ_BLOCK);
		shape.setVectorData(new Vector(-1, 0, 1), m);
		shape.setVectorData(new Vector(1, 0, 1), m);
		shape.setVectorData(new Vector(-1, 2, 1), m);
		shape.setVectorData(new Vector(1, 2, 1), m);
		shape.setVectorData(new Vector(0, 2, 1),
				shape.new MaterialDataValue(Material.QUARTZ_PILLAR).withBlockData(Orientable.class, Direction.WEST));
		shape.setVectorData(new Vector(0, 0, 1),
				shape.new MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Bisected.class, Direction.UP)
				.withBlockData(Directional.class, Direction.NORTH));
		shape.setVectorData(new Vector(0, 1, 1), Material.WHITE_STAINED_GLASS);
		m = shape.new MaterialDataValue(Material.STONE_BUTTON).withBlockData(Directional.class, Direction.SOUTH);
		shape.setVectorData(new Vector(-1, 2, 0), m);
		shape.setVectorData(new Vector(1, 2, 0), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.NORTH);
		shape.setVectorData(new Vector(-1, 0, -1), m);
		shape.setVectorData(new Vector(0, 0, -1), m);
		shape.setVectorData(new Vector(1, 0, -1), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_PILLAR).withBlockData(Orientable.class, Direction.UP);
		shape.setVectorData(new Vector(-1, 1, 1), m);
		shape.setVectorData(new Vector(1, 1, 1), m);
		shape.setVectorData(new Vector(-1, 1, 0), Material.RED_CARPET);
		shape.setVectorData(new Vector(0, 1, 0), Material.GRAY_CARPET);
		shape.setVectorData(new Vector(1, 1, 0), Material.LIME_CARPET);

		drop = new ItemStack(Material.CHEST);
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Transportalizer");
		drop.setItemMeta(meta);
		this.requests = new HashMap<>();
	}

	@Override
	public int getCost() {
		return 1000;
	}

	private Location getHoloLocation(ConfigurationSection storage) {
		return getKey(storage).add(Shape.getRelativeVector(getDirection(storage), new Vector(0.5, 1.1, 1.5)));
	}

	private void setFuel(ConfigurationSection storage, long fuel) {
		ArmorStand hologram = holograms.getOrCreateHologram(getHoloLocation(storage));
		hologram.setCustomName(String.valueOf(fuel));
		storage.set("fuel", fuel);
	}

	private long getFuel(ConfigurationSection storage) {
		return storage.getLong("fuel", 0);
	}

	@Override
	public boolean assemble(Player player, ConfigurationSection storage) {
		setFuel(storage, getFuel(storage));
		return super.assemble(player, storage);
	}

	@Override
	public boolean handleHopperPickupItem(InventoryPickupItemEvent event, ConfigurationSection storage) {
		ItemStack inserted = event.getItem().getItemStack();
		Location key = getKey(storage);
		if (hasValue(inserted.getType())) {
			setFuel(storage, getFuel(storage) + getValue(inserted.getType()) * inserted.getAmount());
			key.getWorld().playSound(key, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
			event.getItem().remove();
		} else {
			event.getItem().teleport(
					key.add(Shape.getRelativeVector(
							getDirection(storage).getRelativeDirection(Direction.NORTH),
							new Vector(0.5, 0.5, -1.5))));
		}
		return true;
	}

	@Override
	public boolean handleHopperMoveItem(InventoryMoveItemEvent event, ConfigurationSection storage) {
		// future: allow pulling from chest or something like that
		return super.handleHopperMoveItem(event, storage);
	}

	/**
	 * Checks if a Material is a fuel
	 *
	 * @param m the Material to check
	 *
	 * @return true if the Material is a fuel
	 */
	private boolean hasValue(Material m) {
		return m == Material.GUNPOWDER || m == Material.REDSTONE || m == Material.BLAZE_POWDER
				|| m == Material.GLOWSTONE_DUST || m == Material.BLAZE_ROD
				|| m == Material.GLOWSTONE || m == Material.REDSTONE_BLOCK;
	}

	/**
	 * Gets the fuel value for the Material provided.
	 *
	 * @param m the Material to check
	 *
	 * @return the fuel value of the Material
	 */
	private int getValue(Material m) {
		switch (m) {
			case GUNPOWDER:
				return 1;
			case REDSTONE:
				return 2;
			case BLAZE_POWDER:
				return 3;
			case GLOWSTONE_DUST:
				return 4;
			case BLAZE_ROD:
				return 6;
			case GLOWSTONE:
				return 16;
			case REDSTONE_BLOCK:
				return 18;
			default:
				return 0;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		if (super.handleInteract(event, storage)) {
			return true;
		}

		// Hopper inventory has to suck up items from the world, it should not be openable.
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& event.getClickedBlock().getType() == Material.HOPPER) {
			return true;
		}

		if (event.getClickedBlock().getType() != Material.STONE_BUTTON) {
			return false;
		}

		Location key = getKey(storage);

		// Check for a sign in the proper location
		Block signBlock = key.clone().add(new Vector(0, 2, 0)).getBlock();
		if (signBlock.getType() != Material.WALL_SIGN) {
			event.getPlayer().sendMessage(Language.getColor("bad")
					+ "Please place a sign on your transportalizer between the buttons to use it."
					+ "\nThe third row should contain your desired coordinates in x, y, z format."
					+ "\nAll the other rows can contain whatever you like.");
			return false;
		}

		Sign sign = (Sign) signBlock.getState();
		// Check sign for proper format - sign lines are 0-3, third line is line 2
		String line3 = sign.getLine(2);
		if (!line3.matches("-?[0-9]+(\\s|,\\s?)[0-9]+(\\s|,\\s?)-?[0-9]+")) {
			event.getPlayer().sendMessage(Language.getColor("bad")
					+ "The third line of your transportalizer sign must contain "
					+ "your desired destination in x, y, z format. Ex: 0, 64, 0");
			return false;
		}

		// Parse remote location. Do not allow invalid height or coords.
		WorldBorder border = key.getWorld().getWorldBorder();
		double borderRadius = border.getSize() / 2;
		String[] locString = line3.split("(\\s|,\\s?)");
		int x0 = Integer.parseInt(locString[0]);
		int x = (int) Math.max(border.getCenter().getX() - borderRadius, Math.min(border.getCenter().getX() + borderRadius, x0));
		int y0 = Integer.parseInt(locString[1]);
		int y = Math.max(1, Math.min(255, y0));
		int z0 = Integer.parseInt(locString[2]);
		int z = (int) Math.max(border.getCenter().getZ() - borderRadius, Math.min(border.getCenter().getZ() + borderRadius, z0));
		if (x != x0 | y != y0 || z != z0) {
			sign.setLine(2, x + ", " + y + ", " + z);
			sign.update(true);
			event.getPlayer().sendMessage(Language.getColor("bad")
					+ "Your destination has been corrected for safety. Please review it.");
			return true;
		}
		Location remote = new Location(event.getClickedBlock().getWorld(), x, y, z);

		// 50 fuel per block of distance, rounded up.
		int cost = (int) (key.distance(remote) / 50 + 1);
		// CHECK FUEL
		long fuel = getFuel(storage);
		if (fuel < cost) {
			event.getPlayer().sendMessage(Language.getColor("bad")
					+ "The Transportalizer begins humming through standard teleport procedure,"
					+ " when all of a sudden it growls to a halt."
					+ "\nPerhaps it requires more fuel?");
			key.getWorld().playSound(key, Sound.ENTITY_WOLF_GROWL, 1F, 0F);
			return false;
		}

		// TELEPORT
		Block pad = event.getClickedBlock().getRelative(BlockFace.DOWN);
		Location source;
		Location target;
		if (pad.getState().getData().getData() == 5) {
			source = pad.getLocation();
			target = remote;
		} else {
			source = remote;
			target = pad.getLocation();
		}
		for (Entity entity : key.getWorld().getEntities()) {
			if (!entity.getLocation().getBlock().equals(source.getBlock())) {
				continue;
			}
			if (source == remote) {
				if (!canPull(event.getPlayer(), entity, source, target, storage, cost)) {
					return false;
				}
			} else {
				if (!canPush(event.getPlayer(), entity, target)) {
					return false;
				}
			}
			setFuel(storage, fuel - cost);
			teleport(entity, source, target);
			return false;
		}
		return false;
	}

	private boolean canPush(Player player, Entity entity, Location to) {
		if (entity instanceof Player) {
			// Sender must have button access to send players
			for (ProtectionHook hook : protections.getHooks()) {
				if (!hook.canUseButtonsAt(player, to)) {
					player.sendMessage(Language.getColor("bad") + "You do not have access to the location specified!");
					return false;
				}
			}
			return true;
		}
		if (entity instanceof Monster || entity instanceof Explosive || entity instanceof ExplosiveMinecart) {
			// Hostiles, TNT, wither projectiles, fireballs, etc. require build permissions
			for (ProtectionHook hook : protections.getHooks()) {
				if (!hook.canBuildAt(player, to)) {
					player.sendMessage(Language.getColor("bad") + "You do not have access to the location specified!");
					return false;
				}
			}
			return true;
		}
		// Ender dragon or ender dragon parts
		if (entity instanceof ComplexLivingEntity || entity instanceof ComplexEntityPart) {
			player.sendMessage(Language.getColor("bad") + "Great effort, but you can't transportalize a dragon.");
			return false;
		}
		for (ProtectionHook hook : protections.getHooks()) {
			if (!hook.canMobsSpawn(to) && !hook.canBuildAt(player, to)) {
				player.sendMessage(Language.getColor("bad") + "You don't have access to the location specified!");
				return false;
			}
		}
		return true;
	}

	private boolean canPull(Player player, Entity entity, Location from, Location to,
			ConfigurationSection storage, int cost) {
		if (entity instanceof Player) {
			if (!player.getUniqueId().equals(entity.getUniqueId())) {
				Player targetPlayer = (Player) entity;
				player.sendMessage(String.format("%1$sConfirming transportalization with %2$s%3$s%1$s!",
						Language.getColor("good"), Language.getColor("player.good"), targetPlayer.getDisplayName()));
				targetPlayer.sendMessage(String.format("%1$s%2$s %3$swould like to transportalize you!"
						+ "\nTo accept, use %4$s/tpyes%3$s. To decline, use %4$s/tpno%3$s.",
						Language.getColor("player.good"), player.getDisplayName(), Language.getColor("good"), Language.getColor("command")));
				requests.put(targetPlayer.getUniqueId(), new TransportalizationRequest(storage, from, to, cost));
				return false;
			}
			return true;
		}
		// Ender dragon or ender dragon parts
		if (entity instanceof ComplexLivingEntity || entity instanceof ComplexEntityPart) {
			player.sendMessage(Language.getColor("bad") + "Great effort, but you can't transportalize a dragon.");
			return false;
		}
		if (entity instanceof ArmorStand) {
			// Pulling armor stands from an area requires build trust
			for (ProtectionHook hook : protections.getHooks()) {
				if (!hook.canBuildAt(player, to)) {
					player.sendMessage(Language.getColor("bad") + "You do not have access to the location specified!");
					return false;
				}
			}
			return true;
		}
		// Pulling out of a protected area requires container access
		for (ProtectionHook hook : protections.getHooks()) {
			if (!hook.canOpenChestsAt(player, to)) {
				player.sendMessage(Language.getColor("bad") + "You do not have access to the location specified!");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean handleBreak(BlockBreakEvent event, ConfigurationSection storage) {
		if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
			Location key = getKey(storage);
			key.getWorld().dropItemNaturally(key, getUniqueDrop());
			int fuel = (int) (getFuel(storage) / getValue(Material.BLAZE_POWDER));
			while (fuel > 0) {
				int dropAmount = Material.BLAZE_POWDER.getMaxStackSize();
				if (fuel < dropAmount) {
					dropAmount = fuel;
				}
				fuel -= dropAmount;
				key.getWorld().dropItemNaturally(key, new ItemStack(Material.BLAZE_POWDER, dropAmount));
			}
		}
		remove(storage);
		return true;
	}

	@Override
	public boolean handleClick(InventoryClickEvent event, ConfigurationSection section) {
		return false;
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

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}

	private void teleport(Entity entity, Location source, Location target) {
		source.getWorld().playSound(source, Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 2F);
		target.getWorld().playSound(target, Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 2F);
		Location current = entity.getLocation();
		target.setPitch(current.getPitch());
		target.setYaw(current.getYaw());
		/*
		 * Pad the location. Padding X and Z cause the player to arrive in the center of the target
		 * block. Padding the Y axis allows players to arrive on top of carpet, etc. and helps
		 * prevent players with higher latency arriving in unloaded chunks getting stuck in blocks.
		 * More y padding results in suffocation damage in 2-block high areas, as the character is
		 * 1.8 blocks tall.
		 */
		target.add(0.5, 0.2, 0.5);
		entity.teleport(target);
		source.getWorld().playEffect(source, Effect.ENDER_SIGNAL, 4);
		source.getWorld().playEffect(target, Effect.ENDER_SIGNAL, 4);
	}

	public boolean pendingTransportalizationFailed(Player player, boolean accept) {
		if (requests.containsKey(player.getUniqueId())) {
			if (accept) {
				requests.remove(player.getUniqueId()).doTeleport(player);
			} else {
				requests.remove(player.getUniqueId());
			}
			return false;
		}
		return true;
	}

	private class TransportalizationRequest {

		private final ConfigurationSection storage;
		private final Location from, to;
		private final long cost;

		TransportalizationRequest(ConfigurationSection storage,
				Location from, Location to, long cost) {
			this.storage = storage;
			this.from = from;
			this.to = to;
			this.cost = cost;
		}

		void doTeleport(Player player) {
			long fuel = getFuel(storage);
			if (fuel < cost) {
				player.sendMessage("The transportalizer is too low on fuel to pull you to it!");
				return;
			}
			setFuel(storage, fuel - cost);
			teleport(player, from, to);
		}
	}

}
