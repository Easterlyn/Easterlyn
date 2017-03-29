package com.easterlyn.micromodules;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.CruxiteDowel;
import com.easterlyn.effects.Effects;
import com.easterlyn.module.Module;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.material.Dye;

/**
 * 
 * 
 * @author Jikoo
 */
public class VillagerAdjustment extends Module {

	private enum CurrencyType {
		LAPIS(new Dye(DyeColor.BLUE).toItemStack(), 1),
		LAPIS_BLOCK(new ItemStack(Material.LAPIS_BLOCK), 9),
		EMERALD(new ItemStack(Material.EMERALD), 81),
		EMERALD_BLOCK(new ItemStack(Material.EMERALD), 729),
		NOPE(new ItemStack(Material.BARRIER, 65), Integer.MAX_VALUE);

		private final ItemStack currencyItem;
		private final int value;

		private CurrencyType(ItemStack currencyItem, int value) {
			this.currencyItem = currencyItem;
			this.value = value;
		}

		public ItemStack getCurrencyItem() {
			return this.currencyItem.clone();
		}

		public int getValue() {
			return this.value;
		}

		public static boolean isCurrency(ItemStack currencyItem) {
			for (CurrencyType currencyType : values()) {
				if (currencyType.getCurrencyItem().isSimilar(currencyItem)) {
					return true;
				}
			}
			return false;
		}

		public static int getValue(ItemStack currencyItem) {
			for (CurrencyType currencyType : values()) {
				if (currencyType.getCurrencyItem().isSimilar(currencyItem)) {
					return currencyType.getValue() * currencyItem.getAmount();
				}
			}
			return 0;
		}
	}

	private final double overpricedRate, normalRate, underpricedRate;

	private Effects effects;

	public VillagerAdjustment(Easterlyn plugin) {
		super(plugin);
		// TODO: Store somewhere or make externally readable - used by WorthCommand
		this.overpricedRate = 25;
		this.normalRate = 111.3;
		this.underpricedRate = 450;
	}

	@Override
	protected void onEnable() {
		this.effects = this.getPlugin().getModule(Effects.class);
	}

	@Override
	protected void onDisable() {}

	public void adjustMerchant(Merchant merchant) {
		List<MerchantRecipe> newRecipes = new ArrayList<>();
		for (MerchantRecipe recipe : merchant.getRecipes()) {
			MerchantRecipe newRecipe = adjustRecipe(recipe);
			if (newRecipe != null) {
				newRecipes.add(adjustRecipe(recipe));
			}
		}
		merchant.setRecipes(newRecipes);
	}

	public MerchantRecipe adjustRecipe(MerchantRecipe recipe) {
		if (recipe.getIngredients().size() < 1) {
			// Ensure recipe has inputs.
			return null;
		}
		return adjustRecipe(recipe.getIngredients().get(0),
				recipe.getIngredients().size() > 1 ? recipe.getIngredients().get(1) : null,
				recipe.getResult(), recipe.getUses(), recipe.getMaxUses(), recipe.hasExperienceReward());
	}

	private MerchantRecipe adjustRecipe(ItemStack input1, ItemStack input2, ItemStack result,
			int uses, int maxUses, boolean giveExp) {
		if (!CurrencyType.isCurrency(input1) && CurrencyType.isCurrency(input2)
				&& !CurrencyType.isCurrency(result)) {
			// Modification of input for result - deal is not supposed to be good.
			// Use overpriced rate for worth of result.
			double resultCost = CruxiteDowel.expCost(effects, result) / overpricedRate;
			// Use underpriced rate for input.
			double inputCost = CruxiteDowel.expCost(effects, input1) / underpricedRate;
			double cost = Math.abs(resultCost - inputCost);
			// Round up money.
			ItemStack money = getSingleMoneyStack(cost, RoundingMode.UP);
			if (money.getType() == Material.BARRIER) {
				// Too valuable.
				return null;
			}

			MerchantRecipe recipe = new MerchantRecipe(result, uses, maxUses, giveExp);
			recipe.addIngredient(input1);
			recipe.addIngredient(money);
			return recipe;
		}
		if (CurrencyType.isCurrency(input1) && (input2 == null || input2.getType() == Material.AIR)
				&& !CurrencyType.isCurrency(result)) {
			// TODO: Does not support value > 64EB (e.g. item worth 80 EB will be unpurchasable instead of 64 and 16 EB)
			// Purchase result - deal is not supposed to be good.
			// Use overpriced rate for worth of result.
			double resultCost = CruxiteDowel.expCost(effects, result) / overpricedRate;
			// Round down - second stack will cover remainder.
			input1 = this.getSingleMoneyStack(resultCost, RoundingMode.DOWN);

			if (input1.getType() == Material.BARRIER) {
				// Too valuable.
				return null;
			}

			MerchantRecipe recipe = new MerchantRecipe(result, uses, maxUses, giveExp);
			recipe.addIngredient(input1);

			double remainder = resultCost - CurrencyType.getValue(input1);

			if (remainder > 0) {
				// TODO: merge stacks if similar due to rounding up
				recipe.addIngredient(this.getSingleMoneyStack(remainder, RoundingMode.UP));
			}

			return recipe;
		}
		if (!CurrencyType.isCurrency(input1) && (input2 == null || input2.getType() == Material.AIR)
				&& CurrencyType.isCurrency(result)) {
			// Sell input - deal is not supposed to be good.
			// Use overpriced rate for worth of result.
			double inputCost = CruxiteDowel.expCost(effects, input1) / underpricedRate;
			// Round down - reduce value of trade further.
			result = this.getSingleMoneyStack(inputCost, RoundingMode.DOWN);

			if (result.getType() == Material.BARRIER) {
				return null;
			}

			MerchantRecipe recipe = new MerchantRecipe(result, uses, maxUses, giveExp);
			recipe.addIngredient(input1);
			return recipe;
		}
		return null;
	}

	private ItemStack getSingleMoneyStack(double worth, RoundingMode mode) {
		CurrencyType[] currencies = CurrencyType.values();
		CurrencyType currency = currencies[0];

		if (worth <= 0) {
			// Minimum 1 money
			return currency.getCurrencyItem().clone();
		}

		double amount = worth;
		int roundAmount = new BigDecimal(amount).setScale(0, mode).intValue();

		for (int i = currency.ordinal() + 1; i < currencies.length; ++i) {
			CurrencyType nextCurrency = currencies[i];
			double currencyMultiplier = ((double) nextCurrency.getValue()) / currency.getValue();
			double nextAmount = amount / currencyMultiplier;
			int nextRound = new BigDecimal(nextAmount).setScale(0, mode).intValue();
			double roundRemainder = nextRound % currencyMultiplier;

			// Next currency conditions
			if (
					// Amount cannot be over 64, always increment.
					amount > 64
					// Next currency divides in evenly
					|| roundRemainder == 0
					// Amount > 18 (hardcoded, currencies are 9 difference) and
					|| (amount > 18 && 
							(mode == RoundingMode.UP || mode == RoundingMode.CEILING)
							// Rounding up, remainder is close
							? roundRemainder > currencyMultiplier - (currencyMultiplier / 4)
									// Rounding down, remainder is close
									: (mode == RoundingMode.DOWN || mode == RoundingMode.FLOOR)
									? roundRemainder < currencyMultiplier / 4
											// Other rounding mode, close to either end
											: Math.abs(roundRemainder - (currencyMultiplier / 2)) < currencyMultiplier / 4)
					) {
				// Set new values
				currency = nextCurrency;
				amount = nextAmount;
				roundAmount = nextRound;
				continue;
			}
			break;
		}

		if (roundAmount < 1 || roundAmount > 64) {
			return CurrencyType.NOPE.getCurrencyItem();
		}

		ItemStack currencyItem = currency.getCurrencyItem();
		currencyItem.setAmount(roundAmount);
		return currencyItem;
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getName() {
		return "VillagerAdjustment";
	}

}
