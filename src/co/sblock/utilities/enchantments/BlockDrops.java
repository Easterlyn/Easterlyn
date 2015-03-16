package co.sblock.utilities.enchantments;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

/**
 * Utility for getting accurate drops from a block - Block.getDrops(ItemStack) does not take into
 * account enchantments.
 * 
 * @author Jikoo
 */
public class BlockDrops {

	public static Collection<ItemStack> getDrops(ItemStack tool, Block block) {
		return getDrops(tool, block, 0);
	}

	public static Collection<ItemStack> getDrops(ItemStack tool, Block block, int fortuneBonus) {
		if (tool.containsEnchantment(Enchantment.SILK_TOUCH) && tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0) {
			Collection<ItemStack> drops = getSilkDrops(tool, block.getState().getData());
			if (drops != null) {
				return drops;
			}
		}
		if (fortuneBonus > 0 || tool.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)
				&& tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + fortuneBonus > 0) {
			Collection<ItemStack> drops = getFortuneDrops(tool, block.getState().getData(), fortuneBonus);
			if (drops != null) {
				return drops;
			}
		}
		return block.getDrops(tool);
	}

	public static int getExp(ItemStack tool, Block block) {
		// TODO exp
		return 0;
	}

	@SuppressWarnings("deprecation")
	private static Collection<ItemStack> getSilkDrops(ItemStack tool, MaterialData material) {
		ArrayList<ItemStack> drops = new ArrayList<>();
		switch (material.getItemType()) {
		case COAL_ORE:
		case DIAMOND_ORE:
		case EMERALD_ORE:
		case ENDER_CHEST:
		case LAPIS_ORE:
		case QUARTZ_ORE:
		case REDSTONE_ORE:
		case SNOW_BLOCK:
		case STONE:
		case WEB:
			if (canMine(tool.getType(), material.getItemType())) {
				drops.add(new ItemStack(material.getItemType()));
			}
			return drops;
		case GLOWING_REDSTONE_ORE:
			if (canMine(tool.getType(), Material.GLOWING_REDSTONE_ORE)) {
				drops.add(new ItemStack(Material.REDSTONE_ORE));
			}
			return drops;
		case BOOKSHELF:
		case CLAY:
		case GLASS:
		case GLOWSTONE:
		case GRASS:
		case GRAVEL:
		case HUGE_MUSHROOM_1:
		case HUGE_MUSHROOM_2:
		case ICE:
		case MELON_BLOCK:
		case MYCEL:
		case PACKED_ICE:
		case SEA_LANTERN:
		case STAINED_GLASS:
		case STAINED_GLASS_PANE:
		case THIN_GLASS:
			drops.add(new ItemStack(material.getItemType(), 1, material.getData()));
			return drops;
		case LEAVES:
		case LEAVES_2:
			drops.add(new ItemStack(material.getItemType(), 1, (short) (material.getData() % 4)));
			return drops;
		default:
			return null;
		}
	}

	private static boolean canMine(Material tool, Material block) {
		switch (block) {
		case STONE:
		case COAL_ORE:
		case QUARTZ_ORE:
			if (tool == Material.WOOD_PICKAXE || tool == Material.GOLD_PICKAXE) {
				return true;
			}
		case LAPIS_ORE:
			if (tool == Material.STONE_PICKAXE) {
				return true;
			}
		case DIAMOND_ORE:
		case EMERALD_ORE:
		case ENDER_CHEST:
		case GLOWING_REDSTONE_ORE:
		case REDSTONE_ORE:
			return tool == Material.IRON_PICKAXE || tool == Material.DIAMOND_PICKAXE;
		case WEB:
			return tool == Material.SHEARS;
		case SNOW_BLOCK:
			return tool == Material.DIAMOND_SPADE || tool == Material.IRON_SPADE
					|| tool == Material.GOLD_SPADE || tool == Material.WOOD_SPADE;
		default:
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	private static Collection<ItemStack> getFortuneDrops(ItemStack tool, MaterialData material, int fortuneBonus) {
		int fortune = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + fortuneBonus;
		ArrayList<ItemStack> drops = new ArrayList<>();
		switch (material.getItemType()) {
		case CARROT:
			drops.add(doFortune(Material.CARROT_ITEM, 1, 4, fortune, false));
			break;
		case COAL_ORE:
			drops.add(doFortune(Material.COAL, 1, 1, fortune, true));
			break;
		case CROPS:
			drops.add(new ItemStack(Material.WHEAT));
			ItemStack seeds = doFortune(Material.SEEDS, 0, 3, fortune, false);
			if (seeds.getAmount() > 0) {
				drops.add(seeds);
			}
			break;
		case DIAMOND_ORE:
			drops.add(doFortune(Material.DIAMOND, 1, 1, fortune, true));
			break;
		case EMERALD_ORE:
			drops.add(doFortune(Material.EMERALD, 1, 1, fortune, true));
			break;
		case GLOWING_REDSTONE_ORE:
		case REDSTONE_ORE:
			// Only ore that is supposed to use addition-style fortune.
			drops.add(doFortune(Material.REDSTONE, 1, 1, fortune, false));
			break;
		case GLOWSTONE:
			ItemStack dust = doFortune(Material.GLOWSTONE_DUST, 1, 4, fortune, false);
			if (dust.getAmount() > 4) {
				dust.setAmount(4);
			}
			drops.add(dust);
			break;
		case GRAVEL:
			boolean flint = fortune > 2 || Math.random() < (fortune == 1 ? .14 : .25);
			if (flint) {
				drops.add(new ItemStack(Material.FLINT));
			} else {
				drops.add(new ItemStack(Material.GRAVEL));
			}
			break;
		case LAPIS_ORE:
			ItemStack lapis = doFortune(Material.INK_SACK, 4, 8, fortune, true);
			lapis.setDurability(DyeColor.BLUE.getDyeData());
			drops.add(lapis);
			break;
		case LEAVES:
		case LEAVES_2:
			int treeType = material.getData() % 4;
			if (material.getItemType() == Material.LEAVES_2) {
				treeType += 4;
			}
			if ((treeType == 0 || treeType == 5) && Math.random() < 1 / (200 - (20 * fortune))) {
				// Oak and dark oak drop apples
				drops.add(new ItemStack(Material.APPLE));
			}
			double dropRate = treeType == 3 ? fortune < 1 ? .025 : fortune == 1 ? .0278 : fortune == 2 ? .03125 : .0417
					: fortune < 1 ? .06 : fortune == 1 ? .0625 : fortune == 2 ? .0833 : .01;
			if (Math.random() < dropRate) {
				drops.add(new ItemStack(Material.SAPLING, 1, (short) treeType));
			}
			break;
		case MELON_BLOCK:
			ItemStack melon = doFortune(Material.MELON, 1, 9, fortune, false);
			if (melon.getAmount() > 9) {
				melon.setAmount(9);
			}
			drops.add(melon);
			break;
		case NETHER_WARTS:
			drops.add(doFortune(Material.NETHER_STALK, 2, 4, fortune, false));
			break;
		case POTATO:
			drops.add(doFortune(Material.POTATO_ITEM, 1, 4, fortune, false));
			if (Math.random() < .02) {
				drops.add(new ItemStack(Material.POISONOUS_POTATO));
			}
			break;
		case QUARTZ_ORE:
			drops.add(doFortune(Material.QUARTZ, 1, 1, fortune, true));
			break;
		case SEA_LANTERN:
			ItemStack crystals = doFortune(Material.PRISMARINE_CRYSTALS, 1, 5, fortune, false);
			if (crystals.getAmount() > 5) {
				crystals.setAmount(5);
			}
			drops.add(crystals);
			break;
		default:
			return null;
		}
		return drops;
	}

	private static ItemStack doFortune(Material drop, int min, int max, int fortune, boolean multiply) {
		max -= (min - 1);
		int bonus = (int) (Math.random() * (fortune + 2));
		if (bonus < 1) {
			bonus = 1;
		}
		if (multiply) {
			return new ItemStack(drop, (int) ((Math.random() * max) + min) * bonus);
		}
		return new ItemStack(drop, (int) (Math.random() * max) + min + bonus - 1);
	}
}
