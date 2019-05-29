package com.easterlyn.machines.type;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.captcha.ManaMappings;
import com.easterlyn.chat.Language;
import com.easterlyn.effects.Effects;
import com.easterlyn.machines.MachineInventoryTracker;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.utilities.Direction;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.machines.utilities.Shape.MaterialDataValue;
import com.easterlyn.utilities.Experience;
import com.easterlyn.utilities.InventoryUtils;
import com.easterlyn.utilities.tuple.Triple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

/**
 * Simulate a Sburb Alchemiter in Minecraft.
 *
 * @author Jikoo
 */
public class Alchemiter extends Machine {

	private static Triple<ItemStack, ItemStack, ItemStack> exampleRecipes;

	private final Captcha captcha;
	private final Effects effects;
	private final MachineInventoryTracker tracker;
	private final ItemStack drop, barrier;

	public Alchemiter(Easterlyn plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Alchemiter");
		this.captcha = plugin.getModule(Captcha.class);
		this.effects = plugin.getModule(Effects.class);
		this.tracker = machines.getInventoryTracker();
		Shape shape = getShape();
		MaterialDataValue m = new Shape.MaterialDataValue(Material.CHISELED_QUARTZ_BLOCK);
		shape.setVectorData(new Vector(0, 0, 0), m);
		shape.setVectorData(new Vector(0, 0, 1), m);
		shape.setVectorData(new Vector(1, 0, 1), m);
		shape.setVectorData(new Vector(1, 0, 0), m);
		m = new Shape.MaterialDataValue(Material.QUARTZ_PILLAR);
		shape.setVectorData(new Vector(0, 0, 2), m);
		m = new Shape.MaterialDataValue(Material.NETHER_BRICK_FENCE);
		shape.setVectorData(new Vector(0, 1, 2), m);
		shape.setVectorData(new Vector(0, 2, 2), m);
		shape.setVectorData(new Vector(0, 3, 2), m);
		shape.setVectorData(new Vector(0, 3, 1), m);
		m = new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.NORTH);
		shape.setVectorData(new Vector(-1, 0, -1), m);
		shape.setVectorData(new Vector(0, 0, -1), m);
		shape.setVectorData(new Vector(1, 0, -1), m);
		shape.setVectorData(new Vector(2, 0, -1), m);
		m = new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.SOUTH);
		shape.setVectorData(new Vector(-1, 0, 2), m);
		shape.setVectorData(new Vector(1, 0, 2), m);
		shape.setVectorData(new Vector(2, 0, 2), m);
		m = new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.EAST);
		shape.setVectorData(new Vector(-1, 0, 1), m);
		shape.setVectorData(new Vector(-1, 0, 0), m);
		m = new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.WEST);
		shape.setVectorData(new Vector(2, 0, 1), m);
		shape.setVectorData(new Vector(2, 0, 0), m);

		drop = new ItemStack(Material.QUARTZ_PILLAR, 1);
		InventoryUtils.consumeAs(ItemMeta.class, drop.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.WHITE + "Alchemiter");
			drop.setItemMeta(itemMeta);
		});

		barrier = new ItemStack(Material.BARRIER);
		InventoryUtils.consumeAs(ItemMeta.class, barrier.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(Language.getColor("emphasis.bad") + "No Result");
			barrier.setItemMeta(itemMeta);
		});
	}

	@Override
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		if (super.handleInteract(event, storage)) {
			return true;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return true;
		}
		if (event.getPlayer().isSneaking()) {
			return false;
		}
		tracker.openVillagerInventory(event.getPlayer(), this, getKey(storage));
		InventoryUtils.updateVillagerTrades(event.getPlayer(), getExampleRecipes());
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean handleClick(InventoryClickEvent event, ConfigurationSection storage) {
		updateInventory(event.getWhoClicked().getUniqueId());
		if (event.getRawSlot() != event.getView().convertSlot(event.getRawSlot())) {
			// Clicked inv is not the top.
			return false;
		}
		if (event.getSlot() == 1) {
			// Exp slot is being clicked. No adding or removing items.
			return true;
		}
		if (event.getSlot() == 2 && event.getCurrentItem() != null
				&& event.getCurrentItem().getType() != Material.AIR) {
			if (event.getCurrentItem().getType() == Material.BARRIER) {
				return true;
			}
			// Item is being crafted
			Inventory top = event.getView().getTopInventory();
			Player player = (Player) event.getWhoClicked();
			if (event.getClick().name().contains("SHIFT")) {
				if (InventoryUtils.hasSpaceFor(event.getCurrentItem(), player.getInventory())) {
					player.getInventory().addItem(event.getCurrentItem().clone());
				} else {
					return true;
				}
			} else if (event.getCursor() == null || event.getCursor().getType() == Material.AIR
					|| (event.getCursor().isSimilar(event.getCurrentItem())
					&& event.getCursor().getAmount() + event.getCurrentItem().getAmount()
					< event.getCursor().getMaxStackSize())) {
				ItemStack result = event.getCurrentItem().clone();
				if (result.isSimilar(event.getCursor())) {
					result.setAmount(result.getAmount() + event.getCursor().getAmount());
				}
				event.setCursor(result);
			} else {
				return true;
			}
			event.setCurrentItem(null);
			top.setItem(0, InventoryUtils.decrement(top.getItem(0), 1));
			// Color code + "Grist cost: " = 14 chars
			//noinspection ConstantConditions // This is guaranteed to be okay.
			int expCost = Integer.valueOf(top.getItem(1).getItemMeta().getDisplayName().substring(14));
			Experience.changeExp(player, -expCost);
		}
		return false;
	}

	@Override
	public boolean handleClick(InventoryDragEvent event, ConfigurationSection storage) {
		updateInventory(event.getWhoClicked().getUniqueId());
		// Raw slot 1 = second slot of top inventory
		return event.getRawSlots().contains(1);
	}

	/**
	 * Calculate result slot and update inventory on a delay (post-event completion)
	 *
	 * @param id the UUID of the player who is using the Punch Designix
	 */
	private void updateInventory(final UUID id) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
			// Must re-obtain player or update doesn't seem to happen
			Player player = Bukkit.getPlayer(id);
			if (player == null || tracker.hasNoMachineOpen(player)) {
				// Player has logged out or closed inventory. Inventories are per-player, ignore.
				return;
			}

			Inventory open = player.getOpenInventory().getTopInventory();
			ItemStack input = open.getItem(0);
			if (input == null) {
				input = new ItemStack(Material.AIR);
			}
			ItemStack expCost;
			ItemStack result;
			if (Captcha.isDowel(input)) {
				input = input.clone();
				input.setAmount(1);
				result = captcha.getItemForCaptcha(input);
				expCost = new ItemStack(Material.EXPERIENCE_BOTTLE);
				int exp = (int) Math.ceil(ManaMappings.expCost(effects, result));
				int playerExp = Experience.getExp(player);
				int remainder = playerExp - exp;
				ChatColor color = remainder >= 0 ? ChatColor.GREEN : ChatColor.DARK_RED;
				ArrayList<String> lore = new ArrayList<>();
				lore.add(ChatColor.GOLD + "Current: " + playerExp);
				if (remainder >= 0) {
					lore.add(ChatColor.GOLD + "Remainder: " + remainder);
				} else {
					lore.add(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Not enough grist!");
					result = barrier;
				}
				InventoryUtils.consumeAs(ItemMeta.class, expCost.getItemMeta(), itemMeta -> {
					itemMeta.setDisplayName(color + "Grist cost: " + exp);
					itemMeta.setLore(lore);
					
					expCost.setItemMeta(itemMeta);
				});
			} else {
				result = barrier;
				expCost = new ItemStack(Material.AIR);
			}
			// Set items
			open.setItem(1, expCost);
			open.setItem(2, result);
			InventoryUtils.updateVillagerTrades(player, getExampleRecipes(), new Triple<>(input, expCost, result));
		});
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}

	/**
	 * Singleton for getting usage help ItemStacks.
	 */
	private static Triple<ItemStack, ItemStack, ItemStack> getExampleRecipes() {
		if (exampleRecipes == null) {
			exampleRecipes = createExampleRecipes();
		}
		return exampleRecipes;
	}

	/**
	 * Creates the ItemStacks used in displaying usage help.
	 *
	 * @return the example recipe
	 */
	private static Triple<ItemStack, ItemStack, ItemStack> createExampleRecipes() {
		ItemStack input = new ItemStack(Material.NETHER_BRICK);
		InventoryUtils.consumeAs(ItemMeta.class, input.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.GOLD + "Cruxite Totem");
			input.setItemMeta(itemMeta);
		});

		ItemStack cost = new ItemStack(Material.BARRIER);
		InventoryUtils.consumeAs(ItemMeta.class, cost.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.GOLD + "Grist Cost");
			itemMeta.setLore(Arrays.asList(ChatColor.WHITE + "This will display when a",
					ChatColor.WHITE + "valid totem is inserted."));
			cost.setItemMeta(itemMeta);
		});

		ItemStack result = new ItemStack(Material.DIRT);
		InventoryUtils.consumeAs(ItemMeta.class, result.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.GOLD + "Perfectly Generic Result");
			itemMeta.setLore(Collections.singletonList(ChatColor.WHITE + "Your result here."));
			result.setItemMeta(itemMeta);
		});

		return new Triple<>(input, cost, result);
	}

}
