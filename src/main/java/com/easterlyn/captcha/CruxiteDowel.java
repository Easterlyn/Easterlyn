package com.easterlyn.captcha;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.easterlyn.effects.Effects;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.utilities.InventoryUtils;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

/**
 * A class for handling all functions of cruxite dowels.
 * 
 * @author Jikoo
 */
public class CruxiteDowel {

	private static Map<Pair<Material, Short>, Double> manaMappings;
	private static final ItemStack DOWEL_ITEM;

	static {
		DOWEL_ITEM = new ItemStack(Material.NETHER_BRICK_ITEM);
		ItemMeta meta = DOWEL_ITEM.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Cruxite Totem");
		meta.setLore(Arrays.asList(Captcha.HASH_PREFIX + "00000000"));
		DOWEL_ITEM.setItemMeta(meta);
	}

	public static ItemStack getDowel() {
		return DOWEL_ITEM.clone();
	}

	public static boolean isBlankDowel(ItemStack is) {
		return DOWEL_ITEM.isSimilar(is);
	}

	public static boolean isDowel(ItemStack is) {
		return is != null && is.getType() == Material.NETHER_BRICK_ITEM && is.hasItemMeta()
				&& is.getItemMeta().hasLore() && is.getItemMeta().hasDisplayName()
				&& is.getItemMeta().getDisplayName().equals(ChatColor.WHITE + "Cruxite Totem");
	}

	public static boolean isUsedDowel(ItemStack is) {
		return isDowel(is) && is.getItemMeta().hasLore();
	}

	public static ItemStack carve(ItemStack is) {
		ItemStack dowel = getDowel();
		if (is == null || !is.hasItemMeta()) {
			return dowel;
		}
		ItemMeta im = dowel.getItemMeta();
		im.setLore(is.getItemMeta().getLore());
		dowel.setItemMeta(im);
		return dowel;
	}

	public static double expCost(Effects effects, ItemStack toCreate) {
		if (toCreate == null || toCreate.getAmount() < 1) {
			return Double.MAX_VALUE;
		}
		double cost = getMana().getOrDefault(new ImmutablePair<>(toCreate.getType(), toCreate.getDurability()), Double.MAX_VALUE);
		if (cost == Double.MAX_VALUE) {
			// Item cannot be made with mana
			return Double.MAX_VALUE;
		}
		if (!toCreate.hasItemMeta()) {
			// No additional costs from meta, finish fast.
			if (Double.MAX_VALUE / toCreate.getAmount() <= cost) {
				return Double.MAX_VALUE;
			}
			return cost * toCreate.getAmount();
		}
		if (InventoryUtils.isUniqueItem(effects.getPlugin(), toCreate)) {
			return Double.MAX_VALUE;
		}

		ItemMeta meta = toCreate.getItemMeta();

		// In case of shulker boxes, etc. do not (yet) allow duplicating unless empty.
		if (meta instanceof BlockStateMeta) {
			BlockState state = ((BlockStateMeta) meta).getBlockState();
			if (state instanceof InventoryHolder) {
				for (ItemStack item : ((InventoryHolder) state).getInventory().getContents()) {
					if (item != null && item.getType() != Material.AIR) {
						return Double.MAX_VALUE;
					}
				}
			}
		}

		if (meta.hasEnchants()) {
			for (Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
				// Note: All level values are from 1.7
				// (16 - weight) * level * 20 seems to be a good rate.
				// Sharpness 5: 6 * 5 * 20 = 300 exp, 0-26
				// silk touch 1: 15 * 1 * 20 = 300 exp
				// fortune 3: 14 * 3 * 20 = 840 exp, 0-30
				// Rebalanced to *40, removed 2x multiplier from final cost
				// Rebalanced weights to account for mending/frostwalker being rarer
				/*
				 * TODO recalc and explain reasoning, double check balance
				 * Enchantment levels are a short, no risk of overflow until added to cost.
				 */
				Enchantment enchant = entry.getKey();
				int enchantCost = (20 - getWeight(enchant));
				// Balance: Base cost on percentage of max level, not only current level
				enchantCost *= 225D / enchant.getMaxLevel() * Math.abs(entry.getValue());
				if (enchant.isCursed()) {
					// Curses are also treasure, should be handled first.
					enchantCost /= 1.5;
				} else if (enchant.isTreasure()) {
					// Rarer, increase cost.
					enchantCost *= 1.5;
				}
				if (Double.MAX_VALUE - enchantCost <= cost) {
					return Double.MAX_VALUE;
				}
				cost += enchantCost;
			}
			if (toCreate.getType().getMaxDurability() == 0) {
				if (Double.MAX_VALUE / 4 <= cost) {
					return Double.MAX_VALUE;
				}
				cost *= 4;
			}
		}

		if (meta instanceof EnchantmentStorageMeta) {
			for (Entry<Enchantment, Integer> entry : ((EnchantmentStorageMeta) meta).getStoredEnchants().entrySet()) {
				Enchantment enchant = entry.getKey();
				int enchantCost = (18 - getWeight(enchant));
				// Balance: Base cost on percentage of max level, not only current level
				enchantCost *= 200D / enchant.getMaxLevel() * Math.abs(entry.getValue());
				if (enchant.isCursed()) {
					// Curses are also treasure, should be handled first.
					enchantCost /= 1.5;
				} else if (enchant.isTreasure()) {
					// Rarer, increase cost.
					enchantCost *= 1.5;
				}
				if (Double.MAX_VALUE - enchantCost <= cost) {
					return Double.MAX_VALUE;
				}
				cost += enchantCost;
			}
		}

		if (toCreate.getItemMeta().hasDisplayName()) {
			// Naming an unenchanted item in an anvil costs 1 additional level in 1.8. Since we're nice, a fixed cost.
			if (Double.MAX_VALUE - 15 <= cost) {
				return Double.MAX_VALUE;
			}
			cost += 15;
		}

		int effectCost = 0;
		for (Entry<Effect, Integer> effect : effects.getEffects(true, toCreate).entrySet()) {
			int entryCost = effect.getKey().getCost();
			if (entryCost == Integer.MAX_VALUE || Integer.MAX_VALUE / effect.getValue() <= entryCost) {
				return Double.MAX_VALUE;
			}
			entryCost *= effect.getValue();
			if (Integer.MAX_VALUE - entryCost <= effectCost) {
				return Double.MAX_VALUE;
			}
			effectCost += entryCost;
		}
		// if item contains special lore and doesn't need repair, raise price
		if (toCreate.getType().getMaxDurability() == 0) {
			if (Integer.MAX_VALUE / 4 <= effectCost) {
				return Double.MAX_VALUE;
			}
			effectCost *= 4;
		}
		if (effectCost < 0 || Double.MAX_VALUE - effectCost <= cost) {
			return Double.MAX_VALUE;
		}
		cost += effectCost;

		if (Double.MAX_VALUE / toCreate.getAmount() <= effectCost) {
			return Double.MAX_VALUE;
		}
		cost *= toCreate.getAmount();

		return cost > 0 ? cost : Integer.MAX_VALUE;
	}

	public static Map<Pair<Material, Short>, Double> getMana() {
		if (manaMappings == null) {
			manaMappings = createBaseMana();
			// Fill from recipes
			for (Material material : Material.values()) {
				addRecipeCosts(material);
			}
			manaMappings.entrySet().forEach(entry -> {
				if (entry.getValue() == Double.MAX_VALUE) {
					System.out.println(String.format("Entry %s:%s is Double.MAX_VALUE",
							entry.getKey().getLeft(), entry.getKey().getRight()));
				}
			});
			manaMappings.entrySet().removeIf(entry -> entry.getValue() == Double.MAX_VALUE);
		}
		return manaMappings;
	}

	private static Map<Pair<Material, Short>, Double> createBaseMana() {
		Map<Pair<Material, Short>, Double> values = new HashMap<>();

		for (Material material : Material.values()) {
			switch(material) {
			case CLAY_BALL:
			case COOKIE:
			case DEAD_BUSH:
			case DIRT:
			case GRAVEL:
			case LEAVES:
			case LEAVES_2:
			case LONG_GRASS:
			case POISONOUS_POTATO:
			case SAND:
			case SEEDS:
			case SNOW_BALL:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 1D);
				break;
			case CACTUS:
			case CARROT_ITEM:
			case COBBLESTONE:
			case NETHER_BRICK_ITEM:
			case PAPER:
			case RABBIT_HIDE:
			case RED_MUSHROOM:
			case RED_ROSE:
			case SOUL_SAND:
			case SUGAR_CANE:
			case VINE:
			case WHEAT:
			case WATER_LILY:
			case YELLOW_FLOWER:
			case CHORUS_FRUIT:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 2D);
				break;
			case BROWN_MUSHROOM:
			case NETHERRACK:
			case HUGE_MUSHROOM_1:
			case HUGE_MUSHROOM_2:
			case POTATO_ITEM:
			case ROTTEN_FLESH:
			case STONE:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 3D);
				break;
			case ARROW:
			case DOUBLE_PLANT:
			case FEATHER:
			case RAW_CHICKEN:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 4D);
				break;
			case CLAY_BRICK:
			case FLINT:
			case RABBIT:
			case RAW_FISH:
			case WOOL:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 5D);
				break;
			case BAKED_POTATO:
			case EGG:
			case NETHER_BRICK:
			case PUMPKIN:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 6D);
				break;
			case COOKED_CHICKEN:
			case LOG:
			case LOG_2:
			case MUTTON:
			case RAW_BEEF:
			case REDSTONE:
			case STRING:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 8D);
				break;
			case COOKED_FISH:
			case NETHER_WARTS:
			case NETHER_STALK: // Same thing as warts in 1.8 inventories
			case PRISMARINE_SHARD:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 9D);
				break;
			case ENDER_STONE:
			case GLOWSTONE_DUST:
			case ICE:
			case LEATHER:
			case MELON:
			case MOSSY_COBBLESTONE:
			case PORK:
			case SLIME_BALL:
			case STAINED_GLASS:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 10D);
				break;
			case APPLE:
			case BONE:
			case COAL:
			case COOKED_BEEF:
			case GOLD_NUGGET:
			case RABBIT_FOOT:
			case SPIDER_EYE:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 12D);
				break;
			case STAINED_CLAY:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 13D);
				break;
			case GRILLED_PORK:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 14D);
				break;
			case SAPLING:
			case SADDLE:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 16D);
				break;
			case SULPHUR:
			case MAP: // Not crafted, right click
			case MYCEL:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 20D);
				break;
			case ENCHANTED_BOOK:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 25D);
				break;
			case PACKED_ICE:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 28D);
				break;
			case BLAZE_ROD:
			case GRASS:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 30D);
				break;
			case BANNER:
			case GHAST_TEAR:
			case PRISMARINE_CRYSTALS:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 35D);
				break;
			case QUARTZ:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 37D);
				break;
			case IRON_INGOT:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 41D);
				break;
			case COAL_ORE:
			case QUARTZ_ORE:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 44D);
				break;
			case GOLD_RECORD:
			case GREEN_RECORD:
			case IRON_ORE:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 50D);
				break;
			case RECORD_10:
			case RECORD_11:
			case RECORD_12:
			case RECORD_3:
			case RECORD_4:
			case RECORD_5:
			case RECORD_6:
			case RECORD_7:
			case RECORD_8:
			case RECORD_9:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 70D);
				break;
			case PISTON_BASE:
			case OBSIDIAN:
			case REDSTONE_ORE:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 81D);
				break;
			case ENDER_PEARL:
			case PISTON_STICKY_BASE:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 90D);
				break;
			case GOLD_INGOT:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 108D);
				break;
			case GOLD_ORE:
			case LAVA_BUCKET:
			case MILK_BUCKET:
			case WATER_BUCKET:
			case WEB:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 138D);
				break;
			case DIAMOND:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 167D);
				break;
			case DIAMOND_ORE:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 187D);
				break;
			case IRON_BARDING:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 261D);
				break;
			case NAME_TAG:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 405D);
				break;
			case CHAINMAIL_BOOTS:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 600D);
				break;
			case GOLD_BARDING:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 663D);
				break;
			case CHAINMAIL_HELMET:
			case SHULKER_SHELL:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 750D);
				break;
			case DIAMOND_BARDING:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 1000D);
				break;
			case CHAINMAIL_LEGGINGS:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 1050D);
				break;
			case CHAINMAIL_CHESTPLATE:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 1200D);
				break;
			case SKULL_ITEM:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 3000D);
				// Dragon head
				values.put(new ImmutablePair<Material, Short>(material, (short) 5), 16000D);
				break;
			case TOTEM:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 5000D);
				break;
			case NETHER_STAR:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 10000D);
				break;
			case ELYTRA:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 3142D);
				break;
			case DRAGON_EGG:
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), 32000D);
				break;
			// Unobtainable, don't bother searching recipes
			case AIR:
			case BARRIER:
			case BEDROCK:
			case INK_SACK: // Lapis is a dye
			case BOOK_AND_QUILL:
			case BURNING_FURNACE:
			case CARROT: // plant
			case CAKE_BLOCK:
			case COCOA: // plant
			case COMMAND:
			case COMMAND_MINECART:
			case CROPS: // plant
			case EMERALD: // Money
			case EMERALD_BLOCK: // Money
			case EMERALD_ORE: // Money
			case ENDER_PORTAL:
			case ENDER_PORTAL_FRAME:
			case EXP_BOTTLE: // Stupid to make exp with exp
			case FIRE:
			case GLOWING_REDSTONE_ORE:
			case LAPIS_BLOCK: // Money
			case LAPIS_ORE: // Money
			case LAVA:
			case MELON_STEM: // plant
			case MOB_SPAWNER:
			case MONSTER_EGG:
			case MONSTER_EGGS:
			case PISTON_EXTENSION:
			case PISTON_MOVING_PIECE:
			case PORTAL:
			case POTATO: // plant
			case POTION: // Removed until effects are accounted for
			case TIPPED_ARROW: // ^
			case SPLASH_POTION: // ^
			case LINGERING_POTION: // ^
			case PUMPKIN_STEM: // plant
			case SKULL:
			case SOIL:
			case SPONGE:
			case STATIONARY_LAVA:
			case STATIONARY_WATER:
			case WATER:
			case WRITTEN_BOOK: // Duplicate via other means, not alchemy
				values.put(new ImmutablePair<Material, Short>(material, (short) -1), Double.MAX_VALUE);
			default:
				break;
			}
		}
		return values;
	}

	private static void addRecipeCosts(Material material) {
		addRecipeCosts(material, (short) -1, new ArrayList<>());
	}

	private static double addRecipeCosts(Material material, short durability, List<Pair<Material, Short>> pastMaterials) {
		Pair<Material, Short> key = new ImmutablePair<>(material, durability);
		// Check if calculated already
		if (manaMappings.containsKey(key)) {
			return manaMappings.get(key);
		}
		// If a specific data value is provided but not found and a nonspecific value is present, fall through
		// TODO test, may not be desirable
		if (durability != -1) {
			Pair<Material, Short> anyKey = new ImmutablePair<>(material, (short) -1);
			if (!pastMaterials.contains(anyKey) && manaMappings.containsKey(anyKey)) {
				return manaMappings.get(anyKey);
			}
		}

		// Check if mid-calculation
		if (pastMaterials.contains(key)) {
			return Double.MAX_VALUE;
		}

		// Create a new list for sub-elements
		pastMaterials = new ArrayList<>(pastMaterials);
		// Add to mid-calc list
		pastMaterials.add(key);

		if (durability == -1) {
			double maximum = 0;
			Set<Short> durabilities = Bukkit.getRecipesFor(new ItemStack(material, 1, durability)).stream()
					.map(recipe -> recipe.getResult().getDurability()).collect(Collectors.toSet());
			for (short dura : durabilities) {
				if (dura != -1) {
					maximum = Math.max(maximum, addRecipeCosts(material, dura, pastMaterials));
				}
			}
			if (maximum <= 0) {
				// No recipes
				maximum = Double.MAX_VALUE;
			}
			manaMappings.put(key, maximum);
			return maximum;
		}

		double minimum = Double.MAX_VALUE;

		nextRecipe: for (Recipe recipe : Bukkit.getRecipesFor(new ItemStack(material, 1, durability))) {
			ItemStack result = recipe.getResult();
			int amount = result.getAmount();
			if (amount < 1) {
				continue nextRecipe;
			}

			double newMinimum;

			if (recipe instanceof FurnaceRecipe) {
				ItemStack input = ((FurnaceRecipe) recipe).getInput();
				if (pastMaterials.contains(new ImmutablePair<>(input.getType(), input.getDurability()))) {
					continue nextRecipe;
				}
				newMinimum = addRecipeCosts(input.getType(), input.getDurability(), pastMaterials);
				if (newMinimum >= Double.MAX_VALUE - 1.5) {
					continue nextRecipe;
				}
				// Coal is 12, 8 smelts per coal = 1.5 cost per smelt. Hardcoded to prevent a bit of extra code and checks.
				newMinimum += 1.5;
			} else if (recipe instanceof ShapedRecipe) {
				newMinimum = 0;
				ShapedRecipe shaped = (ShapedRecipe) recipe;
				HashMap<Character, Integer> materialQuantity = new HashMap<>();
				for (String line : shaped.getShape()) {
					for (char ingredient : line.toCharArray()) {
						if (materialQuantity.containsKey(ingredient)) {
							materialQuantity.put(ingredient, materialQuantity.get(ingredient) + 1);
						} else {
							materialQuantity.put(ingredient, 1);
						}
					}
				}
				for (Entry<Character, Integer> entry : materialQuantity.entrySet()) {
					ItemStack input = shaped.getIngredientMap().get(entry.getKey());
					if (input == null || input.getType() == Material.AIR || entry.getValue() < 1) {
						continue;
					}
					if (pastMaterials.contains(new ImmutablePair<>(input.getType(), input.getDurability()))) {
						// No loops.
						continue nextRecipe;
					}
					double inputValue = addRecipeCosts(input.getType(), input.getDurability(), pastMaterials);
					if (Double.MAX_VALUE / entry.getValue() <= inputValue) {
						// Input item cannot be duplicated.
						continue nextRecipe;
					}
					inputValue *= entry.getValue();
					if (Double.MAX_VALUE - newMinimum <= inputValue) {
						// Noverflow.
						continue nextRecipe;
					}
					newMinimum += inputValue;
					// Special case: Buckets are not consumed when crafting
					if (input.getType().name().endsWith("_BUCKET")) {
						// Iron ingots are 41, hardcoded bucket value here to avoid a lot of extra code to prevent issues
						newMinimum -= 123 * entry.getValue();
					}
				}
				if (newMinimum <= 0) {
					continue nextRecipe;
				}
			} else if (recipe instanceof ShapelessRecipe) {
				newMinimum = 0;
				for (ItemStack input : ((ShapelessRecipe) recipe).getIngredientList()) {
					if (input == null || input.getType() == Material.AIR) {
						continue;
					}
					if (input.getAmount() < 1 || pastMaterials.contains(new ImmutablePair<>(input.getType(), input.getDurability()))) {
						// No loops, no weird stuff.
						continue nextRecipe;
					}
					double inputValue = addRecipeCosts(input.getType(), input.getDurability(), pastMaterials);
					if (Double.MAX_VALUE / input.getAmount() <= inputValue) {
						// Input item cannot be duplicated.
						continue nextRecipe;
					}
					inputValue *= input.getAmount();
					if (Double.MAX_VALUE - newMinimum <= inputValue) {
						// Noverflow.
						continue nextRecipe;
					}
					newMinimum += inputValue;
					// Special case: Buckets are not consumed when crafting
					if (input.getType().name().endsWith("_BUCKET")) {
						// Iron ingots are 41, hardcoded bucket value here to avoid a lot of extra code to prevent issues
						newMinimum -= 123 * input.getAmount();
					}
				}
				if (newMinimum <= 0) {
					continue nextRecipe;
				}
			} else {
				// Recipe is injected custom recipe
				continue nextRecipe;
			}

			if (newMinimum == Double.MAX_VALUE) {
				continue nextRecipe;
			}

			newMinimum /= amount;
			if (newMinimum < minimum) {
				minimum = newMinimum;
			}
		}

		// No value = no make.
		if (minimum < 1) {
			minimum = Double.MAX_VALUE;
		}

		// Map and return.
		manaMappings.put(key, minimum);
		return minimum;
	}

	private static int getWeight(Enchantment enchantment) {
		if (enchantment.equals(Enchantment.PROTECTION_ENVIRONMENTAL)
				|| enchantment.equals(Enchantment.DAMAGE_ALL)
				|| enchantment.equals(Enchantment.DIG_SPEED)
				|| enchantment.equals(Enchantment.ARROW_DAMAGE)) {
			return 10;
		}
		if (enchantment.equals(Enchantment.WATER_WORKER)
				|| enchantment.equals(Enchantment.PROTECTION_EXPLOSIONS)
				|| enchantment.equals(Enchantment.OXYGEN)
				|| enchantment.equals(Enchantment.DEPTH_STRIDER)
				|| enchantment.equals(Enchantment.FROST_WALKER)
				|| enchantment.equals(Enchantment.FIRE_ASPECT)
				|| enchantment.equals(Enchantment.LOOT_BONUS_MOBS)
				|| enchantment.equals(Enchantment.SWEEPING_EDGE)
				|| enchantment.equals(Enchantment.LOOT_BONUS_BLOCKS)
				|| enchantment.equals(Enchantment.ARROW_FIRE)
				|| enchantment.equals(Enchantment.ARROW_KNOCKBACK)
				|| enchantment.equals(Enchantment.LUCK)
				|| enchantment.equals(Enchantment.LURE)
				|| enchantment.equals(Enchantment.MENDING)) {
			return 2;
		}
		if (enchantment.equals(Enchantment.THORNS)
				|| enchantment.equals(Enchantment.SILK_TOUCH)
				|| enchantment.equals(Enchantment.ARROW_INFINITE)
				|| enchantment.equals(Enchantment.BINDING_CURSE)
				|| enchantment.equals(Enchantment.VANISHING_CURSE)) {
			return 1;
		}
		return 5;
	}

}
