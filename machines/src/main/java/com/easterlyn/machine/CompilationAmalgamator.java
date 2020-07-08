package com.easterlyn.machine;

import com.easterlyn.EasterlynCaptchas;
import com.easterlyn.EasterlynMachines;
import com.easterlyn.util.Direction;
import com.easterlyn.util.GenericUtil;
import com.easterlyn.util.Shape;
import com.easterlyn.util.inventory.InventoryUtil;
import com.easterlyn.util.inventory.ItemUtil;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Automation at its finest!
 *
 * @author Jikoo
 */
public class CompilationAmalgamator extends Machine {

	private final ItemStack drop;

	public CompilationAmalgamator(EasterlynMachines machines) {
		super(machines, new Shape(), "Compilation Amalgamator");
		Shape shape = getShape();
		shape.setVectorData(new Vector(0, 0, 0),
				new Shape.MaterialDataValue(Material.DROPPER).withBlockData(Directional.class, Direction.SOUTH));
		shape.setVectorData(new Vector(0, 0, 1),
				new Shape.MaterialDataValue(Material.HOPPER).withBlockData(Directional.class, Direction.SOUTH));

		drop = new ItemStack(Material.DROPPER);
		GenericUtil.consumeAs(ItemMeta.class, drop.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.WHITE + "Compilation Amalgamator");
			drop.setItemMeta(itemMeta);
		});

		ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(machines, "compilation_amalgamator"), drop);
		recipe.shape(" A ", "XYX", " Z ");
		recipe.setIngredient('A', Material.HOPPER);
		recipe.setIngredient('X', Material.ENDER_EYE);
		recipe.setIngredient('Y', Material.COMPARATOR);
		recipe.setIngredient('Z', Material.DROPPER);
		machines.getServer().addRecipe(recipe);
	}

	@Override
	public double getCost() {
		return 777;
	}

	@Override
	public void handleOpen(@NotNull InventoryOpenEvent event, ConfigurationSection storage) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!(event.getPlayer() instanceof Player)) {
					return;
				}
				Player player = (Player) event.getPlayer();
				if (!player.isOnline()) {
					return;
				}
				String name;
				if (event.getInventory().getType() == InventoryType.HOPPER) {
					name = "Blank Captcha Input";
				} else {
					name = getName();
				}
				InventoryUtil.changeWindowName(player, name);
			}
		}.runTask(getMachines());
	}

	@Override
	public void handleClick(@NotNull InventoryClickEvent event, ConfigurationSection storage) {
		Inventory top = event.getView().getTopInventory();
		if (top.getType() == InventoryType.HOPPER) {
			this.cleanHopperLater(top, storage);
		} else {
			this.updateLater(top, storage);
		}
	}

	@Override
	public void handleDrag(@NotNull InventoryDragEvent event, ConfigurationSection storage) {
		Inventory top = event.getView().getTopInventory();
		if (top.getType() == InventoryType.HOPPER) {
			this.cleanHopperLater(top, storage);
		} else {
			this.updateLater(top, storage);
		}
	}

	@Override
	public void handleHopperPickupItem(@NotNull InventoryPickupItemEvent event, @NotNull ConfigurationSection storage) {
		if (!EasterlynCaptchas.isBlankCaptcha(event.getItem().getItemStack())) {
			event.getItem().remove();
			this.ejectItem(this.getKey(storage), event.getItem().getItemStack(), storage);
			event.setCancelled(true);
		}
	}

	@Override
	public void handleHopperMoveItem(@NotNull InventoryMoveItemEvent event, @NotNull ConfigurationSection storage) {
		if (event.getDestination().getType() == InventoryType.HOPPER) {
			// Hopper only accepts blank captchas
			if (!EasterlynCaptchas.isBlankCaptcha(event.getItem())) {
				this.cleanHopperLater(event.getDestination(), storage);
			}
			return;
		}
		if (event.getItem().getType().getMaxDurability() > 0 || event.getItem().hasItemMeta()) {
			// Cancel if item is degradable or has meta, no automatic handling.
			event.setCancelled(true);
			return;
		}
		this.updateLater(event.getDestination(), storage);
	}

	private void updateLater(final Inventory inventory, ConfigurationSection storage) {
		new BukkitRunnable() {
			@Override
			public void run() {
				update(inventory, null, storage);
			}
		}.runTask(getMachines());
	}

	private void update(Inventory inventory, Inventory captchaInv, ConfigurationSection storage) {
		ItemStack captchaTarget = null;
		for (ItemStack item : inventory.getContents()) {
			//noinspection ConstantConditions
			if (item == null || item.getType() == Material.AIR) {
				continue;
			}
			if (item.getAmount() != item.getMaxStackSize()) {
				continue;
			}
			if (item.getMaxStackSize() > 1 && item.getType().getMaxDurability() == 0 && !item.hasItemMeta()) {
				captchaTarget = item.clone();
				break;
			}
			// This is safe, no CME because we're iterating over a copied array
			inventory.removeItem(item);
			if (inventory.getLocation() != null) {
				this.ejectItem(inventory.getLocation(), item, storage);
			}
		}

		if (captchaTarget == null) {
			return;
		}

		if (inventory.getLocation() == null) {
			return;
		}

		if (captchaInv == null) {
			Location captchaStorage = inventory.getLocation().add(Shape.getRelativeVector(
					this.getDirection(storage).getRelativeDirection(Direction.NORTH),
					new Vector(0, 0, 1)));
			BlockState blockState = captchaStorage.getBlock().getState();

			if (!(blockState instanceof InventoryHolder)) {
				return;
			}

			captchaInv = ((InventoryHolder) blockState).getInventory();
		}
		boolean usedCaptcha = false;
		for (int i = 0; i < captchaInv.getSize(); i++) {
			ItemStack captcha = captchaInv.getItem(i);
			if (EasterlynCaptchas.isBlankCaptcha(captcha)) {
				captchaInv.setItem(i, ItemUtil.decrement(captcha, 1));
				usedCaptcha = true;
				break;
			}
		}

		if (!usedCaptcha) {
			return;
		}

		RegisteredServiceProvider<EasterlynCaptchas> registration = getMachines().getServer().getServicesManager().getRegistration(EasterlynCaptchas.class);
		if (registration == null) {
			return;
		}

		ItemStack newCaptcha = registration.getProvider().getCaptchaForItem(captchaTarget);
		inventory.removeItem(captchaTarget);

		this.ejectItem(inventory.getLocation(), newCaptcha, storage);
	}

	private void cleanHopperLater(Inventory inventory, ConfigurationSection storage) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ejectAllInvalidItems(EasterlynCaptchas::isBlankCaptcha, inventory, storage);
				BlockState state = getKey(storage).getBlock().getState();
				if (!(state instanceof InventoryHolder)) {
					return;
				}
				update(((InventoryHolder) state).getInventory(), inventory, storage);
			}
		}.runTask(getMachines());
	}

	private void ejectAllInvalidItems(Function<ItemStack, Boolean> func, Inventory inventory, ConfigurationSection storage) {
		Location key = getKey(storage);
		for (ItemStack item : inventory.getContents()) {
			//noinspection ConstantConditions
			if (item == null || item.getType() == Material.AIR || func.apply(item)) {
				continue;
			}
			// This is safe, no CME because we're iterating over a copied array
			inventory.removeItem(item);
			this.ejectItem(key, item, storage);
		}
	}

	private void ejectItem(Location key, ItemStack item, ConfigurationSection storage) {
		Direction facing = this.getDirection(storage).getRelativeDirection(Direction.SOUTH);

		BlockState blockState = key.clone()
				.add(Shape.getRelativeVector(facing, new Vector(0, 0, 1))).getBlock().getState();
		if (blockState instanceof InventoryHolder) {
			// MACHINES InventoryMoveItemEvent
			if (((InventoryHolder) blockState).getInventory().addItem(item).size() == 0) {
				return;
			}
		}

		// Center block location
		key.add(Shape.getRelativeVector(facing, new Vector(0.5D, 0.5D, 1.5D)));
		BlockFace face = facing.toBlockFace();

		// See net.minecraft.server.DispenseBehaviorItem
		Random random = ThreadLocalRandom.current();
		double motionRandom = random.nextDouble() * 0.1D + 0.2D;
		// 0.007499999832361937D * 6
		double motX = face.getModX() * motionRandom + random.nextGaussian() * 0.044999998994171622D;
		double motY = 0.2000000029802322D + random.nextGaussian() * 0.044999998994171622D;
		double motZ = face.getModZ() * motionRandom + random.nextGaussian() * 0.044999998994171622D;

		// MACHINES BlockDispenseEvent
		if (key.getWorld() != null) {
			key.getWorld().dropItem(key, item).setVelocity(new Vector(motX, motY, motZ));
			key.getWorld().playSound(key, Sound.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1F, 1F);
			key.getWorld().playEffect(key, Effect.SMOKE, face);
		}
	}

	@NotNull
	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}

}
