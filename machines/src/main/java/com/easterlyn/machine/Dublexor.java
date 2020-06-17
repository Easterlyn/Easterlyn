package com.easterlyn.machine;

import com.easterlyn.EasterlynCaptchas;
import com.easterlyn.EasterlynMachines;
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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
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
	// TODO:
	//  - single item doesn't work
	//  - cost display slot doesn't refresh properly

	private static MerchantRecipe exampleRecipe;

	private final NamespacedKey dublekey;
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

		dublekey = new NamespacedKey(machines, "dublexor");

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
			updateLater(event.getWhoClicked().getUniqueId(), this::addInputRecipe);
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

		MerchantInventory merchantInv = (MerchantInventory) event.getView().getTopInventory();
		ItemStack expDisplay = merchantInv.getItem(1);

		if (expDisplay == null ||expDisplay.getType() == Material.AIR) {
			// No exp cost set up.
			event.setCancelled(true);
			return;
		}

		MerchantRecipe selectedRecipe = merchantInv.getSelectedRecipe();

		if (selectedRecipe == null) {
			// Operation is not happening.
			event.setCancelled(true);
			return;
		}

		int expCost = expDisplay.getItemMeta().getPersistentDataContainer()
				.getOrDefault(dublekey, PersistentDataType.INTEGER, Integer.MAX_VALUE);
		ItemStack originalInput = merchantInv.getItem(0);
		ItemStack clonedInput;
		if (originalInput != null) {
			clonedInput = originalInput.clone();
		} else {
			clonedInput = null;
		}

		int recipeIndex = merchantInv.getSelectedRecipeIndex();
		int uses = selectedRecipe.getUses();

		// TODO while this is technically safe (recipes are capped by uses) could use more double checking
		updateLater(event.getWhoClicked().getUniqueId(), (player, inventory) -> {
			if (player.getGameMode() == GameMode.CREATIVE) {
				return;
			}
			MerchantRecipe recipe = inventory.getMerchant().getRecipe(recipeIndex);
			int timesUsed = recipe.getUses() - uses;
			ExperienceUtil.changeExp(player, -expCost * timesUsed);
		}, (player, inventory) -> {
			inventory.setItem(0, clonedInput);
			inventory.setItem(1, getExpDisplay(player, clonedInput));
		}, this::recalculateUses);
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
			updateLater(event.getWhoClicked().getUniqueId(), this::setExpDisplay, this::addInputRecipe);
		}
	}

	@Override
	public void selectTrade(@NotNull TradeSelectEvent event, @NotNull ConfigurationSection storage) {
		event.getInventory().setItem(1, null);
		updateLater(event.getWhoClicked().getUniqueId(), this::setExpDisplay, this::addInputRecipe);
	}

	/**
	 * Calculate result slot and update inventory on a delay (post-event completion)
	 *
	 * @param id the UUID of the Player using the Dublexor
	 */
	@SafeVarargs
	private final void updateLater(final @NotNull UUID id,
			final @NotNull BiConsumer<Player, MerchantInventory>... consumers) {
		if (consumers.length == 0) {
			throw new IllegalStateException("You dummy, you forgot the update functions.");
		}
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
			for (BiConsumer<Player, MerchantInventory> consumer : consumers) {
				consumer.accept(player, open);
			}
		});
	}

	private void setExpDisplay(Player player, MerchantInventory open) {
		open.setItem(1, getExpDisplay(player, open.getItem(0)));
		InventoryUtil.updateWindowSlot(player, 1);
	}

	private ItemStack getExpDisplay(@NotNull Player player, @Nullable ItemStack input) {
		if (input == null || input.getType() == Material.AIR) {
			return null;
		}

		ItemStack expItem = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);

		Pair<ItemStack, Integer> uncaptcha = unCaptcha(input);
		ItemStack modifiedInput = uncaptcha.getLeft();
		int multiplier = uncaptcha.getRight();

		// Ensure non-unique item (excluding captchas)
		if (ItemUtil.isUniqueItem(modifiedInput)) {
			GenericUtil.consumeAs(ItemMeta.class, expItem.getItemMeta(), itemMeta -> {
				itemMeta.setDisplayName(ChatColor.RED + "Cannot copy");
				itemMeta.getPersistentDataContainer().set(dublekey, PersistentDataType.INTEGER, Integer.MIN_VALUE);
				expItem.setItemMeta(itemMeta);
			});
			return expItem;
		}

		// Calculate cost based on final item, adjusting for captcha depth and quantities.
		int cost;
		try {
			double doubleCost = NumberUtil.multiplySafe(EconomyUtil.getWorth(modifiedInput), multiplier);
			if (doubleCost > Integer.MAX_VALUE) {
				cost = Integer.MIN_VALUE;
			} else {
				cost = (int) Math.ceil(doubleCost);
			}
		} catch (ArithmeticException e) {
			cost = Integer.MIN_VALUE;
		}

		int playerExp = ExperienceUtil.getExp(player);
		ArrayList<String> lore = new ArrayList<>();
		lore.add(ChatColor.GOLD + "Current: " + playerExp);
		int remainder = playerExp - cost;

		ChatColor color;
		if (remainder >= 0 || player.getGameMode() == GameMode.CREATIVE) {
			color = ChatColor.GREEN;
			lore.add(ChatColor.GOLD + "Remainder: " + remainder);
		} else {
			color = ChatColor.RED;
			lore.add(color.toString() + ChatColor.BOLD + "Not enough mana!");
		}

		if (player.getGameMode() == GameMode.CREATIVE) {
			lore.add(ChatColor.GOLD + "Creative exp bypass engaged.");
		}

		int finalCost = cost;
		GenericUtil.consumeAs(ItemMeta.class, expItem.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(color + "Mana cost: " + finalCost);
			itemMeta.setLore(lore);
			itemMeta.getPersistentDataContainer().set(dublekey, PersistentDataType.INTEGER, finalCost);
			expItem.setItemMeta(itemMeta);
		});
		return expItem;
	}

	private void addInputRecipe(Player player, MerchantInventory open) {
		ItemStack input = open.getItem(0);

		if (input == null || input.getType() == Material.AIR) {
			return;
		}

		input = input.clone();
		ArrayList<MerchantRecipe> recipes = new ArrayList<>(open.getMerchant().getRecipes());

		Iterator<MerchantRecipe> iterator = recipes.iterator();
		while (iterator.hasNext()) {
			MerchantRecipe recipe = iterator.next();
			// Existing recipe matches.
			if (recipe.getResult().equals(input)) {
				return;
			}
			// Existing recipe will interfere with client's ability to display trade.
			if (recipe.getResult().isSimilar(input)) {
				iterator.remove();
				break;
			}
		}

		ItemStack result = input.clone();
		MerchantRecipe recipe = new MerchantRecipe(result, Integer.MAX_VALUE);
		recipe.addIngredient(input);
		recipe.addIngredient(new ItemStack(Material.EXPERIENCE_BOTTLE));
		recalculateModifiesRecipe(player.getGameMode() == GameMode.CREATIVE ? Integer.MAX_VALUE : ExperienceUtil.getExp(player), recipe);
		recipes.add(recipe);
		open.getMerchant().setRecipes(recipes);
		open.setItem(2, result);

		InventoryUtil.updateVillagerTrades(player, recipes);
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
				break;
			}
			multiplier = Math.multiplyExact(multiplier, Math.max(1, Math.abs(potentialCaptcha.getAmount())));
			potentialCaptcha = newModInput;
		}

		return new Pair<>(potentialCaptcha, multiplier);
	}

	private void recalculateUses(@NotNull Player player, @NotNull MerchantInventory inventory) {
		int expTotal = player.getGameMode() == GameMode.CREATIVE ? Integer.MAX_VALUE : ExperienceUtil.getExp(player);
		boolean updatedRecipes = false;

		List<MerchantRecipe> recipes = new ArrayList<>(inventory.getMerchant().getRecipes());

		for (MerchantRecipe recipe : recipes) {
			if (recalculateModifiesRecipe(expTotal, recipe)) {
				updatedRecipes = true;
			}
		}

		if (!updatedRecipes) {
			return;
		}

		inventory.getMerchant().setRecipes(recipes);
		InventoryUtil.updateVillagerTrades(player, recipes);
	}

	private boolean recalculateModifiesRecipe(int expTotal, @NotNull MerchantRecipe recipe) {
		List<ItemStack> ingredients = recipe.getIngredients();
		if (ingredients.size() < 2) {
			return false;
		}

		Pair<ItemStack, Integer> uncaptcha = unCaptcha(recipe.getIngredients().get(0).clone());
		ItemStack modifiedInput = uncaptcha.getLeft();
		int multiplier = uncaptcha.getRight();

		// Ensure non-unique item (excluding captchas)
		if (ItemUtil.isUniqueItem(modifiedInput)) {
			return denyModifiesRecipe(recipe);
		}

		int cost;

		// Calculate cost based on final item, adjusting for captcha depth and quantities.
		try {
			double doubleCost = NumberUtil.multiplySafe(EconomyUtil.getWorth(modifiedInput), multiplier);
			if (doubleCost > Integer.MAX_VALUE) {
				cost = Integer.MIN_VALUE;
			} else {
				cost = (int) Math.ceil(doubleCost);
			}
		} catch (ArithmeticException e) {
			cost = Integer.MIN_VALUE;
		}

		// Disallow costs < 0.
		if (cost <= 0) {
			return denyModifiesRecipe(recipe);
		}

		// Allow infinite uses in creative.
		if (expTotal == Integer.MAX_VALUE) {
			if (recipe.getMaxUses() != Integer.MAX_VALUE) {
				recipe.setMaxUses(Integer.MAX_VALUE);
				recipe.setUses(0);
				return true;
			}
			return false;
		}

		int uses = expTotal / cost;
		if (uses <= 0) {
			return denyModifiesRecipe(recipe);
		}

		if (recipe.getMaxUses() - recipe.getUses() == uses) {
			return false;
		}

		recipe.setMaxUses(recipe.getUses() + uses);
		return true;
	}

	private boolean denyModifiesRecipe(@NotNull MerchantRecipe recipe) {
		if (recipe.getUses() != recipe.getMaxUses() || recipe.getMaxUses() < 1) {
			recipe.setMaxUses(1);
			recipe.setUses(1);
			return true;
		}
		return false;
	}

	@Override
	public void handleClose(@NotNull InventoryCloseEvent event, @Nullable ConfigurationSection storage) {
		// Clear exp item
		event.getView().getTopInventory().setItem(1, null);
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

			MerchantRecipe recipe = new MerchantRecipe(result, 1);
			recipe.addIngredient(input);
			recipe.addIngredient(cost);
			recipe.setUses(1);

			exampleRecipe = recipe;
		}
		return exampleRecipe;
	}

}
