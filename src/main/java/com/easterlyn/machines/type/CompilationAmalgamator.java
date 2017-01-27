package com.easterlyn.machines.type;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.utilities.Direction;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.machines.utilities.Shape.MaterialDataValue;
import com.easterlyn.utilities.InventoryUtils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

/**
 * Automation at its finest!
 * 
 * @author Jikoo
 */
public class CompilationAmalgamator extends Machine {

	private final Captcha captcha;
	private final ItemStack drop;

	public CompilationAmalgamator(Easterlyn plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Compilation Amalgamator");
		this.captcha = plugin.getModule(Captcha.class);
		Shape shape = getShape();
		MaterialDataValue m = shape.new MaterialDataValue(Material.DROPPER, Direction.NORTH, "chest");
		shape.setVectorData(new Vector(0, 0, 0), m);
		m = shape.new MaterialDataValue(Material.HOPPER, Direction.NORTH, "chest");
		shape.setVectorData(new Vector(0, 0, 1), m);

		drop = new ItemStack(Material.DROPPER);
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Compilation Amalgamator");
		drop.setItemMeta(meta);
	}

	@Override
	public int getCost() {
		return 777;
	}

	@Override
	public boolean handleOpen(InventoryOpenEvent event, ConfigurationSection storage) {
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
				InventoryUtils.changeWindowName(player, name);
			}
		}.runTask(getPlugin());
		return false;
	}

	@Override
	public boolean handleClick(InventoryClickEvent event, ConfigurationSection storage) {
		Inventory top = event.getView().getTopInventory();
		if (top.getType() == InventoryType.HOPPER) {
			this.cleanHopperLater(top, storage);
		} else {
			this.updateLater(top, storage);
		}

		return false;
	}

	@Override
	public boolean handleClick(InventoryDragEvent event, ConfigurationSection storage) {
		Inventory top = event.getView().getTopInventory();
		if (top.getType() == InventoryType.HOPPER) {
			this.cleanHopperLater(top, storage);
		} else {
			this.updateLater(top, storage);
		}

		return false;
	}

	@Override
	public boolean handleHopperPickupItem(InventoryPickupItemEvent event,
			ConfigurationSection storage) {
		if (!Captcha.isBlankCaptcha(event.getItem().getItemStack())) {
			event.getItem().remove();
			this.ejectItem(this.getKey(storage), event.getItem().getItemStack(), storage);
			return true;
		}
		return false;
	}

	@Override
	public boolean handleHopperMoveItem(InventoryMoveItemEvent event, ConfigurationSection storage) {
		if (event.getDestination().getType() == InventoryType.HOPPER) {
			// Hopper only accepts blank captchas
			if (!Captcha.isBlankCaptcha(event.getItem())) {
				this.cleanHopperLater(event.getDestination(), storage);
			}
			return false;
		}
		if (event.getItem().getType().getMaxDurability() > 0 || event.getItem().hasItemMeta()) {
			// Cancel if item is degradable or has meta, no automatic handling.
			return true;
		}
		this.updateLater(event.getDestination(), storage);
		return false;
	}

	private void updateLater(final Inventory inventory, ConfigurationSection storage) {
		new BukkitRunnable() {
			@Override
			public void run() {
				update(inventory, null, storage);
			}
		}.runTask(getPlugin());
	}

	private void update(Inventory inventory, Inventory captchaInv, ConfigurationSection storage) {
		ItemStack captchaTarget = null;
		for (ItemStack item : inventory.getContents()) {
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
			this.ejectItem(inventory.getLocation(), item, storage);
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
			if (Captcha.isBlankCaptcha(captcha)) {
				captchaInv.setItem(i, InventoryUtils.decrement(captcha, 1));
				usedCaptcha = true;
				break;
			}
		}

		if (!usedCaptcha) {
			return;
		}

		ItemStack newCaptcha = this.captcha.itemToCaptcha(captchaTarget);
		inventory.removeItem(captchaTarget);

		this.ejectItem(inventory.getLocation(), newCaptcha, storage);
	}

	private void cleanHopperLater(Inventory inventory, ConfigurationSection storage) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ejectAllInvalidItems(new Function<ItemStack, Boolean>() {
					@Override
					public Boolean apply(ItemStack item) {
						return Captcha.isBlankCaptcha(item);
					}
				}, inventory, storage);
				BlockState state = getKey(storage).getBlock().getState();
				if (!(state instanceof InventoryHolder)) {
					return;
				}
				update(((InventoryHolder) state).getInventory(), inventory, storage);
			}
		}.runTask(getPlugin());
	}

	private void ejectAllInvalidItems(Function<ItemStack, Boolean> func, Inventory inventory, ConfigurationSection storage) {
		for (ItemStack item : inventory.getContents()) {
			if (item == null || item.getType() == Material.AIR || func.apply(item)) {
				continue;
			}
			// This is safe, no CME because we're iterating over a copied array
			inventory.removeItem(item);
			this.ejectItem(this.getKey(storage), item, storage);
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
		// TODO play click + smoke
		key.getWorld().dropItem(key, item).setVelocity(new Vector(motX, motY, motZ));
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}

}
