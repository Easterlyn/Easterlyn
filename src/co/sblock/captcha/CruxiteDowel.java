package co.sblock.captcha;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.effects.Effects;
import co.sblock.effects.effect.Effect;

import net.md_5.bungee.api.ChatColor;

/**
 * A class for handling all functions of cruxite dowels.
 * 
 * @author Jikoo
 */
public class CruxiteDowel {

	private static HashMap<Material, Integer> grist;
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
		ItemMeta im = dowel.getItemMeta();
		im.setLore(is.getItemMeta().getLore());
		dowel.setItemMeta(im);
		return dowel;
	}

	public static int expCost(Effects effects, ItemStack toCreate) {
		int cost = getGrist().get(toCreate.getType());
		if (Captcha.isCaptcha(toCreate)) {
			cost = Integer.MAX_VALUE;
		}
		if (cost == Integer.MAX_VALUE) {
			// Item cannot be made with grist, we're done here.
			return cost;
		}

		ItemMeta meta = toCreate.getItemMeta();

		if (meta.hasEnchants()) {
			for (Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
				// Note: All level values are from 1.7
				// (16 - weight) * level * 20 seems to be a good rate.
				// Sharpness 5: 6 * 5 * 20 = 300 exp, 0-26
				// silk touch 1: 15 * 1 * 20 = 300 exp
				// fortune 3: 14 * 3 * 20 = 840 exp, 0-30
				// Rebalanced to *40, removed 2x multiplier from final cost
				cost += (20 - getWeight(entry.getKey())) * Math.abs(entry.getValue()) * 45;
			}
			if (toCreate.getType().getMaxDurability() == 0) {
				cost *= 4;
			}
		}

		if (meta instanceof EnchantmentStorageMeta) {
			for (Entry<Enchantment, Integer> entry : ((EnchantmentStorageMeta) meta).getStoredEnchants().entrySet()) {
				cost += (16 - getWeight(entry.getKey())) * entry.getValue() * 40;
			}
		}


		if (toCreate.getItemMeta().hasDisplayName()) {
			// Naming an unenchanted item in an anvil costs 1 additional level in 1.8
			cost += 15;
		}

		int effectCost = 0;
		for (Entry<Effect, Integer> effect : effects.getEffects(false, toCreate).entrySet()) {
			effectCost += effect.getKey().getCost() * effect.getValue();
		}
		// if item contains special lore and doesn't need repair, raise price
		if (toCreate.getType().getMaxDurability() > 0) {
			effectCost *= 4;
		}
		cost += effectCost;

		cost *= toCreate.getAmount();

		return cost;
	}

	public static HashMap<Material, Integer> getGrist() {
		if (grist == null) {
			grist = createBaseGrist();
			fillFromRecipes();
		}
		return grist;
	}

	private static HashMap<Material, Integer> createBaseGrist() {
		HashMap<Material, Integer> materialValues = new HashMap<>();

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
				materialValues.put(material, 1);
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
				materialValues.put(material, 2);
				break;
			case BROWN_MUSHROOM:
			case NETHERRACK:
			case HUGE_MUSHROOM_1:
			case HUGE_MUSHROOM_2:
			case POTATO_ITEM:
			case ROTTEN_FLESH:
			case STONE:
				materialValues.put(material, 3);
				break;
			case ARROW:
			case DOUBLE_PLANT:
			case FEATHER:
			case RAW_CHICKEN:
				materialValues.put(material, 4);
				break;
			case CLAY_BRICK:
			case FLINT:
			case RABBIT:
			case RAW_FISH:
			case WOOL:
				materialValues.put(material, 5);
				break;
			case BAKED_POTATO:
			case EGG:
			case NETHER_BRICK:
			case PUMPKIN:
				materialValues.put(material, 6);
				break;
			case COOKED_CHICKEN:
			case LOG:
			case LOG_2:
			case MUTTON:
			case RAW_BEEF:
			case REDSTONE:
			case STRING:
				materialValues.put(material, 8);
				break;
			case COOKED_FISH:
			case NETHER_WARTS:
			case NETHER_STALK: // Same thing as warts in 1.8 inventories
			case PRISMARINE_SHARD:
				materialValues.put(material, 9);
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
				materialValues.put(material, 10);
				break;
			case APPLE:
			case BONE:
			case COAL:
			case COOKED_BEEF:
			case GOLD_NUGGET:
			case RABBIT_FOOT:
			case SPIDER_EYE:
				materialValues.put(material, 12);
				break;
			case STAINED_CLAY:
				materialValues.put(material, 13);
				break;
			case GRILLED_PORK:
				materialValues.put(material, 14);
				break;
			case SAPLING:
			case SADDLE:
				materialValues.put(material, 16);
				break;
			case SULPHUR:
			case MAP: // Not crafted, right click
			case MYCEL:
				materialValues.put(material, 20);
				break;
			case ENCHANTED_BOOK:
				materialValues.put(material, 25);
				break;
			case PACKED_ICE:
				materialValues.put(material, 28);
				break;
			case BLAZE_ROD:
			case GRASS:
				materialValues.put(material, 30);
				break;
			case BANNER:
			case GHAST_TEAR:
			case PRISMARINE_CRYSTALS:
				materialValues.put(material, 35);
				break;
			case QUARTZ:
				materialValues.put(material, 37);
				break;
			case IRON_INGOT:
				materialValues.put(material, 41);
				break;
			case COAL_ORE:
			case QUARTZ_ORE:
				materialValues.put(material, 44);
				break;
			case GOLD_RECORD:
			case GREEN_RECORD:
			case IRON_ORE:
			case POTION: // Flat high price to account for regen etc.
				materialValues.put(material, 50);
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
				materialValues.put(material, 70);
				break;
			case PISTON_BASE:
			case OBSIDIAN:
			case REDSTONE_ORE:
				materialValues.put(material, 81);
				break;
			case ENDER_PEARL:
			case PISTON_STICKY_BASE:
				materialValues.put(material, 90);
				break;
			case GOLD_INGOT:
				materialValues.put(material, 108);
				break;
			case GOLD_ORE:
			case LAVA_BUCKET:
			case MILK_BUCKET:
			case WATER_BUCKET:
			case WEB:
				materialValues.put(material, 138);
				break;
			case DIAMOND:
				materialValues.put(material, 167);
				break;
			case DIAMOND_ORE:
				materialValues.put(material, 187);
				break;
			case IRON_BARDING:
				materialValues.put(material, 261);
				break;
			case NAME_TAG:
				materialValues.put(material, 405);
				break;
			case CHAINMAIL_BOOTS:
				materialValues.put(material, 600);
				break;
			case GOLD_BARDING:
				materialValues.put(material, 663);
				break;
			case CHAINMAIL_HELMET:
				materialValues.put(material, 750);
				break;
			case DIAMOND_BARDING:
				materialValues.put(material, 1000);
				break;
			case CHAINMAIL_LEGGINGS:
				materialValues.put(material, 1050);
				break;
			case CHAINMAIL_CHESTPLATE:
				materialValues.put(material, 1200);
				break;
			case SKULL_ITEM:
				materialValues.put(material, 3000);
			case NETHER_STAR:
				materialValues.put(material, 10000);
			case DRAGON_EGG:
				materialValues.put(material, 32000);
				break;
			// Unobtainable, don't bother searching recipes
			case AIR:
			case BARRIER:
			case BEDROCK:
			case INK_SACK: // Lapis is a dye
			case BOOK_AND_QUILL:
			case BURNING_FURNACE:
			case CARROT: // plant
			case CAKE_BLOCK: // No infinite food
			case COCOA: // plant
			case COMMAND:
			case COMMAND_MINECART:
			case CROPS: // plant
			case EMERALD: // Money
			case EMERALD_BLOCK: // Money
			case EMERALD_ORE: // Can't be captcha'd
			case ENDER_PORTAL:
			case ENDER_PORTAL_FRAME:
			case EXP_BOTTLE: // Can't be captcha'd
			case FIRE:
			case GLOWING_REDSTONE_ORE:
			case LAPIS_BLOCK: // Money
			case LAPIS_ORE: // Can't be captcha'd
			case LAVA:
			case MELON_STEM: // plant
			case MOB_SPAWNER:
			case MONSTER_EGG:
			case MONSTER_EGGS:
			case PISTON_EXTENSION:
			case PISTON_MOVING_PIECE:
			case PORTAL:
			case POTATO: // plant
			case PUMPKIN_STEM: // plant
			case SKULL:
			case SNOW:
			case SOIL:
			case SPONGE:
			case STATIONARY_LAVA:
			case STATIONARY_WATER:
			case WATER:
			case WRITTEN_BOOK: // Can't be captcha'd
				materialValues.put(material, Integer.MAX_VALUE);
			default:
				break;
			}
		}
		return materialValues;
	}

	private static void fillFromRecipes() {
		for (Material material : Material.values()) {
			grist.put(material, getRecipeCost(material));
		}
	}

	private static int getRecipeCost(Material material) {
		if (grist.containsKey(material)) {
			return grist.get(material);
		}
		int minimum = Integer.MAX_VALUE;
		for (Recipe r : Bukkit.getRecipesFor(new ItemStack(material))) {
			int amount = r.getResult().getAmount();
			if (amount < 1) {
				continue;
			}
			int newMin;
			if (r instanceof FurnaceRecipe) {
				newMin = 2 + getRecipeCost(((FurnaceRecipe) r).getInput().getType());
			} else if (r instanceof ShapedRecipe) {
				newMin = 0;
				HashMap<Character, Integer> materialQuantity = new HashMap<>();
				for (String s : ((ShapedRecipe) r).getShape()) {
					for (char c : s.toCharArray()) {
						if (materialQuantity.containsKey(c)) {
							materialQuantity.put(c, materialQuantity.get(c) + 1);
						} else {
							materialQuantity.put(c, 1);
						}
					}
				}
				for (Entry<Character, Integer> e : materialQuantity.entrySet()) {
					ItemStack is = ((ShapedRecipe) r).getIngredientMap().get(e.getKey());
					if (is == null) {
						continue;
					}
					if (is.getType() == r.getResult().getType()) {
						newMin = Integer.MAX_VALUE;
						break;
					}
					newMin += getRecipeCost(is.getType()) * e.getValue();
				}
			} else  if (r instanceof ShapelessRecipe) {
				newMin = 0;
				for (ItemStack is : ((ShapelessRecipe) r).getIngredientList()) {
					if (is.getType() == r.getResult().getType()) {
						newMin = Integer.MAX_VALUE;
						break;
					}
					newMin += getRecipeCost(is.getType());
				}
			} else {
				// Recipe is injected custom recipe
				continue;
			}
			if (newMin == Integer.MAX_VALUE) {
				continue;
			}
			newMin /= amount;
			if (newMin < minimum) {
				minimum = newMin;
			}
		}
		return minimum < 1 ? 1 : minimum;
	}

	public static int getWeight(Enchantment enchantment) {
		if (enchantment.equals(Enchantment.PROTECTION_ENVIRONMENTAL) || enchantment.equals(Enchantment.DAMAGE_ALL)
				|| enchantment.equals(Enchantment.DIG_SPEED) || enchantment.equals(Enchantment.ARROW_DAMAGE)) {
			return 10;
		}
		if (enchantment.equals(Enchantment.WATER_WORKER) || enchantment.equals(Enchantment.PROTECTION_EXPLOSIONS)
				|| enchantment.equals(Enchantment.OXYGEN) || enchantment.equals(Enchantment.FIRE_ASPECT)
				|| enchantment.equals(Enchantment.LOOT_BONUS_MOBS) || enchantment.equals(Enchantment.LOOT_BONUS_BLOCKS)
				|| enchantment.equals(Enchantment.ARROW_FIRE) || enchantment.equals(Enchantment.ARROW_KNOCKBACK)
				|| enchantment.equals(Enchantment.DEPTH_STRIDER)) {
			return 2;
		}
		if (enchantment.equals(Enchantment.THORNS) || enchantment.equals(Enchantment.SILK_TOUCH)
				|| enchantment.equals(Enchantment.ARROW_INFINITE)) {
			return 1;
		}
		return 5;
	}
}
