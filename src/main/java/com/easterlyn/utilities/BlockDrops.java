package com.easterlyn.utilities;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.Effects;
import com.easterlyn.effects.effect.Effect;
import net.minecraft.server.v1_12_R1.*;
import net.minecraft.server.v1_12_R1.BlockWood.EnumLogVariant;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

		return getDrops(tool != null ? tool.getType() : null, block, fortuneBonus);
	}

	public static int getExp(ItemStack tool, Block block) {
		if (tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH)
				&& block.getType() != Material.MOB_SPAWNER) {
			return 0;
		}
		if (!isUsableTool(tool.getType(), block.getType())) {
			return 0;
		}
		switch (block.getType()) {
		case COAL_ORE:
			return (int) (Math.random() * 3);
		case DIAMOND_ORE:
		case EMERALD_ORE:
			return (int) (3 + Math.random() * 5);
		case LAPIS_ORE:
		case QUARTZ_ORE:
			return (int) (2 + Math.random() * 4);
		case REDSTONE_ORE:
			return (int) (1 + Math.random() * 5);
		case MOB_SPAWNER:
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
		case QUARTZ_ORE:
		case REDSTONE_ORE:
		case SNOW_BLOCK:
		case STONE:
			if (isUsableTool(tool.getType(), material.getItemType())) {
				drops.add(material.toItemStack(1));
			}
			return drops;
		case GLOWING_REDSTONE_ORE:
			if (isUsableTool(tool.getType(), Material.GLOWING_REDSTONE_ORE)) {
				drops.add(new ItemStack(Material.REDSTONE_ORE));
			}
			return drops;
		case HUGE_MUSHROOM_1:
		case HUGE_MUSHROOM_2:
			drops.add(new ItemStack(material.getItemType()));
			return drops;
		case BOOKSHELF:
		case CLAY:
		case GLASS:
		case GLOWSTONE:
		case GRASS:
		case GRAVEL:
		case ICE:
		case MELON_BLOCK:
		case MYCEL:
		case PACKED_ICE:
		case SEA_LANTERN:
		case STAINED_GLASS:
		case STAINED_GLASS_PANE:
		case THIN_GLASS:
			drops.add(material.toItemStack(1));
			return drops;
		case LEAVES:
		case LEAVES_2:
			drops.add(new ItemStack(material.getItemType(), 1, (short) (material.getData() % 4)));
			return drops;
		default:
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	private static boolean isUsableTool(Material tool, Material block) {
		net.minecraft.server.v1_12_R1.Block nmsBlock = net.minecraft.server.v1_12_R1.Block.getById(block.getId());
		if (nmsBlock == null) {
			return false;
		}
		IBlockData data = nmsBlock.getBlockData();
		return data.getMaterial().isAlwaysDestroyable() || tool != null && tool != Material.AIR
				&& Item.getById(tool.getId()).canDestroySpecialBlock(data);
	}

	@SuppressWarnings("deprecation")
	private static Collection<ItemStack> getDrops(Material tool, Block block, int fortune) {
		Random random = ThreadLocalRandom.current();
		List<ItemStack> drops = new ArrayList<>();

		net.minecraft.server.v1_12_R1.Block nmsBlock = net.minecraft.server.v1_12_R1.Block.getById(block.getTypeId());
		net.minecraft.server.v1_12_R1.WorldServer nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
		if (nmsBlock == Blocks.AIR || !isUsableTool(tool, block.getType())) {
			return drops;
		}
		byte data = block.getData();

		if (Blocks.NETHER_WART == nmsBlock) {
			// Nether wart: Drop count is always 0
			drops.add(new ItemStack(Material.NETHER_STALK, 2 + random.nextInt(3)));
			return drops;
		}

		if (Blocks.COCOA == nmsBlock) {
			// Cocoa: drop Item is null rather than a dye
			int age = nmsBlock.fromLegacyData(data).get(BlockCocoa.AGE).intValue();
			int dropAmount = (age >= 2) ? 3 : 1;
			drops.add(new ItemStack(Material.INK_SACK, dropAmount, DyeColor.BROWN.getDyeData()));
			return drops;
		}

		if (nmsBlock instanceof BlockBanner) {
			// Banner: Set data based on tile entity. See BlockBanner#dropNaturally
			BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
			TileEntity localTileEntity = nmsWorld.getTileEntity(position);
			if (localTileEntity instanceof TileEntityBanner) {
				net.minecraft.server.v1_12_R1.ItemStack nmsStack = ItemBanner.a(
						((TileEntityBanner) localTileEntity).color,
						((TileEntityBanner) localTileEntity).patterns);

				drops.add(CraftItemStack.asBukkitCopy(nmsStack));
				return drops;
			}
		}

		if (nmsBlock instanceof BlockShulkerBox) {
			TileEntity localTileEntity = nmsWorld.getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));

			if (localTileEntity instanceof TileEntityShulkerBox) {
				TileEntityShulkerBox localTileEntityShulkerBox = (TileEntityShulkerBox) localTileEntity;

				if ((!(localTileEntityShulkerBox.r())) && (localTileEntityShulkerBox.F())) {
					net.minecraft.server.v1_12_R1.ItemStack nmsStack = new net.minecraft.server.v1_12_R1.ItemStack(Item.getItemOf(nmsBlock));
					NBTTagCompound localNBTTagCompound = new NBTTagCompound();

					localNBTTagCompound.set("BlockEntityTag", ((TileEntityShulkerBox) localTileEntity).f(new NBTTagCompound()));
					nmsStack.setTag(localNBTTagCompound);
					if (localTileEntityShulkerBox.hasCustomName()) {
						nmsStack.g(localTileEntityShulkerBox.getName());
					}

					drops.add(CraftItemStack.asBukkitCopy(nmsStack));
					return drops;
				}
			}
		}

		if (Blocks.SKULL == nmsBlock) {
			// Skull: Set data based on tile entity. See BlockSkull#dropNaturally
			BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
			TileEntity tileentity = nmsWorld.getTileEntity(position);
			net.minecraft.server.v1_12_R1.ItemStack nmsStack;
			if (tileentity instanceof TileEntitySkull) {
				TileEntitySkull tileEntitySkull = (TileEntitySkull) tileentity;
				int skullData = ((TileEntitySkull) tileentity).getSkullType();

				nmsStack = new net.minecraft.server.v1_12_R1.ItemStack(
						Items.SKULL, 1, skullData);

				if (skullData == 3 && tileEntitySkull.getGameProfile() != null) {
					nmsStack.setTag(new NBTTagCompound());
					NBTTagCompound nbttagcompound = new NBTTagCompound();

					GameProfileSerializer.serialize(nbttagcompound, tileEntitySkull.getGameProfile());
					nmsStack.getTag().set("SkullOwner", nbttagcompound);
				}
			} else {
				nmsStack = new net.minecraft.server.v1_12_R1.ItemStack(
						Items.SKULL, 1, 0);
			}

			drops.add(CraftItemStack.asBukkitCopy(nmsStack));
			return drops;
		}

		if (Blocks.WEB == nmsBlock && tool != null && tool == Material.SHEARS) {
			drops.add(new ItemStack(Material.WEB));
			return drops;
		}

		int count = nmsBlock.getDropCount(fortune, random);
		if (count == 0) {
			return drops;
		}

		Item item = nmsBlock.getDropType(nmsBlock.fromLegacyData(data), random, fortune);
		if (item == null || item == Items.a) {
			return drops;
		}

		ItemStack drop = new ItemStack(CraftMagicNumbers.getMaterial(item), count,
				(short) nmsBlock.getDropData(nmsBlock.fromLegacyData(data)));

		if (nmsBlock instanceof BlockLeaves) {
			IBlockData iblockdata = nmsWorld.getType(new BlockPosition(block.getX(), block.getY(), block.getZ()));
			EnumLogVariant variant = Blocks.LEAVES == nmsBlock
					? iblockdata.get(BlockLeaves1.VARIANT) : iblockdata.get(BlockLeaves2.VARIANT);

			// See BlockLeaves#i(IBlockData) and BlockLeaves1#i(IBlockData)
			int dropChanceBase = variant == EnumLogVariant.JUNGLE ? 40 : 20;

			// See BlockLeaves#dropNaturally
			if (fortune > 0) {
				dropChanceBase -= (2 << fortune);
				if (dropChanceBase < 10) {
					dropChanceBase = 10;
				}
			}
			if (random.nextInt(dropChanceBase) == 0) {
				drop.setAmount(1);
				drops.add(drop);
			}

			// Oak and dark oak: Apple chance (BlockLeaves#a)
			if (variant != EnumLogVariant.OAK && variant != EnumLogVariant.DARK_OAK) {
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

		if (nmsBlock instanceof BlockCrops && data >= ((BlockCrops) nmsBlock).g()) {
			// Base max of 3 seeds
			int seeds = random.nextInt(4 + fortune);
			if (seeds == 0) {
				drops.add(drop);
				return drops;
			}
			if (Blocks.WHEAT == nmsBlock) {
				drops.add(new ItemStack(Material.SEEDS, seeds));
			} else if (Blocks.BEETROOT == nmsBlock) {
				drops.add(new ItemStack(Material.BEETROOT_SEEDS, seeds));
			} else {
				// Carrot/potato drop the same ripe product item as seed item
				drop.setAmount(drop.getAmount() + seeds);
			}
			drops.add(drop);
			return drops;
		}

		drops.add(drop);

		return drops;
	}

}
