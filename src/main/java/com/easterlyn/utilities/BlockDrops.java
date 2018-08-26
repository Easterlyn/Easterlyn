package com.easterlyn.utilities;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.Effects;
import com.easterlyn.effects.effect.Effect;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.Item;
import net.minecraft.server.v1_13_R2.Items;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.material.MaterialData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility for getting accurate drops from a block - Block.getDrops(ItemStack) does not take into
 * account enchantments.
 *
 * @author Jikoo
 */
public class BlockDrops {

	public static Collection<ItemStack> getDrops(Easterlyn plugin, Player player, ItemStack tool,
			Block block) {
		int bonus;
		Effects effects = plugin.getModule(Effects.class);
		Map<Effect, Integer> effectMap = effects.getAllEffects(player);
		Effect light = effects.getEffect("Fortuna");
		bonus = effectMap.getOrDefault(light, 0);

		return getDrops(tool, block, bonus);
	}

	private static Collection<ItemStack> getDrops(@Nullable ItemStack tool, Block block, int fortuneBonus) {

		if (tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH) && tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0) {
			Collection<ItemStack> drops = getSilkDrops(tool, block.getState().getData());
			if (drops != null) {
				return drops;
			}
		}

		if (tool != null && tool.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
			fortuneBonus += tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
		}

		return getFortuneDrops(tool, block, fortuneBonus);
	}

	public static int getExp(ItemStack tool, Block block) {
		if (tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH)
				&& block.getType() != Material.SPAWNER) {
			return 0;
		}
		if (!isUsableTool(tool, block.getType())) {
			return 0;
		}
		switch (block.getType()) {
			case COAL_ORE:
				return (int) (Math.random() * 3);
			case DIAMOND_ORE:
			case EMERALD_ORE:
				return (int) (3 + Math.random() * 5);
			case LAPIS_ORE:
			case NETHER_QUARTZ_ORE:
				return (int) (2 + Math.random() * 4);
			case REDSTONE_ORE:
				return (int) (1 + Math.random() * 5);
			case SPAWNER:
				return (int) (15 + Math.random() * 29);
			default:
				return 0;
		}
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
			case NETHER_QUARTZ_ORE:
			case REDSTONE_ORE:
			case SNOW_BLOCK:
			case STONE:
				if (isUsableTool(tool, material.getItemType())) {
					drops.add(material.toItemStack(1));
				}
				return drops;
			case BROWN_MUSHROOM_BLOCK:
			case RED_MUSHROOM_BLOCK:
				drops.add(new ItemStack(material.getItemType()));
				return drops;
			case BOOKSHELF:
			case CLAY:
			case GLASS:
			case GLOWSTONE:
			case GRASS:
			case GRAVEL:
			case ICE:
			case MELON:
			case MYCELIUM:
			case PACKED_ICE:
			case SEA_LANTERN:
			case BLACK_STAINED_GLASS:
			case BLUE_STAINED_GLASS:
			case BROWN_STAINED_GLASS:
			case CYAN_STAINED_GLASS:
			case GRAY_STAINED_GLASS:
			case GREEN_STAINED_GLASS:
			case LIGHT_BLUE_STAINED_GLASS:
			case LIGHT_GRAY_STAINED_GLASS:
			case LIME_STAINED_GLASS:
			case MAGENTA_STAINED_GLASS:
			case ORANGE_STAINED_GLASS:
			case PINK_STAINED_GLASS:
			case PURPLE_STAINED_GLASS:
			case RED_STAINED_GLASS:
			case WHITE_STAINED_GLASS:
			case YELLOW_STAINED_GLASS:
			case BLACK_STAINED_GLASS_PANE:
			case BLUE_STAINED_GLASS_PANE:
			case BROWN_STAINED_GLASS_PANE:
			case CYAN_STAINED_GLASS_PANE:
			case GRAY_STAINED_GLASS_PANE:
			case GREEN_STAINED_GLASS_PANE:
			case LIGHT_BLUE_STAINED_GLASS_PANE:
			case LIGHT_GRAY_STAINED_GLASS_PANE:
			case LIME_STAINED_GLASS_PANE:
			case MAGENTA_STAINED_GLASS_PANE:
			case ORANGE_STAINED_GLASS_PANE:
			case PINK_STAINED_GLASS_PANE:
			case PURPLE_STAINED_GLASS_PANE:
			case RED_STAINED_GLASS_PANE:
			case WHITE_STAINED_GLASS_PANE:
			case YELLOW_STAINED_GLASS_PANE:
			case GLASS_PANE:
				drops.add(material.toItemStack(1));
				return drops;
			case ACACIA_LEAVES:
			case BIRCH_LEAVES:
			case DARK_OAK_LEAVES:
			case JUNGLE_LEAVES:
			case OAK_LEAVES:
			case SPRUCE_LEAVES:
				drops.add(new ItemStack(material.getItemType(), 1, (short) (material.getData() % 4)));
				return drops;
			default:
				return null;
		}
	}

	@SuppressWarnings("deprecation")
	private static boolean isUsableTool(ItemStack tool, Material block) {
		// TODO messy
		net.minecraft.server.v1_13_R2.Block nmsBlock = net.minecraft.server.v1_13_R2.Block.asBlock(CraftItemStack.asNMSCopy(new ItemStack(block)).getItem());
		if (nmsBlock == null) {
			return false;
		}
		IBlockData data = nmsBlock.getBlockData();
		return data.getMaterial().isAlwaysDestroyable() || tool != null && tool.getType() != Material.AIR
				&& Item.getById(tool.getType().getId()).canDestroySpecialBlock(data);
	}

	@SuppressWarnings("deprecation")
	private static Collection<ItemStack> getFortuneDrops(ItemStack tool, Block block, int fortune) {
		List<ItemStack> drops = new ArrayList<>();

		if (block.isEmpty() || !isUsableTool(tool, block.getType())) {
			return drops;
		}

		BlockData blockData = block.getBlockData();
		Random random = ThreadLocalRandom.current();

		if (blockData instanceof Ageable) {

			Ageable ageable = ((Ageable) blockData);
			boolean isFullyGrown = ageable.getAge() ==  ageable.getMaximumAge();

			if (blockData.getMaterial() == Material.NETHER_WART) {
				drops.add(new ItemStack(Material.NETHER_WART, isFullyGrown ? 2 + random.nextInt(3) : 1));
				return drops;
			}

			if (blockData.getMaterial() == Material.COCOA) {
				drops.add(new ItemStack(Material.COCOA, isFullyGrown ? 3 : 1, DyeColor.BROWN.getDyeData()));
				return drops;
			}

			// Base max of 3 seeds
			int seeds = random.nextInt(4 + fortune);

			if (seeds < 1) {
				return drops;
			}

			if (blockData.getMaterial() == Material.WHEAT) {
				drops.add(new ItemStack(Material.WHEAT_SEEDS, seeds));
				return drops;
			}
			if (blockData.getMaterial() == Material.BEETROOTS) {
				drops.add(new ItemStack(Material.BEETROOT_SEEDS, seeds));
				return drops;
			}
			if (blockData.getMaterial() == Material.CARROTS) {
				drops.add(new ItemStack(Material.CARROT, seeds));
				return drops;
			}
			if (blockData.getMaterial() == Material.POTATO) {
				drops.add(new ItemStack(Material.CARROT, seeds));
				return drops;
			}
		}

		BlockState blockState = block.getState();

		if (blockState instanceof Banner) {
			ItemStack drop = new ItemStack(blockData.getMaterial());
			BannerMeta bannerMeta = (BannerMeta) Bukkit.getItemFactory().getItemMeta(Material.BLACK_BANNER);
			bannerMeta.setPatterns(((Banner) blockState).getPatterns());
			drop.setItemMeta(bannerMeta);
			drops.add(drop);
			return drops;
		}

		if (blockState instanceof ShulkerBox) {
			ItemStack drop = new ItemStack(blockData.getMaterial());
			BlockStateMeta blockStateMeta = (BlockStateMeta) Bukkit.getItemFactory().getItemMeta(Material.BLACK_SHULKER_BOX);
			blockStateMeta.setBlockState(blockState);
			drop.setItemMeta(blockStateMeta);
			drops.add(drop);
			return drops;
		}

		if (blockData.getMaterial() == Material.COBWEB && tool != null && tool.getType() == Material.SHEARS) {
			drops.add(new ItemStack(Material.COBWEB));
			return drops;
		}

		if (blockData.getMaterial().name().endsWith("_LEAVES")) {

			Material saplingMaterial = Material.getMaterial(blockData.getMaterial().name().replace("_LEAVES", "_SAPLING"));
			if (saplingMaterial == null) {
				return drops;
			}

			// See net.minecraft.server.BlockLeaves
			// Jungle saplings are rarer.
			int dropChanceBase = blockData.getMaterial() == Material.JUNGLE_LEAVES ? 40 : 20;

			// Fortune affects drops up to a 1/10 chance for all tree types.
			if (fortune > 0) {
				dropChanceBase -= (2 << fortune);
				if (dropChanceBase < 10) {
					dropChanceBase = 10;
				}
			}

			if (random.nextInt(dropChanceBase) == 0) {
				drops.add(new ItemStack(saplingMaterial));
			}

			// Oak and dark oak: Apple chance
			if (blockData.getMaterial() != Material.OAK_LEAVES && blockData.getMaterial() != Material.DARK_OAK_LEAVES) {
				return drops;
			}

			dropChanceBase = 200;
			if (fortune > 0) {
				dropChanceBase -= (10 << fortune);
				if (dropChanceBase < 40) {
					dropChanceBase = 40;
				}
			}
			if (random.nextInt(dropChanceBase) == 0) {
				drops.add(new ItemStack(Material.APPLE));
			}
			return drops;
		}

		net.minecraft.server.v1_13_R2.Block nmsBlock = CraftMagicNumbers.getBlock(block.getType());
		net.minecraft.server.v1_13_R2.WorldServer nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
		BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());

		int count = nmsBlock.getDropCount(nmsBlock.getBlockData(), fortune, nmsWorld, blockPosition, random);
		if (count == 0) {
			return drops;
		}

		Item item = nmsBlock.getDropType(nmsBlock.getBlockData(), nmsWorld, blockPosition, fortune).getItem();
		if (item == null || item == Items.AIR) {
			return drops;
		}

		ItemStack drop = new ItemStack(CraftMagicNumbers.getMaterial(item), count);


		drops.add(drop);

		return drops;
	}

}
