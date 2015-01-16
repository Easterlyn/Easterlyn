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

	// TODO exp

	public static Collection<ItemStack> getDrops(ItemStack tool, Block block) {
		if (tool.containsEnchantment(Enchantment.SILK_TOUCH) && tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0) {
			Collection<ItemStack> drops = getSilkDrops(tool, block.getState().getData());
			if (drops != null) {
				return drops;
			}
		}
		if (tool.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS) && tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) > 0) {
			Collection<ItemStack> drops = getFortuneDrops(tool, block.getState().getData());
			if (drops != null) {
				return drops;
			}
		}
		return block.getDrops(tool);
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
			if (tool == Material.WOOD_PICKAXE) {
				return true;
			}
		case LAPIS_ORE:
			if (tool == Material.STONE_PICKAXE || tool == Material.GOLD_PICKAXE) {
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
	private static Collection<ItemStack> getFortuneDrops(ItemStack tool, MaterialData material) {
		int fortune = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
		ArrayList<ItemStack> drops = new ArrayList<>();
		switch (material.getItemType()) {
		case CARROT:
			drops.add(doAdditionFortune(Material.CARROT_ITEM, 1, 4, fortune));
			break;
		case COAL_ORE:
			drops.add(doMultiplicationFortune(Material.COAL, 1, 1, fortune));
			break;
		case CROPS:
			drops.add(new ItemStack(Material.WHEAT));
			ItemStack seeds = doAdditionFortune(Material.SEEDS, 0, 3, fortune);
			if (seeds.getAmount() > 0) {
				drops.add(seeds);
			}
			break;
		case DIAMOND_ORE:
			drops.add(doMultiplicationFortune(Material.DIAMOND, 1, 1, fortune));
			break;
		case EMERALD_ORE:
			drops.add(doMultiplicationFortune(Material.EMERALD, 1, 1, fortune));
			break;
		case GLOWING_REDSTONE_ORE:
		case REDSTONE_ORE:
			// Only ore that is supposed to use addition-style fortune.
			drops.add(doAdditionFortune(Material.DIAMOND, 1, 1, fortune));
			break;
		case GLOWSTONE:
			ItemStack dust = doAdditionFortune(Material.GLOWSTONE_DUST, 1, 4, fortune);
			if (dust.getAmount() > 4) {
				dust.setAmount(4);
			}
			drops.add(dust);
			break;
		case GRAVEL:
			boolean gravel = Math.random() < (fortune == 1 ? .14 : fortune == 2 ? .25 : 1);
			if (gravel) {
				drops.add(new ItemStack(Material.GRAVEL));
			} else {
				drops.add(new ItemStack(Material.FLINT));
			}
			break;
		case LAPIS_ORE:
			ItemStack lapis = doMultiplicationFortune(Material.INK_SACK, 4, 8, fortune);
			lapis.setData(new MaterialData(Material.INK_SACK, DyeColor.BLUE.getDyeData()));
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
			ItemStack melon = doAdditionFortune(Material.MELON, 1, 9, fortune);
			if (melon.getAmount() > 9) {
				melon.setAmount(9);
			}
			drops.add(melon);
			break;
		case NETHER_WARTS:
			drops.add(doAdditionFortune(Material.NETHER_STALK, 2, 4, fortune));
			break;
		case POTATO:
			drops.add(doAdditionFortune(Material.POTATO_ITEM, 1, 4, fortune));
			if (Math.random() < .02) {
				drops.add(new ItemStack(Material.POISONOUS_POTATO));
			}
			break;
		case QUARTZ_ORE:
			drops.add(doMultiplicationFortune(Material.QUARTZ, 1, 1, fortune));
			break;
		case SEA_LANTERN:
			ItemStack crystals = doAdditionFortune(Material.PRISMARINE_CRYSTALS, 1, 5, fortune);
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

	private static ItemStack doAdditionFortune(Material drop, int min, int max, int fortune) {
		// Max given is the maximum output. We need the maximum number of additional drops + 1
		max -= (min - 1);
		return new ItemStack(drop, (int) (Math.random() * (max + fortune)) + min);
	}

	private static ItemStack doMultiplicationFortune(Material drop, int min, int max, int fortune) {
		max -= (min - 1);
		double fortuneChance = fortune == 1 ? .33 : fortune == 2 ? .25 : .2;
		int fortuneMultiplier = 1;
		for (; fortune > 0; --fortune) {
			if (Math.random() < fortuneChance) {
				++fortuneMultiplier;
			}
		}
		return new ItemStack(drop, (int) ((Math.random() * max) + min) * fortuneMultiplier);
	}
}
