package co.sblock.Sblock.Utilities.Captcha;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

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
				&& is.getItemMeta().getDisplayName().equals(ChatColor.WHITE + "Cruxite Dowel");
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

	public static int expCost(ItemStack toCreate) {
		// if hashmap contains materialdata
		// fetch
		// if item contains special lore and !repairable, raise price
		// if repairable, get % and repair cost
		return 0; // TODO
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
			case RAW_FISH:
				materialValues.put(m.name(), 5);
				break;
			case BAKED_POTATO:
			case EGG:
			case NETHER_BRICK:
			case STONE:
				materialValues.put(m.name(), 6);
				break;
			case COOKED_CHICKEN:
			case LOG:
			case LOG_2:
			case RAW_BEEF:
			case REDSTONE:
			case STRING:
				materialValues.put(m.name(), 8);
				break;
			case COOKED_FISH:
			case INK_SACK: // Dyes all == most expensive. No phr33 st00f.
			case NETHER_WARTS:
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
				materialValues.put(m.name(), 10);
				break;
			case APPLE:
			case BONE:
			case COAL:
			case COOKED_BEEF:
			case GOLD_NUGGET:
			case PUMPKIN:
			case SPIDER_EYE:
				materialValues.put(m.name(), 12);
				break;
			case GRILLED_PORK:
				materialValues.put(m.name(), 14);
				break;
			case SAPLING:
			case SADDLE:
				materialValues.put(m.name(), 16);
				break;
			case CHAINMAIL_BOOTS:
			case SULPHUR:
			case MAP: // Not crafted, right click
			case MYCEL:
				materialValues.put(m.name(), 20);
				break;
			case CHAINMAIL_HELMET:
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
			case CHAINMAIL_LEGGINGS:
			case GHAST_TEAR:
				materialValues.put(m.name(), 35);
				break;
			case IRON_ORE:
			case QUARTZ:
				materialValues.put(m.name(), 37);
				break;
			case CHAINMAIL_CHESTPLATE:
				materialValues.put(m.name(), 40);
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
			case EMERALD:
			case REDSTONE_ORE:
				materialValues.put(m.name(), 81);
				break;
			case ENDER_PEARL:
			case PISTON_STICKY_BASE:
				materialValues.put(m.name(), 90);
				break;
			case GOLD_ORE:
				materialValues.put(m.name(), 104);
				break;
			case LAVA_BUCKET:
			case MILK_BUCKET:
			case WATER_BUCKET:
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
			case GOLD_BARDING:
				materialValues.put(m.name(), 663);
				break;
			case DIAMOND_BARDING:
			case DRAGON_EGG:
			case NETHER_STAR:
				materialValues.put(m.name(), 1000);
				break;
			// UNOBTAINABLE
			case AIR:
			case BEDROCK:
			case BURNING_FURNACE:
			case CARROT: // plant
			case COCOA: // plant
			case COMMAND:
			case COMMAND_MINECART:
			case CROPS: // plant
			case EMERALD_ORE: // Can't be captcha'd
			case ENDER_PORTAL:
			case ENDER_PORTAL_FRAME:
			case EXP_BOTTLE: // Can't be captcha'd
			case FIRE:
			case GLOWING_REDSTONE_ORE:
			case LAPIS_ORE: // Can't be captcha'd
			case LAVA:
			case MELON_STEM: // plant
			case MOB_SPAWNER:
			case MONSTER_EGG:
			case MONSTER_EGGS:
			case NETHER_STALK: // plant
			case PISTON_EXTENSION:
			case PISTON_MOVING_PIECE:
			case PORTAL:
			case POTATO: // plant
			case POTION: // Can't be captcha'd
			case PUMPKIN_STEM: // plant
			case SKULL:
			case SKULL_ITEM: // Can't be captcha'd
			case SNOW:
			case SOIL:
			case SPONGE:
			case STATIONARY_LAVA:
			case STATIONARY_WATER:
			case WATER:
			case WEB:
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
			return getGrist().get(m.name());
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
					newMin += getRecipeCost(((ShapedRecipe) r).getIngredientMap().get(e.getKey()).getType()) * e.getValue();
				}
			} else {
				newMin = 0;
				for (ItemStack is : ((ShapelessRecipe) r).getIngredientList()) {
					newMin += getRecipeCost(is.getType());
				}
			}
			if (newMin < minimum) {
				minimum = newMin;
			}
		}
		return minimum;
	}
}
