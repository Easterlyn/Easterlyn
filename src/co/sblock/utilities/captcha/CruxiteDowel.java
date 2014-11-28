package co.sblock.utilities.captcha;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import co.sblock.effects.ActiveEffect;
import co.sblock.effects.PassiveEffect;

/**
 * A class for handling all functions of cruxite dowels.
 * 
 * @author Jikoo
 */
public class CruxiteDowel {

	private static HashMap<String, Integer> grist;

	public static ItemStack getDowel() {
		ItemStack is = new ItemStack(Material.NETHER_BRICK_ITEM);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.WHITE + "Cruxite Dowel");
		is.setItemMeta(im);
		return is;
	}

	public static boolean isBlankDowel(ItemStack is) {
		return isDowel(is) && !is.getItemMeta().hasLore();
	}

	public static boolean isDowel(ItemStack is) {
		return is != null && is.getType() == Material.NETHER_BRICK_ITEM && is.hasItemMeta()
				&& is.getItemMeta().hasDisplayName()
				&& (is.getItemMeta().getDisplayName().equals(ChatColor.WHITE + "Cruxite Dowel")
				|| is.getItemMeta().getDisplayName().equals(ChatColor.WHITE + "Cruxite Totem"));
	}

	public static boolean isUsedDowel(ItemStack is) {
		return isDowel(is) && is.getItemMeta().hasLore();
	}

	public static ItemStack carve(ItemStack is) {
		ItemStack dowel = getDowel();
		ItemMeta im = dowel.getItemMeta();
		im.setLore(is.getItemMeta().getLore());
		im.setDisplayName(ChatColor.WHITE + "Cruxite Totem");
		dowel.setItemMeta(im);
		return dowel;
	}

	public static int expCost(ItemStack toCreate) {
		int cost = getGrist().get(toCreate.getType().name());
		if (Captcha.isCaptcha(toCreate)) {
			cost = Integer.MAX_VALUE;
		}
		if (cost == Integer.MAX_VALUE) {
			// Item cannot be made with grist, we're done here.
			return cost;
		}

		for (Entry<Enchantment, Integer> entry : toCreate.getEnchantments().entrySet()) {
			// (16 - weight) * level * 20 seems to be a good rate.
			// Sharpness 5: 6 * 5 * 20 = 300 exp, 0-26
			// silk touch 1: 15 * 1 * 20 = 300 exp
			// fortune 3: 14 * 3 * 20 = 840 exp, 0-30
			cost += (16 - getWeight(entry.getKey())) * entry.getValue() * 20;
		}

		if (toCreate.getItemMeta().hasDisplayName()) {
			// Naming an unenchanted item in an anvil costs 5 levels.
			// 0-5 is 85 exp.
			cost += 85;
		}


		if (toCreate.getItemMeta().hasLore()) {
			// if item contains special lore and !repairable, raise price
			boolean willNeedRepair = toCreate.getItemMeta() instanceof Repairable;

			for (String lore : toCreate.getItemMeta().getLore()) {
				int loreCost = 0;
				ActiveEffect active = ActiveEffect.getEffect(lore);
				if (active != null) {
					loreCost = active.getCost();
				} else {
					PassiveEffect passive = PassiveEffect.getEffect(lore);
					if (passive != null) {
						loreCost = passive.getCost();
					}
				}
				if (!willNeedRepair) {
					loreCost *= 1.5;
				}
				cost += loreCost;
			}
		}

		// 2 exp/boondollar seems reasonable.
		// Puts a stack of cobble at lvl 0-13, nether star at 0-42.
		cost *= 2 * toCreate.getAmount();

		return cost;
	}

	public static HashMap<String, Integer> getGrist() {
		if (grist == null) {
			grist = createBaseGrist();
			fillFromRecipes();
		}
		return grist;
	}

	private static HashMap<String, Integer> createBaseGrist() {
		HashMap<String, Integer> materialValues = new HashMap<>();

		for (Material m : Material.values()) {
			switch(m) {
			case CLAY_BALL:
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
				materialValues.put(m.name(), 1);
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
				materialValues.put(m.name(), 2);
				break;
			case BROWN_MUSHROOM:
			case NETHERRACK:
			case HUGE_MUSHROOM_1:
			case HUGE_MUSHROOM_2:
			case POTATO_ITEM:
			case ROTTEN_FLESH:
			case STONE:
				materialValues.put(m.name(), 3);
				break;
			case ARROW:
			case DOUBLE_PLANT:
			case FEATHER:
			case RAW_CHICKEN:
				materialValues.put(m.name(), 4);
				break;
			case CLAY_BRICK:
			case FLINT:
			case RABBIT:
			case RAW_FISH:
			case WOOL:
				materialValues.put(m.name(), 5);
				break;
			case BAKED_POTATO:
			case EGG:
			case NETHER_BRICK:
				materialValues.put(m.name(), 6);
				break;
			case COOKED_CHICKEN:
			case LOG:
			case LOG_2:
			case MUTTON:
			case RAW_BEEF:
			case REDSTONE:
			case STRING:
				materialValues.put(m.name(), 8);
				break;
			case COOKED_FISH:
			case NETHER_WARTS:
			case NETHER_STALK: // Same thing as warts in 1.8 inventories
			case PRISMARINE_SHARD:
				materialValues.put(m.name(), 9);
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
				materialValues.put(m.name(), 10);
				break;
			case APPLE:
			case BONE:
			case COAL:
			case COOKED_BEEF:
			case GOLD_NUGGET:
			case PUMPKIN:
			case RABBIT_FOOT:
			case SPIDER_EYE:
				materialValues.put(m.name(), 12);
				break;
			case STAINED_CLAY:
				materialValues.put(m.name(), 13);
				break;
			case GRILLED_PORK:
				materialValues.put(m.name(), 14);
				break;
			case SAPLING:
			case SADDLE:
				materialValues.put(m.name(), 16);
				break;
			case SULPHUR:
			case MAP: // Not crafted, right click
			case MYCEL:
				materialValues.put(m.name(), 20);
				break;
			case ENCHANTED_BOOK:
				materialValues.put(m.name(), 25);
				break;
			case PACKED_ICE:
				materialValues.put(m.name(), 28);
				break;
			case BLAZE_ROD:
			case GRASS:
				materialValues.put(m.name(), 30);
				break;
			case BANNER:
			case GHAST_TEAR:
			case PRISMARINE_CRYSTALS:
				materialValues.put(m.name(), 35);
				break;
			case QUARTZ:
				materialValues.put(m.name(), 37);
				break;
			case IRON_INGOT:
				materialValues.put(m.name(), 41);
				break;
			case COAL_ORE:
			case QUARTZ_ORE:
				materialValues.put(m.name(), 44);
				break;
			case GOLD_RECORD:
			case GREEN_RECORD:
			case IRON_ORE:
			case POTION: // Flat high price to account for regen etc.
				materialValues.put(m.name(), 50);
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
				materialValues.put(m.name(), 70);
				break;
			case PISTON_BASE:
			case OBSIDIAN:
			case REDSTONE_ORE:
				materialValues.put(m.name(), 81);
				break;
			case ENDER_PEARL:
			case PISTON_STICKY_BASE:
				materialValues.put(m.name(), 90);
				break;
			case GOLD_INGOT:
				materialValues.put(m.name(), 108);
				break;
			case GOLD_ORE:
			case LAVA_BUCKET:
			case MILK_BUCKET:
			case WATER_BUCKET:
			case WEB:
				materialValues.put(m.name(), 138);
				break;
			case DIAMOND:
				materialValues.put(m.name(), 167);
				break;
			case DIAMOND_ORE:
				materialValues.put(m.name(), 187);
				break;
			case IRON_BARDING:
				materialValues.put(m.name(), 261);
				break;
			case NAME_TAG:
				materialValues.put(m.name(), 405);
				break;
			case CHAINMAIL_BOOTS:
				materialValues.put(m.name(), 600);
				break;
			case GOLD_BARDING:
				materialValues.put(m.name(), 663);
				break;
			case CHAINMAIL_HELMET:
				materialValues.put(m.name(), 750);
				break;
			case DIAMOND_BARDING:
				materialValues.put(m.name(), 1000);
				break;
			case CHAINMAIL_LEGGINGS:
				materialValues.put(m.name(), 1050);
				break;
			case CHAINMAIL_CHESTPLATE:
				materialValues.put(m.name(), 1200);
				break;
			case DRAGON_EGG:
			case NETHER_STAR:
				materialValues.put(m.name(), 16000);
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
			case SKULL_ITEM: // Can't be captcha'd
			case SNOW:
			case SOIL:
			case SPONGE:
			case STATIONARY_LAVA:
			case STATIONARY_WATER:
			case WATER:
			case WRITTEN_BOOK: // Can't be captcha'd
				materialValues.put(m.name(), Integer.MAX_VALUE);
			default:
				break;
			}
		}
		return materialValues;
	}

	private static void fillFromRecipes() {
		for (Material m : Material.values()) {
			grist.put(m.name(), getRecipeCost(m));
		}
	}

	private static int getRecipeCost(Material m) {
		if (grist.containsKey(m.name())) {
			return grist.get(m.name());
		}
		int minimum = Integer.MAX_VALUE;
		for (Recipe r : Bukkit.getRecipesFor(new ItemStack(m))) {
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
					if (is != null) {
						newMin += getRecipeCost(is.getType()) * e.getValue();
					}
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
				// Recipe is injected custom recipe, e.g. banner pattern recipe
				newMin = Integer.MAX_VALUE;
			}
			if (newMin < minimum) {
				minimum = newMin;
			}
		}
		return minimum;
	}

	public static int getWeight(Enchantment e) {
		if (e == Enchantment.PROTECTION_ENVIRONMENTAL || e == Enchantment.DAMAGE_ALL
				|| e == Enchantment.DIG_SPEED || e == Enchantment.ARROW_DAMAGE) {
			return 10;
		}
		if (e == Enchantment.WATER_WORKER || e == Enchantment.PROTECTION_EXPLOSIONS
				|| e == Enchantment.OXYGEN || e == Enchantment.FIRE_ASPECT
				|| e == Enchantment.LOOT_BONUS_MOBS || e == Enchantment.LOOT_BONUS_BLOCKS
				|| e == Enchantment.ARROW_FIRE || e == Enchantment.ARROW_KNOCKBACK) {
			return 2;
		}
		if (e == Enchantment.THORNS || e == Enchantment.SILK_TOUCH
				|| e == Enchantment.ARROW_INFINITE) {
			return 1;
		}
		return 5;
	}
}
