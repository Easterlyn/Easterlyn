package com.easterlyn.machines.machine;

import com.easterlyn.captcha.EasterlynCaptcha;
import com.easterlyn.machines.EasterlynMachines;
import com.easterlyn.machines.Machine;
import com.easterlyn.util.Direction;
import com.easterlyn.util.EconomyUtil;
import com.easterlyn.util.ExperienceUtil;
import com.easterlyn.util.GenericUtil;
import com.easterlyn.util.inventory.ItemUtil;
import com.easterlyn.util.NumberUtil;
import com.easterlyn.util.Shape;
import com.easterlyn.util.tuple.Pair;
import com.easterlyn.util.tuple.Triple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
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
public class Dublexor extends Machine implements InventoryHolder, Merchant {

	private static Triple<ItemStack, ItemStack, ItemStack> exampleRecipes;

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
		event.getPlayer().openInventory(getInventory());
		event.setCancelled(true);
	}

	@Override
	public void handleClick(@NotNull InventoryClickEvent event, ConfigurationSection storage) {
		updateInventory(event.getWhoClicked().getUniqueId());
		if (event.getRawSlot() != event.getView().convertSlot(event.getRawSlot())) {
			// Clicked inv is not the top.
			return;
		}
		if (event.getSlot() == 1) {
			// Exp slot is being clicked. No adding or removing items.
			event.setCancelled(true);
			return;
		}
		if (event.getSlot() == 2 && event.getCurrentItem() != null
				&& event.getCurrentItem().getType() != Material.AIR) {
			if (event.getCurrentItem().getType() == Material.BARRIER) {
				event.setCancelled(true);
				return;
			}

			// Item is being crafted
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
				expCost = Integer.valueOf(costString);
			} catch (NumberFormatException e) {
				System.err.println("Unable to parse ");
				e.printStackTrace();
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
				event.setCancelled(true);
				return;
			}
			event.setCurrentItem(null);
		}
	}

	@Override
	public void handleDrag(@NotNull InventoryDragEvent event, ConfigurationSection storage) {
		updateInventory(event.getWhoClicked().getUniqueId());
		// Raw slot 1 = second slot of top inventory
		if (event.getRawSlots().contains(1)) {
			event.setCancelled(true);
		}
	}

	/**
	 * Calculate result slot and update inventory on a delay (post-event completion)
	 *
	 * @param id the UUID of the Player using the Dublexor
	 */
	private void updateInventory(final UUID id) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(getMachines(), () -> {
			// Must re-obtain player or update doesn't seem to happen
			Player player = Bukkit.getPlayer(id);
			if (player == null || !(player.getOpenInventory().getTopInventory().getHolder() instanceof Dublexor)) {
				// Player has logged out or closed inventory. Inventories are per-player, ignore.
				return;
			}

			Inventory open = player.getOpenInventory().getTopInventory();
			ItemStack originalInput = open.getItem(0);

			if (originalInput == null || originalInput.getType() == Material.AIR) {
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
				setSecondTrade(player, open, originalInput, expCost, barrier);
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
			setSecondTrade(player, open, originalInput, expCost, result);
		});
	}

	private Pair<ItemStack, Integer> unCaptcha(ItemStack potentialCaptcha) {
		try {
			Class.forName("com.easterlyn.captcha.EasterlynCaptcha");
		} catch (ClassNotFoundException e) {
			return new Pair<>(potentialCaptcha, 1);
		}

		RegisteredServiceProvider<EasterlynCaptcha> registration = getMachines().getServer().getServicesManager().getRegistration(EasterlynCaptcha.class);
		if (registration == null) {
			return new Pair<>(potentialCaptcha, 1);
		}

		int multiplier = 1;
		while (EasterlynCaptcha.isUsedCaptcha(potentialCaptcha)) {
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

	private void setSecondTrade(@NotNull Player player, @NotNull Inventory open, @NotNull ItemStack input,
			@NotNull ItemStack expCost, @NotNull ItemStack result) {
		open.setItem(1, expCost);
		open.setItem(2, result);
		ItemUtil.updateVillagerTrades(player, getExampleRecipes(), new Triple<>(input, expCost, result));
		player.updateInventory();
	}

	/**
	 * Singleton for getting usage help ItemStacks.
	 */
	@NotNull
	private static Triple<ItemStack, ItemStack, ItemStack> getExampleRecipes() {
		if (exampleRecipes == null) {
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

			exampleRecipes = new Triple<>(input, cost, result);
		}
		return exampleRecipes;
	}

	@NotNull
	@Override
	public Inventory getInventory() {
		return Bukkit.createInventory(this, InventoryType.MERCHANT);
	}

	@NotNull
	@Override
	public List<MerchantRecipe> getRecipes() {
		Triple<ItemStack, ItemStack, ItemStack> exampleRecipes = getExampleRecipes();
		MerchantRecipe recipe = new MerchantRecipe(exampleRecipes.getRight(), Integer.MAX_VALUE);
		recipe.addIngredient(exampleRecipes.getLeft());
		recipe.addIngredient(exampleRecipes.getMiddle());
		return Collections.singletonList(recipe);
	}

	@Override
	public void setRecipes(@NotNull List<MerchantRecipe> list) {}

	@NotNull
	@Override
	public MerchantRecipe getRecipe(int i) throws IndexOutOfBoundsException {
		Triple<ItemStack, ItemStack, ItemStack> exampleRecipes = getExampleRecipes();
		MerchantRecipe recipe = new MerchantRecipe(exampleRecipes.getRight(), Integer.MAX_VALUE);
		recipe.addIngredient(exampleRecipes.getLeft());
		recipe.addIngredient(exampleRecipes.getMiddle());
		return recipe;
	}

	@Override
	public void setRecipe(int i, @NotNull MerchantRecipe merchantRecipe) throws IndexOutOfBoundsException {}

	@Override
	public int getRecipeCount() {
		return 1;
	}

	@Override
	public boolean isTrading() {
		return false;
	}

	@Nullable
	@Override
	public HumanEntity getTrader() {
		return null;
	}

}
