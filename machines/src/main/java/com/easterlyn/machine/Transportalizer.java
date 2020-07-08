package com.easterlyn.machine;

import com.easterlyn.EasterlynCore;
import com.easterlyn.EasterlynMachines;
import com.easterlyn.user.User;
import com.easterlyn.util.Direction;
import com.easterlyn.util.GenericUtil;
import com.easterlyn.util.HologramUtil;
import com.easterlyn.util.ProtectionUtil;
import com.easterlyn.util.Request;
import com.easterlyn.util.Shape;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
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
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	private final ItemStack drop;

	public Transportalizer(EasterlynMachines machines) {
		super(machines, new Shape(), "Transportalizer");
		Shape shape = getShape();
		shape.setVectorData(new Vector(0, 0, 0),
				new Shape.MaterialDataValue(Material.HOPPER).withBlockData(Directional.class, Direction.NORTH));
		Shape.MaterialDataValue m = new Shape.MaterialDataValue(Material.QUARTZ_BLOCK);
		shape.setVectorData(new Vector(-1, 0, 0), m);
		shape.setVectorData(new Vector(1, 0, 0), m);
		m = new Shape.MaterialDataValue(Material.CHISELED_QUARTZ_BLOCK);
		shape.setVectorData(new Vector(-1, 0, 1), m);
		shape.setVectorData(new Vector(1, 0, 1), m);
		shape.setVectorData(new Vector(-1, 2, 1), m);
		shape.setVectorData(new Vector(1, 2, 1), m);
		shape.setVectorData(new Vector(0, 2, 1),
				new Shape.MaterialDataValue(Material.QUARTZ_PILLAR).withBlockData(Orientable.class, Direction.WEST));
		shape.setVectorData(new Vector(0, 0, 1),
				new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Bisected.class, Direction.UP)
				.withBlockData(Directional.class, Direction.NORTH));
		shape.setVectorData(new Vector(0, 1, 1), Material.WHITE_STAINED_GLASS);
		m = new Shape.MaterialDataValue(Material.STONE_BUTTON).withBlockData(Directional.class, Direction.SOUTH);
		shape.setVectorData(new Vector(-1, 2, 0), m);
		shape.setVectorData(new Vector(1, 2, 0), m);
		m = new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.NORTH);
		shape.setVectorData(new Vector(-1, 0, -1), m);
		shape.setVectorData(new Vector(0, 0, -1), m);
		shape.setVectorData(new Vector(1, 0, -1), m);
		m = new Shape.MaterialDataValue(Material.QUARTZ_PILLAR).withBlockData(Orientable.class, Direction.UP);
		shape.setVectorData(new Vector(-1, 1, 1), m);
		shape.setVectorData(new Vector(1, 1, 1), m);
		shape.setVectorData(new Vector(-1, 1, 0), Material.RED_CARPET);
		shape.setVectorData(new Vector(0, 1, 0), Material.GRAY_CARPET);
		shape.setVectorData(new Vector(1, 1, 0), Material.LIME_CARPET);

		drop = new ItemStack(Material.CHEST);
		GenericUtil.consumeAs(ItemMeta.class, drop.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.WHITE + "Transportalizer");
			drop.setItemMeta(itemMeta);
		});

		ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(machines, "transportalizer"), drop);
		recipe.shape("BAB", "DCD", "FEF");
		recipe.setIngredient('A', Material.END_CRYSTAL);
		recipe.setIngredient('B', new RecipeChoice.MaterialChoice(Tag.BUTTONS));
		recipe.setIngredient('C', Material.ENDER_CHEST);
		recipe.setIngredient('D', Material.ENDER_EYE);
		recipe.setIngredient('E', Material.HOPPER);
		recipe.setIngredient('F', new RecipeChoice.MaterialChoice(Material.QUARTZ_BLOCK, Material.QUARTZ_PILLAR,
				Material.SMOOTH_QUARTZ, Material.CHISELED_QUARTZ_BLOCK));
		machines.getServer().addRecipe(recipe);
	}

	@Override
	public double getCost() {
		return 1000;
	}

	private Location getHoloLocation(ConfigurationSection storage) {
		return getKey(storage).add(Shape.getRelativeVector(getDirection(storage), new Vector(0.5, 1.1, 1.5)));
	}

	private void setFuel(ConfigurationSection storage, long fuel) {
		ArmorStand hologram = HologramUtil.getOrCreateHologram(getHoloLocation(storage));
		if (hologram != null) {
			hologram.setCustomName(String.valueOf(fuel));
		}
		storage.set("fuel", fuel);
	}

	private long getFuel(ConfigurationSection storage) {
		return storage.getLong("fuel", 0);
	}

	@Override
	public boolean failedAssembly(@NotNull ConfigurationSection storage) {
		setFuel(storage, getFuel(storage));
		return super.failedAssembly(storage);
	}

	@Override
	public void handleHopperPickupItem(@NotNull InventoryPickupItemEvent event, @NotNull ConfigurationSection storage) {
		ItemStack inserted = event.getItem().getItemStack();
		Location key = getKey(storage);
		if (hasValue(inserted.getType())) {
			setFuel(storage, getFuel(storage) + getValue(inserted.getType()) * inserted.getAmount());
			if (key.getWorld() != null) {
				key.getWorld().playSound(key, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
			}
			event.getItem().remove();
		} else {
			event.getItem().teleport(
					key.add(Shape.getRelativeVector(
							getDirection(storage).getRelativeDirection(Direction.NORTH),
							new Vector(0.5, 0.5, -1.5))));
		}
		event.setCancelled(true);
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

	@Override
	public void handleInteract(@NotNull PlayerInteractEvent event, @NotNull ConfigurationSection storage) {
		super.handleInteract(event, storage);
		if (event.useInteractedBlock() == Event.Result.DENY) {
			return;
		}

		// Hopper inventory has to suck up items from the world, it should not be openable.
		if (event.getClickedBlock() == null || event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& event.getClickedBlock().getType() == Material.HOPPER) {
			event.setUseInteractedBlock(Event.Result.DENY);
			return;
		}

		if (event.getClickedBlock().getType() != Material.STONE_BUTTON) {
			return;
		}

		Location keyLocation = getKey(storage);
		Block keyBlock = keyLocation.getBlock();

		// Check for a sign in the proper location
		BlockState signState = keyBlock.getRelative(0, 2, 0).getState();
		if (!(signState instanceof Sign)) {
			event.getPlayer().sendMessage(ChatColor.RED
					+ "Please place a sign on your transportalizer between the buttons to use it."
					+ "\nThe third row should contain your desired coordinates in x, y, z format."
					+ "\nAll the other rows can contain whatever you like.");
			return;
		}

		Sign sign = (Sign) signState;
		// Check sign for proper format - sign lines are 0-3, third line is line 2
		String line3 = sign.getLine(2);
		if (!line3.matches("-?[0-9]+(\\s|,\\s?)[0-9]+(\\s|,\\s?)-?[0-9]+")) {
			event.getPlayer().sendMessage(ChatColor.RED
					+ "The third line of your transportalizer sign must contain "
					+ "your desired destination in x, y, z format. Ex: 0, 64, 0");
			return;
		}

		// Parse remote location. Do not allow invalid height or coords.
		WorldBorder border = keyBlock.getWorld().getWorldBorder();
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
			event.getPlayer().sendMessage(ChatColor.RED + "Your destination has been corrected for safety. Please review it.");
			return;
		}
		Block remote = keyBlock.getWorld().getBlockAt(x, y, z);

		// 50 fuel per block of distance, rounded up.
		int cost = (int) (keyLocation.distance(remote.getLocation()) / 50 + 1);
		// CHECK FUEL
		long fuel = getFuel(storage);
		if (fuel < cost) {
			event.getPlayer().sendMessage(ChatColor.RED
					+ "The Transportalizer begins humming through standard teleport procedure,"
					+ " when all of a sudden it growls to a halt."
					+ "\nPerhaps it requires more fuel?");
			keyBlock.getWorld().playSound(keyLocation, Sound.ENTITY_WOLF_GROWL, 1F, 0F);
			return;
		}

		// TELEPORT
		Block pad = event.getClickedBlock().getRelative(BlockFace.DOWN);
		Block source;
		Block target;
		boolean push = pad.getType() == Material.LIME_CARPET;
		if (push) {
			source = pad;
			target = remote;
		} else {
			source = remote;
			target = pad;
		}
		String failureMessage = null;
		for (Entity entity : keyBlock.getWorld().getNearbyEntities(BoundingBox.of(source))) {
			failureMessage = getFailureMessage(event.getPlayer(), entity, push, source.getLocation(),
					target.getLocation(), storage, cost);
			if (failureMessage != null) {
				if (!push && entity instanceof Player) {
					// Special case: pulling Player requires acceptance of a request
					return;
				}
				continue;
			}
			setFuel(storage, fuel - cost);
			teleport(entity, source.getLocation(), target.getLocation());
			return;
		}

		if (failureMessage != null) {
			event.getPlayer().sendMessage(failureMessage);
		}
	}

	private @Nullable String getFailureMessage(Player player, Entity entity, boolean push, Location from, Location to,
			ConfigurationSection storage, int cost) {
		if (push && entity instanceof Player) {
			// Sender must have button access to send players
			if (!ProtectionUtil.canUseButtonsAt(player, to)) {
				return ChatColor.RED + "You do not have access to the location specified!";
			}
			return null;
		}

		if (!push && entity instanceof Player) {
			RegisteredServiceProvider<EasterlynCore> registration = getMachines().getServer().getServicesManager().getRegistration(EasterlynCore.class);
			if (registration == null) {
				return "Easterlyn core plugin not loaded! Cannot pull players.";
			}
			if (player.getUniqueId().equals(entity.getUniqueId())) {
				return null;
			}

			User target = registration.getProvider().getUserManager().getUser(entity.getUniqueId());
			User issuer = registration.getProvider().getUserManager().getUser(player.getUniqueId());
			if (target.setPendingRequest(new Request() {
				@Override
				public void accept() {
					if (getFuel(storage) < cost) {
						target.sendMessage("Transportalizer is too low on fuel! Ask " + issuer.getDisplayName() + " to add more!");
					}
					Player localTarget = target.getPlayer();
					if (localTarget == null) {
						return;
					}
					teleport(entity, from, to);
					setFuel(storage, getFuel(storage) - cost);
				}

				@Override
				public void decline() {
					target.sendMessage("Request declined!");
				}
			})) {
				target.sendMessage(issuer.getDisplayName() + " is requesting to transportalize you!\nUse /accept or /decline to manage the request.");
				return "";
			}
			return null;
		}

		// Ender dragon or ender dragon parts
		if (entity instanceof ComplexLivingEntity || entity instanceof ComplexEntityPart) {
			return ChatColor.RED + "Great effort, but you can't transportalize a dragon.";
		}

		if (entity instanceof ArmorStand) {
			// Armor stand manipulation requires the ability to build
			if (!ProtectionUtil.canBuildAt(player, from) || !ProtectionUtil.canBuildAt(player, to)) {
				return ChatColor.RED + "You do not have access to the location specified!";
			}
			return null;
		}

		if (entity instanceof Monster || entity instanceof Explosive || entity instanceof ExplosiveMinecart) {
			// Hostiles, TNT, wither projectiles, fireballs, etc. require build permissions
			if (!ProtectionUtil.canOpenChestsAt(player, from)
					|| !ProtectionUtil.canBuildAt(player, to)) {
				return ChatColor.RED + "You do not have access to the location specified!";
			}
			return null;
		}

		// Generic push/pull requires chest access in send location and full access in arrival location
		if (!ProtectionUtil.canOpenChestsAt(player, from)
				|| !ProtectionUtil.canMobsSpawn(to) && !ProtectionUtil.canBuildAt(player, to)) {
			return ChatColor.RED + "You do not have access to the location specified!";
		}
		return null;
	}

	@Override
	public void handleBreak(@NotNull BlockBreakEvent event, @NotNull ConfigurationSection storage) {
		Location key = getKey(storage);
		if (event.getPlayer().getGameMode() == GameMode.SURVIVAL && key.getWorld() != null) {
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
		super.handleBreak(event, storage);
	}

	@Override
	public void enable(@NotNull ConfigurationSection storage) {
		ArmorStand hologram = HologramUtil.getOrCreateHologram(getHoloLocation(storage));
		if (hologram != null) {
			hologram.setCustomName(String.valueOf(getFuel(storage)));
		}
	}

	@Override
	public void disable(@NotNull ConfigurationSection storage) {
		ArmorStand hologram = HologramUtil.getHologram(getHoloLocation(storage));
		if (hologram != null) {
			hologram.remove();
		}
	}

	@NotNull
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

}
