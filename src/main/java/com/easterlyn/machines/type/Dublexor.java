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
import com.easterlyn.utilities.Experience;
import com.easterlyn.utilities.InventoryUtils;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Machine for item duplication.
 *
 * @author Jikoo
 */
public class Dublexor extends Machine {

	private static Triple<ItemStack, ItemStack, ItemStack> exampleRecipes;

	private final Captcha captcha;
	private final Effects effects;
	private final MachineInventoryTracker tracker;
	private final ItemStack drop, barrier;

	public Dublexor(Easterlyn plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Dublexor");
		this.captcha = plugin.getModule(Captcha.class);
		this.effects = plugin.getModule(Effects.class);
		this.tracker = machines.getInventoryTracker();

		Shape shape = getShape();

		shape.setVectorData(new Vector(0, 0, 0), Material.GLASS);

		shape.setVectorData(new Vector(0, 1, 0), Material.ENCHANTING_TABLE);
		shape.setVectorData(new Vector(0, 0, -1),
				shape.new MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.NORTH));
		shape.setVectorData(new Vector(1, 0, 0),
				shape.new MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.WEST));
		shape.setVectorData(new Vector(-1, 0, 0),
				shape.new MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.EAST));
		shape.setVectorData(new Vector(0, 0, 1),
				shape.new MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.SOUTH));

		Shape.MaterialDataValue m = shape.new MaterialDataValue(Material.QUARTZ_SLAB);
		shape.setVectorData(new Vector(1, 0, -1), m);
		shape.setVectorData(new Vector(-1, 0, -1), m);
		shape.setVectorData(new Vector(1, 0, 1), m);
		shape.setVectorData(new Vector(-1, 0, 1), m);

		this.drop = new ItemStack(Material.ENCHANTING_TABLE);
		ItemMeta meta = this.drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Dublexor");
		this.drop.setItemMeta(meta);

		this.barrier = new ItemStack(Material.BARRIER);
		meta = this.barrier.getItemMeta();
		meta.setDisplayName(Language.getColor("emphasis.bad") + "No Result");
		this.barrier.setItemMeta(meta);
	}

	@Override
	public ItemStack getUniqueDrop() {
		return this.drop;
	}

	@Override
	public int getCost() {
		return 1500;
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
		updateInventory(event.getPlayer().getUniqueId());
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

			// Color code + "Mana cost: " = 13 characters, as is color code + "Cannot copy"
			String costString = top.getItem(1).getItemMeta().getDisplayName().substring(13);
			if (costString.isEmpty()) {
				return true;
			}

			// Remove exp first in case of an unforeseen issue.
			int expCost;
			try {
				expCost = Integer.valueOf(costString);
			} catch (NumberFormatException e) {
				System.err.println("Unable to parse ");
				e.printStackTrace();
				return true;
			}
			Player player = (Player) event.getWhoClicked();
			if (player.getGameMode() != GameMode.CREATIVE) {
				Experience.changeExp(player, -expCost);
			}


			if (event.getClick().name().contains("SHIFT")) {
				// Ensure inventory can contain items
				if (InventoryUtils.hasSpaceFor(event.getCurrentItem(), player.getInventory())) {
					player.getInventory().addItem(event.getCurrentItem().clone());
				} else {
					return true;
				}
			} else if (event.getCursor() == null || event.getCursor().getType() == Material.AIR
					|| (event.getCursor().isSimilar(event.getCurrentItem())
						&& event.getCursor().getAmount() + event.getCurrentItem().getAmount()
						< event.getCursor().getMaxStackSize())) {
				// Cursor can contain items
				ItemStack result = event.getCurrentItem().clone();
				if (result.isSimilar(event.getCursor())) {
					result.setAmount(result.getAmount() + event.getCursor().getAmount());
				}
				event.setCursor(result);
			} else {
				// Cursor cannot contain items
				return true;
			}
			event.setCurrentItem(null);
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
	 * @param id the UUID of the Player using the Dublexor
	 */
	public void updateInventory(final UUID id) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
			// Must re-obtain player or update doesn't seem to happen
			Player player = Bukkit.getPlayer(id);
			if (player == null || !tracker.hasMachineOpen(player)) {
				// Player has logged out or closed inventory. Inventories are per-player, ignore.
				return;
			}

			Inventory open = player.getOpenInventory().getTopInventory();
			ItemStack originalInput = open.getItem(0);

			if (originalInput == null || originalInput.getType() == Material.AIR) {
				setSecondTrade(player, open, null, null, null);
				return;
			}

			ItemStack expCost = new ItemStack(Material.EXPERIENCE_BOTTLE);
			ItemMeta im = expCost.getItemMeta();
			im.setDisplayName(Language.getColor("emphasis.bad") + "Cannot copy");
			expCost.setItemMeta(im);

			ItemStack modifiedInput = originalInput.clone();
			int multiplier = 1;
			while (Captcha.isUsedCaptcha(modifiedInput)) {
				ItemStack newModInput = captcha.getItemForCaptcha(modifiedInput);
				if (newModInput == null || modifiedInput.isSimilar(newModInput)) {
					// Broken captcha, don't infinitely loop.
					setSecondTrade(player, open, originalInput, expCost, barrier);
					return;
				}
				multiplier *= Math.max(1, Math.abs(modifiedInput.getAmount()));
				modifiedInput = newModInput;
			}

			// Ensure non-unique item (excluding captchas)
			if (InventoryUtils.isUniqueItem(getPlugin(), modifiedInput)) {
				setSecondTrade(player, open, originalInput, expCost, barrier);
				return;
			}

			// Calculate cost based on final item.
			double resultCost = ManaMappings.expCost(effects, modifiedInput);

			// Ensure item can be replicated.
			if (Double.MAX_VALUE / multiplier <= resultCost) {
				setSecondTrade(player, open, originalInput, expCost, barrier);
				return;
			}

			// Adjust cost based on captcha depth and quantities.
			resultCost *= multiplier;
			int exp = (int) Math.ceil(resultCost);
			int playerExp = Experience.getExp(player);
			int remainder = playerExp - exp;

			ArrayList<String> lore = new ArrayList<>();
			lore.add(ChatColor.GOLD + "Current: " + playerExp);

			ItemStack result;
			ChatColor color;
			if (remainder >= 0 || player.getGameMode() == GameMode.CREATIVE) {
				color = Language.getColor("emphasis.good");
				lore.add(ChatColor.GOLD + "Remainder: " + remainder);
				result = originalInput.clone();
			} else {
				color = Language.getColor("emphasis.bad");
				lore.add(color.toString() + ChatColor.BOLD + "Not enough mana!");
				result = barrier;
			}

			if (player.getGameMode() == GameMode.CREATIVE) {
				lore.add(Language.getColor("emphasis.good") + "Creative exp bypass engaged.");
			}

			im.setDisplayName(color + "Mana cost: " + exp);
			im.setLore(lore);
			expCost.setItemMeta(im);

			// Set items
			setSecondTrade(player, open, originalInput, expCost, result);
		});
	}

	private void setSecondTrade(Player player, Inventory open, ItemStack input, ItemStack expCost, ItemStack result) {
		open.setItem(1, expCost);
		open.setItem(2, result);
		InventoryUtils.updateVillagerTrades(player, getExampleRecipes(),
				new ImmutableTriple<>(input, expCost, result));
		player.updateInventory();
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
	 * @return a Triple containing inputs and a result defining behavior
	 */
	private static Triple<ItemStack, ItemStack, ItemStack> createExampleRecipes() {
		ItemStack is1 = new ItemStack(Material.DIRT, 64);
		ItemMeta im = is1.getItemMeta();
		im.setDisplayName(ChatColor.GOLD + "Input");
		List<String> lore = Collections.singletonList(ChatColor.WHITE + "Insert item here.");
		im.setLore(lore);
		is1.setItemMeta(im);

		ItemStack is2 = new ItemStack(Material.EXPERIENCE_BOTTLE);
		im = is2.getItemMeta();
		im.setDisplayName(ChatColor.GOLD + "Mana Cost");
		lore = Arrays.asList(ChatColor.WHITE + "Displays dublecation cost",
				ChatColor.WHITE + "when an item is inserted.");
		im.setLore(lore);
		is2.setItemMeta(im);

		ItemStack is3 = new ItemStack(Material.DIRT, 64);
		im = is3.getItemMeta();
		im.setDisplayName(ChatColor.GOLD + "Copy of Input");
		lore = Collections.singletonList(ChatColor.WHITE + "Dublecate your items.");
		im.setLore(lore);
		is3.setItemMeta(im);

		return new ImmutableTriple<>(is1, is2, is3);
	}

}
