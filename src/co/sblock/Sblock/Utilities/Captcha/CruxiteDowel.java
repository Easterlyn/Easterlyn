package co.sblock.Sblock.Utilities.Captcha;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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
		if (is != null) {
			ItemStack dowel = getDowel();
			dowel.setAmount(is.getAmount());
			return is.equals(dowel);
		}
		return false;
	}

	public static boolean isUsedDowel(ItemStack is) {
		return is != null && is.getType() == Material.NETHER_BRICK_ITEM && is.hasItemMeta()
				&& is.getItemMeta().hasDisplayName() && is.getItemMeta().hasLore()
				&& is.getItemMeta().getDisplayName().equals(ChatColor.WHITE + "Cruxite Dowel");
	}

	public static ItemStack carve(ItemStack is) {
		ItemStack dowel = getDowel();
		ItemMeta im = dowel.getItemMeta();
		im.setLore(is.getItemMeta().getLore());
		dowel.setItemMeta(im);
		return dowel;
	}

	public static long expCost(ItemStack dowel) {
		// if hashmap contains materialdata
		// fetch
		// if item contains special lore and !repairable, raise price
		// if repairable, get % and repair cost
		return 0; // TODO
	}

	public static HashMap<String, Integer> getGrist() {
		if (grist == null) {
			grist = createGrist();
		}
		return grist;
	}

	private static HashMap<String, Integer> createGrist() {
		for (Material m : Material.values()) {
			switch(m) {
			case AIR:
				break;
			case APPLE:
				break;
			case ARROW:
				break;
			case BAKED_POTATO:
				break;
			case BEDROCK:
				break;
			case BLAZE_ROD:
				break;
			case BONE:
				break;
			case BRICK:
				break;
			case BROWN_MUSHROOM:
				break;
			case BURNING_FURNACE:
				break;
			case CACTUS:
				break;
			case CARROT:
				break;
			case CARROT_ITEM:
				break;
			case CHAINMAIL_BOOTS:
				break;
			case CHAINMAIL_CHESTPLATE:
				break;
			case CHAINMAIL_HELMET:
				break;
			case CHAINMAIL_LEGGINGS:
				break;
			case CLAY_BALL:
				break;
			case CLAY_BRICK:
				break;
			case COAL:
				break;
			case COAL_ORE:
				break;
			case COBBLESTONE:
				break;
			case COCOA:
				break;
			case COMMAND:
				break;
			case COMMAND_MINECART:
				break;
			case COOKED_BEEF:
				break;
			case COOKED_CHICKEN:
				break;
			case COOKED_FISH:
				break;
			case CROPS:
				break;
			case DEAD_BUSH:
				break;
			case DIAMOND:
				break;
			case DIAMOND_BARDING:
				break;
			case DIAMOND_ORE:
				break;
			case DIRT:
				break;
			case DOUBLE_PLANT:
				break;
			case DRAGON_EGG:
				break;
			case EGG:
				break;
			case EMERALD:
				break;
			case EMERALD_ORE:
				break;
			case ENCHANTED_BOOK:
				break;
			case ENDER_PEARL:
				break;
			case ENDER_PORTAL:
				break;
			case ENDER_PORTAL_FRAME:
				break;
			case ENDER_STONE:
				break;
			case EXP_BOTTLE:
				break;
			case FEATHER:
				break;
			case FIRE:
				break;
			case FLINT:
				break;
			case GHAST_TEAR:
				break;
			case GLASS:
				break;
			case GLOWING_REDSTONE_ORE:
				break;
			case GLOWSTONE_DUST:
				break;
			case GOLD_BARDING:
				break;
			case GOLD_INGOT:
				break;
			case GOLD_ORE:
				break;
			case GOLD_RECORD:
				break;
			case GRASS:
				break;
			case GRAVEL:
				break;
			case GREEN_RECORD:
				break;
			case GRILLED_PORK:
				break;
			case HUGE_MUSHROOM_1:
				break;
			case HUGE_MUSHROOM_2:
				break;
			case ICE:
				break;
			case INK_SACK:
				break;
			case IRON_BARDING:
				break;
			case IRON_INGOT:
				break;
			case IRON_ORE:
				break;
			case LAPIS_ORE:
				break;
			case LAVA:
				break;
			case LAVA_BUCKET:
				break;
			case LEATHER:
				break;
			case LEAVES:
			case LEAVES_2:
				break;
			case LOG:
			case LOG_2:
				break;
			case LONG_GRASS:
				break;
			case MAP: // Not crafted, right click
				break;
			case MELON:
				break;
			case MELON_STEM:
				break;
			case MILK_BUCKET:
				break;
			case MOB_SPAWNER:
				break;
			case MONSTER_EGG:
				break;
			case MONSTER_EGGS:
				break;
			case MOSSY_COBBLESTONE:
				break;
			case MYCEL:
				break;
			case NAME_TAG:
				break;
			case NETHERRACK:
				break;
			case NETHER_BRICK:
				break;
			case NETHER_BRICK_ITEM:
				break;
			case NETHER_STALK:
				break;
			case NETHER_STAR:
				break;
			case NETHER_WARTS:
				break;
			case OBSIDIAN:
				break;
			case PACKED_ICE:
				break;
			case PISTON_BASE:
				break;
			case PISTON_EXTENSION:
				break;
			case PISTON_MOVING_PIECE:
				break;
			case PISTON_STICKY_BASE:
				break;
			case POISONOUS_POTATO:
				break;
			case PORK:
				break;
			case PORTAL:
				break;
			case POTATO:
				break;
			case POTATO_ITEM:
				break;
			case POTION:
				break;
			case PUMPKIN:
				break;
			case PUMPKIN_STEM:
				break;
			case QUARTZ:
				break;
			case QUARTZ_ORE:
				break;
			case RAW_BEEF:
				break;
			case RAW_CHICKEN:
				break;
			case RAW_FISH:
				break;
			case RECORD_10:
				break;
			case RECORD_11:
				break;
			case RECORD_12:
				break;
			case RECORD_3:
				break;
			case RECORD_4:
				break;
			case RECORD_5:
				break;
			case RECORD_6:
				break;
			case RECORD_7:
				break;
			case RECORD_8:
				break;
			case RECORD_9:
				break;
			case REDSTONE:
				break;
			case REDSTONE_ORE:
				break;
			case RED_MUSHROOM:
				break;
			case RED_ROSE:
				break;
			case ROTTEN_FLESH:
				break;
			case SADDLE:
				break;
			case SAND:
				break;
			case SAPLING:
				break;
			case SEEDS:
				break;
			case SKULL:
				break;
			case SKULL_ITEM:
				break;
			case SLIME_BALL:
				break;
			case SNOW:
				break;
			case SNOW_BALL:
				break;
			case SOIL:
				break;
			case SOUL_SAND:
				break;
			case SPIDER_EYE:
				break;
			case SPONGE:
				break;
			case STATIONARY_LAVA:
				break;
			case STATIONARY_WATER:
				break;
			case STONE:
				break;
			case STRING:
				break;
			case SUGAR_CANE:
				break;
			case SULPHUR:
				break;
			case VINE:
				break;
			case WATER:
				break;
			case WATER_BUCKET:
				break;
			case WATER_LILY:
				break;
			case WEB:
				break;
			case WHEAT:
				break;
			case WRITTEN_BOOK:
				break;
			case YELLOW_FLOWER:
				break;
			default:
				break;
			
			}
		}
		return grist;
	}
}
