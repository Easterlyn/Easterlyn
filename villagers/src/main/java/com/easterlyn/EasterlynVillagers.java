package com.easterlyn;

import com.easterlyn.event.ReportableEvent;
import com.easterlyn.util.EconomyUtil;
import com.easterlyn.util.NumberUtil;
import com.easterlyn.util.event.SimpleListener;
import com.easterlyn.util.inventory.ItemUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class EasterlynVillagers extends JavaPlugin {

	private final NamespacedKey versionKey = new NamespacedKey(this, "balanceVersion");
	private static final int VERSION = 1;

	@Override
	public void onEnable() {
		ChunkLoadEvent.getHandlerList().register(new SimpleListener<>(ChunkLoadEvent.class,
				event -> getServer().getScheduler().runTask(this, () -> {
					for (Entity entity : event.getChunk().getEntities()) {
						if (entity instanceof Merchant) {
							adjustMerchant((Merchant) entity);
						}
					}
				}), this));

		EntitySpawnEvent.getHandlerList().register(new SimpleListener<>(EntitySpawnEvent.class, event -> {
			if (event.getEntity() instanceof Merchant) {
				adjustMerchant((Merchant) event.getEntity());
			}
		}, this));

		VillagerAcquireTradeEvent.getHandlerList().register(new SimpleListener<>(VillagerAcquireTradeEvent.class, event -> {
			MerchantRecipe adjustRecipe = adjustRecipe(event.getRecipe());
			if (adjustRecipe == null) {
				event.setCancelled(true);
			} else {
				event.setRecipe(adjustRecipe);
			}
		}, this));
	}

	@SuppressWarnings("unused") // IDE does not recognize indirect usage.
	private enum CurrencyType {
		LAPIS(new ItemStack(Material.LAPIS_LAZULI), 1),
		LAPIS_BLOCK(new ItemStack(Material.LAPIS_BLOCK), 9),
		EMERALD(new ItemStack(Material.EMERALD), 81),
		EMERALD_BLOCK(new ItemStack(Material.EMERALD_BLOCK), 729),
		NOPE(new ItemStack(Material.BARRIER, 65), Integer.MAX_VALUE);

		static int getValue(final ItemStack currencyItem) {
			for (CurrencyType currencyType : CurrencyType.values()) {
				if (currencyType.getCurrencyItem().isSimilar(currencyItem)) {
					return currencyType.getValue() * currencyItem.getAmount();
				}
			}
			return 0;
		}
		static boolean isCurrency(final ItemStack currencyItem) {
			for (CurrencyType currencyType : CurrencyType.values()) {
				if (currencyType.getCurrencyItem().isSimilar(currencyItem)) {
					return true;
				}
			}
			return false;
		}

		private final ItemStack currencyItem;

		private final int value;

		CurrencyType(final ItemStack currencyItem, final int value) {
			this.currencyItem = currencyItem;
			this.value = value;
		}

		ItemStack getCurrencyItem() {
			return this.currencyItem.clone();
		}

		int getValue() {
			return this.value;
		}
	}

	private void adjustMerchant(final Merchant merchant) {
		if (merchant instanceof PersistentDataHolder) {
			PersistentDataHolder dataHolder = (PersistentDataHolder) merchant;
			int storedVersion = dataHolder.getPersistentDataContainer().getOrDefault(versionKey, PersistentDataType.INTEGER, 0);
			if (storedVersion == VERSION) {
				return;
			}
		}
		List<MerchantRecipe> newRecipes = new ArrayList<>();
		for (MerchantRecipe recipe : merchant.getRecipes()) {
			MerchantRecipe newRecipe = this.adjustRecipe(recipe);
			if (newRecipe != null) {
				newRecipes.add(this.adjustRecipe(recipe));
			}
		}
		merchant.setRecipes(newRecipes);

		if (merchant instanceof PersistentDataHolder) {
			((PersistentDataHolder) merchant).getPersistentDataContainer().set(versionKey, PersistentDataType.INTEGER, VERSION);
		}
	}

	private MerchantRecipe adjustRecipe(final MerchantRecipe recipe) {
		if (recipe.getIngredients().size() < 1) {
			// Ensure recipe has inputs.
			return null;
		}
		ItemStack input1 = recipe.getIngredients().get(0);
		ItemStack input2 = recipe.getIngredients().size() > 1 ? recipe.getIngredients().get(1) : ItemUtil.AIR;

		MerchantRecipe adjusted;

		try {
			adjusted = this.adjustRecipe(input1, input2, recipe.getResult(),
					recipe.getUses(), recipe.getMaxUses(), recipe.hasExperienceReward(), recipe.getVillagerExperience());
		} catch (Exception e) {
			ReportableEvent.call(String.format("Error adjusting trade: %s + %s -> %s", input1, input2, recipe.getResult()), e, 5);
			e.printStackTrace();
			return null;
		}

		if (adjusted == null && input1.getType() != Material.WRITTEN_BOOK && !(CurrencyType.isCurrency(input1)
				&& CurrencyType.isCurrency(recipe.getResult())) && recipe.getResult().getType() != Material.EXPERIENCE_BOTTLE) {
			// DEBUG: post report on un-adjustable trades

			ReportableEvent.call(String.format("Unable to adjust trade: %s + %s -> %s", input1, input2, recipe.getResult()));
		}

		return adjusted;
	}

	private MerchantRecipe adjustRecipe(@NotNull ItemStack input1, @NotNull ItemStack input2, @NotNull ItemStack result,
			final int uses, final int maxUses, final boolean giveExp, final int villagerExperience) {
		if (result.getType() == Material.EXPERIENCE_BOTTLE || input1.getType() == Material.WRITTEN_BOOK) {
			return null;
		}
		if (CurrencyType.isCurrency(input1) && (input2.getType() == Material.AIR || CurrencyType.isCurrency(input2))
				&& !CurrencyType.isCurrency(result)) {
			// TODO: Does not support value > 64EB (e.g. item worth 80 EB will be unpurchasable instead of 64 and 16 EB)
			// Purchase result - deal is not supposed to be good.
			// Use overpriced rate for worth of result.
			double resultCost = NumberUtil.multiplySafe(EconomyUtil.getWorth(result), EconomyUtil.OVERPRICED_RATE);
			// Round down - second stack will cover remainder.
			input1 = this.getSingleMoneyStack(resultCost, RoundingMode.DOWN);

			if (input1.getType() == Material.BARRIER) {
				// Too valuable.
				return null;
			}

			double remainder = resultCost - CurrencyType.getValue(input1);
			MerchantRecipe recipe = new MerchantRecipe(result, uses, maxUses, giveExp, villagerExperience, 0.0F);

			if (remainder > 0) {
				input2 = this.getSingleMoneyStack(remainder, RoundingMode.UP);
				if (input1.isSimilar(input2)) {
					int amount = input1.getAmount() + input2.getAmount();
					if (amount > input1.getMaxStackSize()) {
						amount -= input1.getMaxStackSize();
						input1.setAmount(input1.getMaxStackSize());
						input2.setAmount(amount);
						recipe.addIngredient(input1);
						recipe.addIngredient(input2);
					} else {
						input1.setAmount(amount);
						recipe.addIngredient(input1);
					}
				} else {
					recipe.addIngredient(input1);
				}
			} else {
				recipe.addIngredient(input1);
			}

			return recipe;
		}
		if (!CurrencyType.isCurrency(input1) && input2.getType() == Material.AIR && CurrencyType.isCurrency(result)) {
			// Sell input - deal is not supposed to be good.
			// Use overpriced rate for worth of result.
			double inputCost = EconomyUtil.getWorth(input1) * EconomyUtil.UNDERPRICED_RATE;
			// Round down - reduce value of trade further.
			result = this.getSingleMoneyStack(inputCost, RoundingMode.DOWN);

			if (result.getType() == Material.BARRIER) {
				return null;
			}

			MerchantRecipe recipe = new MerchantRecipe(result, uses, maxUses, giveExp, villagerExperience, 0.0F);
			recipe.addIngredient(input1);
			return recipe;
		}
		if (CurrencyType.isCurrency(input1) && !CurrencyType.isCurrency(input2)) {
			// Cartographers swap money and item for no apparent reason.
			// Purchase has already been handled so input2 is not empty and not currency.
			ItemStack swap = input1;
			input1 = input2;
			input2 = swap;
		}
		if (!CurrencyType.isCurrency(input1) && CurrencyType.isCurrency(input2)
				&& !CurrencyType.isCurrency(result)) {
			// Modification of input for result - deal is not supposed to be good.
			// Use overpriced rate for worth of result.
			double resultCost = NumberUtil.multiplySafe(EconomyUtil.getWorth(result), EconomyUtil.OVERPRICED_RATE);
			// Use underpriced rate for input.
			double inputCost = NumberUtil.multiplySafe(EconomyUtil.getWorth(result), EconomyUtil.UNDERPRICED_RATE);
			double cost = Math.abs(resultCost - inputCost);
			// Round up money.
			ItemStack money = this.getSingleMoneyStack(cost, RoundingMode.UP);
			if (money.getType() == Material.BARRIER) {
				// Too valuable.
				return null;
			}

			MerchantRecipe recipe = new MerchantRecipe(result, uses, maxUses, giveExp, villagerExperience, 0.0F);
			recipe.addIngredient(input1);
			recipe.addIngredient(money);
			return recipe;
		}
		return null;
	}

	// TODO prioritize not rounding to make hero of the village more beneficial
	private ItemStack getSingleMoneyStack(final double worth, final RoundingMode mode) {

		if (worth == Double.MAX_VALUE) {
			return CurrencyType.NOPE.getCurrencyItem().clone();
		}

		CurrencyType[] currencies = CurrencyType.values();
		CurrencyType currency = currencies[0];

		if (worth <= 0) {
			// Minimum 1 money
			return currency.getCurrencyItem().clone();
		}

		double amount = worth;
		int roundAmount = new BigDecimal(amount).setScale(0, mode).intValue();

		for (int i = currency.ordinal() + 1; amount > 1 && i < currencies.length; ++i) {
			CurrencyType nextCurrency = currencies[i];
			double currencyMultiplier = (double) nextCurrency.getValue() / currency.getValue();
			if (amount < currencyMultiplier) {
				break;
			}
			double nextAmount = amount / currencyMultiplier;
			int nextRound = new BigDecimal(nextAmount).setScale(0, mode).intValue();
			double roundRemainder = nextRound % currencyMultiplier;

			// Next currency conditions
			if (
				// Amount cannot be over 64, always increment.
					amount > 64
							// Next currency divides in evenly
							|| roundRemainder == 0
							// Amount > 2 of the next currency and
							|| (amount > currencyMultiplier * 2 &&
							(mode == RoundingMode.UP || mode == RoundingMode.CEILING)
							// Rounding up, remainder is close
							? roundRemainder > currencyMultiplier - currencyMultiplier / 4
							// Rounding down, remainder is close
							: mode == RoundingMode.DOWN || mode == RoundingMode.FLOOR
							? roundRemainder < currencyMultiplier / 4
							// Other rounding mode, close to either end
							: Math.abs(roundRemainder - currencyMultiplier / 2) < currencyMultiplier / 4)
							|| amount > currencyMultiplier * 4 && Math.abs(roundRemainder - currencyMultiplier / 2) < currencyMultiplier / 6
			) {
				// Set new values
				currency = nextCurrency;
				amount = nextAmount;
				roundAmount = nextRound;
				continue;
			}
			break;
		}

		if (roundAmount > 64) {
			return CurrencyType.NOPE.getCurrencyItem();
		}

		if (roundAmount < 1) {
			roundAmount = 1;
		}

		ItemStack currencyItem = currency.getCurrencyItem();
		currencyItem.setAmount(roundAmount);
		return currencyItem;
	}

}
