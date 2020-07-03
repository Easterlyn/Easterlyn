package com.easterlyn.machine;

import com.easterlyn.EasterlynCaptchas;
import com.easterlyn.EasterlynMachines;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.util.Direction;
import com.easterlyn.util.EconomyUtil;
import com.easterlyn.util.ExperienceUtil;
import com.easterlyn.util.GenericUtil;
import com.easterlyn.util.NumberUtil;
import com.easterlyn.util.Shape;
import com.easterlyn.util.inventory.InventoryUtil;
import com.easterlyn.util.inventory.ItemUtil;
import com.easterlyn.util.tuple.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Machine for item duplication.
 *
 * @author Jikoo
 */
public class Dublexor extends Machine {

	private static MerchantRecipe exampleRecipe;

	private final ItemStack drop, barrier;

	public Dublexor(EasterlynMachines machines) {
		super(machines, new Shape(), "Dublexor");

		Shape shape = getShape();

		shape.setVectorData(new Vector(0, 0, 0), Material.GLASS);

		shape.setVectorData(new Vector(0, 1, 0), Material.ENCHANTING_TABLE);
		shape.setVectorData(new Vector(0, 0, -1),
				new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.NORTH));
		shape.setVectorData(new Vector(1, 0, 0),
				new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.WEST));
		shape.setVectorData(new Vector(-1, 0, 0),
				new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.EAST));
		shape.setVectorData(new Vector(0, 0, 1),
				new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.SOUTH));

		Shape.MaterialDataValue m = new Shape.MaterialDataValue(Material.QUARTZ_SLAB);
		shape.setVectorData(new Vector(1, 0, -1), m);
		shape.setVectorData(new Vector(-1, 0, -1), m);
		shape.setVectorData(new Vector(1, 0, 1), m);
		shape.setVectorData(new Vector(-1, 0, 1), m);

		drop = new ItemStack(Material.ENCHANTING_TABLE);
		GenericUtil.consumeAs(ItemMeta.class, drop.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.WHITE + "Dublexor");
			drop.setItemMeta(itemMeta);
		});

		barrier = new ItemStack(Material.BARRIER);
		GenericUtil.consumeAs(ItemMeta.class, barrier.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.RED + "No Result");
			barrier.setItemMeta(itemMeta);
		});
	}

	@NotNull
	@Override
	public ItemStack getUniqueDrop() {
		return this.drop;
	}

	@Override
	public double getCost() {
		return 1500;
	}

	@Override
	public void handleInteract(@NotNull PlayerInteractEvent event, @NotNull ConfigurationSection storage) {
		super.handleInteract(event, storage);
		//noinspection deprecation
		if (event.isCancelled() || event.getPlayer().isSneaking()) {
			return;
		}
		Merchant merchant = getMachines().getMerchant(getName(), this, storage);
		merchant.setRecipes(Collections.singletonList(Dublexor.getExampleRecipe()));
		event.getPlayer().openMerchant(merchant, true);
		event.setCancelled(true);
	}

	@Override
	public void handleClick(@NotNull InventoryClickEvent event, ConfigurationSection storage) {
		if (event.getRawSlot() == 0) {
			// Clicked slot is input. Update choices.
			updateInventory(event.getWhoClicked().getUniqueId(), false);
			return;
		}

		if (event.getRawSlot() == 1) {
			// Clicked slot is exp cost display. No modification allowed.
			event.setCancelled(true);
			return;
		}

		if (event.getRawSlot() != 2 || event.getCurrentItem() == null
				|| event.getCurrentItem().getType() == Material.AIR) {
			// Result slot is not being clicked or there is no result.
			return;
		}

		if (event.getCurrentItem().getType() == Material.BARRIER) {
			// Operation is not allowed.
			event.setCancelled(true);
			return;
		}

		Inventory top = event.getView().getTopInventory();

		// Color code + "Mana cost: " = 13 characters, as is color code + "Cannot copy"
		//noinspection ConstantConditions // This is guaranteed to be okay.
		String costString = top.getItem(1).getItemMeta().getDisplayName().substring(13);
		if (costString.isEmpty()) {
			event.setCancelled(true);
			return;
		}

		// Remove exp first in case of an unforeseen issue.
		int expCost;
		try {
			expCost = Integer.parseInt(costString);
		} catch (NumberFormatException e) {
			ReportableEvent.call("Unable to parse Dublecation cost:" + costString, e, 5);
			event.setCancelled(true);
			return;
		}

		Player player = (Player) event.getWhoClicked();
		if (player.getGameMode() != GameMode.CREATIVE) {
			ExperienceUtil.changeExp(player, -expCost);
		}

		if (event.getClick().name().contains("SHIFT")) {
			// Ensure inventory can contain items
			if (ItemUtil.hasSpaceFor(event.getCurrentItem(), player.getInventory())) {
				player.getInventory().addItem(event.getCurrentItem().clone());
			} else {
				event.setCancelled(true);
				if (player.getGameMode() != GameMode.CREATIVE) {
					ExperienceUtil.changeExp(player, expCost);
				}
				return;
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
			// noinspection deprecation // No alternative available, desync is handled.
			event.setCursor(result);
		} else {
			// Cursor cannot contain items
			if (player.getGameMode() != GameMode.CREATIVE) {
				ExperienceUtil.changeExp(player, expCost);
			}
			event.setCancelled(true);
			return;
		}

		event.setCurrentItem(null);

		updateInventory(player.getUniqueId(), false);
	}

	@Override
	public void handleDrag(@NotNull InventoryDragEvent event, ConfigurationSection storage) {
		if (event.getRawSlots().contains(1)) {
			// Contains cost display slot. No modification allowed.
			event.setCancelled(true);
			return;
		}
		if (event.getRawSlots().contains(0)) {
			// Contains input. Update choices.
			updateInventory(event.getWhoClicked().getUniqueId(), false);
		}
	}

	/**
	 * Calculate result slot and update inventory on a delay (post-event completion)
	 *
	 * @param id the UUID of the Player using the Dublexor
	 */
	private void updateInventory(final UUID id, boolean fullUpdate) {
		getMachines().getServer().getScheduler().scheduleSyncDelayedTask(getMachines(), () -> {
			// Must re-obtain player or update doesn't seem to happen
			Player player = Bukkit.getPlayer(id);
			if (player == null || !(player.getOpenInventory().getTopInventory() instanceof MerchantInventory)) {
				// Player has logged out or closed inventory. Inventories are per-player, ignore.
				return;
			}

			MerchantInventory open = (MerchantInventory) player.getOpenInventory().getTopInventory();
			Pair<Machine, ConfigurationSection> machineData = getMachines().getMerchantMachine(open.getMerchant());
			if (machineData == null || !Dublexor.this.equals(machineData.getLeft())) {
				return;
			}

			ItemStack originalInput = open.getItem(0);

			if (originalInput == null || originalInput.getType() == Material.AIR) {
				open.setItem(1, null);
				return;
			}

			ItemStack expCost = new ItemStack(Material.EXPERIENCE_BOTTLE);
			GenericUtil.consumeAs(ItemMeta.class, expCost.getItemMeta(), itemMeta -> {
				itemMeta.setDisplayName(ChatColor.RED + "Cannot copy");
				expCost.setItemMeta(itemMeta);
			});

			Pair<ItemStack, Integer> uncaptcha = unCaptcha(originalInput.clone());
			ItemStack modifiedInput = uncaptcha.getLeft();
			int multiplier = uncaptcha.getRight();

			// Ensure non-unique item (excluding captchas)
			if (ItemUtil.isUniqueItem(modifiedInput)) {
				displayTrade(player, open, originalInput, expCost, barrier, fullUpdate);
				return;
			}

			// Calculate cost based on final item.
			double resultCost = EconomyUtil.getWorth(modifiedInput);

			// Adjust cost based on captcha depth and quantities.
			try {
				resultCost = NumberUtil.multiplySafe(resultCost, multiplier);
			} catch (ArithmeticException e) {
				resultCost = Double.POSITIVE_INFINITY;
			}
			int exp = (int) Math.ceil(resultCost);
			int playerExp = ExperienceUtil.getExp(player);
			int remainder = resultCost == Double.POSITIVE_INFINITY ? Integer.MIN_VALUE : playerExp - exp;

			ArrayList<String> lore = new ArrayList<>();
			lore.add(ChatColor.GOLD + "Current: " + playerExp);

			ItemStack result;
			ChatColor color;
			if (remainder >= 0 || player.getGameMode() == GameMode.CREATIVE) {
				color = ChatColor.GREEN;
				lore.add(ChatColor.GOLD + "Remainder: " + remainder);
				result = originalInput.clone();
			} else {
				color = ChatColor.RED;
				lore.add(color.toString() + ChatColor.BOLD + "Not enough mana!");
				result = barrier;
			}

			if (player.getGameMode() == GameMode.CREATIVE) {
				lore.add(ChatColor.GOLD + "Creative exp bypass engaged.");
			}

			GenericUtil.consumeAs(ItemMeta.class, expCost.getItemMeta(), itemMeta -> {
				itemMeta.setDisplayName(color + "Mana cost: " + exp);
				itemMeta.setLore(lore);
				expCost.setItemMeta(itemMeta);
			});

			// Set items
			displayTrade(player, open, originalInput, expCost, result, fullUpdate);
		});
	}

	private Pair<ItemStack, Integer> unCaptcha(ItemStack potentialCaptcha) {
		try {
			Class.forName("com.easterlyn.EasterlynCaptchas");
		} catch (ClassNotFoundException e) {
			return new Pair<>(potentialCaptcha, 1);
		}

		RegisteredServiceProvider<EasterlynCaptchas> registration = getMachines().getServer().getServicesManager().getRegistration(EasterlynCaptchas.class);
		if (registration == null) {
			return new Pair<>(potentialCaptcha, 1);
		}

		int multiplier = 1;
		while (EasterlynCaptchas.isUsedCaptcha(potentialCaptcha)) {
			ItemStack newModInput = registration.getProvider().getItemByCaptcha(potentialCaptcha);
			if (newModInput == null || potentialCaptcha.isSimilar(newModInput)) {
				// Broken captcha, don't infinitely loop.
				potentialCaptcha = barrier;
				break;
			}
			multiplier = Math.multiplyExact(multiplier, Math.max(1, Math.abs(potentialCaptcha.getAmount())));
			potentialCaptcha = newModInput;
		}

		return new Pair<>(potentialCaptcha, multiplier);
	}

	/**
	 * Adds a given trade offer to a Merchant.
	 *
	 * @param player the trading Player
	 * @param open the open MerchantInventory
	 * @param input the first trade input
	 * @param expCost the second trade input
	 * @param result the resulting ItemStack
	 * @param fullUpdate whether or not a full inventory update is required to prevent client desync
	 */
	private void displayTrade(@NotNull Player player, @NotNull MerchantInventory open, @NotNull ItemStack input,
			@NotNull ItemStack expCost, @NotNull ItemStack result, boolean fullUpdate) {
		List<MerchantRecipe> recipes = new ArrayList<>(open.getMerchant().getRecipes());

		// Check if selected recipe is correct for input
		if (open.getSelectedRecipeIndex() > 0 && input.equals(recipes.get(open.getSelectedRecipeIndex()).getIngredients().get(0))) {
			setSlots(player, open, expCost, result, fullUpdate);
			return;
		}

		// Check if a correct recipe exists
		for (int i = 0; i < recipes.size(); ++i) {
			MerchantRecipe recipe = recipes.get(i);
			if (input.equals(recipe.getIngredients().get(0))) {
				// If a recipe is selected, ensure that the correct recipe replaces it
				if (open.getSelectedRecipeIndex() > 0 && open.getSelectedRecipeIndex() != i) {
					recipes.set(i, recipes.get(open.getSelectedRecipeIndex()));
					recipes.set(open.getSelectedRecipeIndex(), recipe);
					open.getMerchant().setRecipes(recipes);
					InventoryUtil.updateVillagerTrades(player, recipes);
				}
				setSlots(player, open, expCost, result, fullUpdate);
				return;
			}
		}

		MerchantRecipe recipe = new MerchantRecipe(result, Integer.MAX_VALUE);
		recipe.addIngredient(input);
		recipe.addIngredient(expCost);

		// Delete the first non-example recipe if list is too large
		if (recipes.size() >= 5) {
			recipes.remove(1);
		}

		// If recipe is selected, swap with selected.
		if (open.getSelectedRecipeIndex() > 0 && open.getSelectedRecipeIndex() < recipes.size()) {
			recipe = recipes.set(open.getSelectedRecipeIndex(), recipe);
		}

		recipes.add(recipe);
		open.getMerchant().setRecipes(recipes);
		InventoryUtil.updateVillagerTrades(player, recipes);
		setSlots(player, open, expCost, result, fullUpdate);
	}

	/**
	 * Set slots and update inventory as required.
	 *
	 * @param player the Player viewing the Inventory
	 * @param open the open Inventory
	 * @param expCost the ItemStack to set in the second slot
	 * @param fullUpdate true if the entire inventory should be updated instead of just relevant slots
	 */
	private void setSlots(@NotNull Player player, @NotNull Inventory open, @Nullable ItemStack expCost,
			@Nullable ItemStack result, boolean fullUpdate) {
		open.setItem(1, expCost);
		open.setItem(2, result);
		if (fullUpdate) {
			player.updateInventory();
		} else {
			InventoryUtil.updateWindowSlot(player, 0);
			InventoryUtil.updateWindowSlot(player, 1);
			InventoryUtil.updateWindowSlot(player, 2);
		}
	}

	/**
	 * Singleton for getting the usage help recipe.
	 */
	private @NotNull static MerchantRecipe getExampleRecipe() {
		if (exampleRecipe == null) {
			ItemStack input = new ItemStack(Material.DIRT, 64);
			GenericUtil.consumeAs(ItemMeta.class, input.getItemMeta(), itemMeta -> {
				itemMeta.setDisplayName(ChatColor.GOLD + "Input");
				List<String> lore = Collections.singletonList(ChatColor.WHITE + "Insert item here.");
				itemMeta.setLore(lore);
				input.setItemMeta(itemMeta);
			});

			ItemStack cost = new ItemStack(Material.EXPERIENCE_BOTTLE);
			GenericUtil.consumeAs(ItemMeta.class, cost.getItemMeta(), itemMeta -> {
				itemMeta.setDisplayName(ChatColor.GOLD + "Mana Cost");
				List<String> lore = Arrays.asList(ChatColor.WHITE + "Displays dublecation cost",
						ChatColor.WHITE + "when an item is inserted.");
				itemMeta.setLore(lore);
				cost.setItemMeta(itemMeta);
			});
			ItemStack result = new ItemStack(Material.DIRT, 64);
			GenericUtil.consumeAs(ItemMeta.class, cost.getItemMeta(), itemMeta -> {
				itemMeta.setDisplayName(ChatColor.GOLD + "Copy of Input");
				List<String> lore = Collections.singletonList(ChatColor.WHITE + "Dublecate your items.");
				itemMeta.setLore(lore);
				result.setItemMeta(itemMeta);
			});

			MerchantRecipe recipe = new MerchantRecipe(result, Integer.MAX_VALUE);
			recipe.addIngredient(input);
			recipe.addIngredient(cost);

			exampleRecipe = recipe;
		}
		return exampleRecipe;
	}

	@Override
	public void handleClose(@NotNull InventoryCloseEvent event, @Nullable ConfigurationSection storage) {
		// Clear exp item
		event.getView().getTopInventory().setItem(1, null);
	}

	@Override
	public void selectTrade(TradeSelectEvent event, @NotNull ConfigurationSection storage) {
		event.getInventory().setItem(1, null);
		updateInventory(event.getWhoClicked().getUniqueId(), true);
	}

}
