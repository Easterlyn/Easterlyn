package co.sblock.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import co.sblock.Sblock;
import co.sblock.effects.Effects;
import co.sblock.effects.effect.Effect;

import net.minecraft.server.v1_9_R1.BlockBanner;
import net.minecraft.server.v1_9_R1.BlockCocoa;
import net.minecraft.server.v1_9_R1.BlockCrops;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.Blocks;
import net.minecraft.server.v1_9_R1.GameProfileSerializer;
import net.minecraft.server.v1_9_R1.IBlockData;
import net.minecraft.server.v1_9_R1.Item;
import net.minecraft.server.v1_9_R1.Items;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.TileEntity;
import net.minecraft.server.v1_9_R1.TileEntityBanner;
import net.minecraft.server.v1_9_R1.TileEntitySkull;

import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_9_R1.util.CraftMagicNumbers;

/**
 * Utility for getting accurate drops from a block - Block.getDrops(ItemStack) does not take into
 * account enchantments.
 * 
 * @author Jikoo
 */
public class BlockDrops {

	private static final Random RAND = new Random();

	public static Collection<ItemStack> getDrops(Sblock plugin, Player player, ItemStack tool,
			Block block) {
		int bonus;
		Effects effects = plugin.getModule(Effects.class);
		Map<Effect, Integer> effectMap = effects.getAllEffects(player);
		Effect light = effects.getEffect("Fortuna");
		if (effectMap.containsKey(light)) {
			bonus = effectMap.get(light);
		} else {
			bonus = 0;
		}

		return getDrops(tool, block, bonus);
	}

	private static Collection<ItemStack> getDrops(ItemStack tool, Block block, int fortuneBonus) {

		if (tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH) && tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0) {
			Collection<ItemStack> drops = getSilkDrops(tool, block.getState().getData());
			if (drops != null) {
				return drops;
			}
		}

		if (tool.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
			fortuneBonus += tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
		}

		if (fortuneBonus > 0) {
			Collection<ItemStack> drops = getFortuneDrops(block.getState().getData(), fortuneBonus);
			if (drops != null) {
				return drops;
			}
		}

		return getDefaultDrops(tool != null ? tool.getType() : null, block);
	}

	public static int getExp(ItemStack tool, Block block) {
		if (tool.containsEnchantment(Enchantment.SILK_TOUCH)
				&& block.getType() != Material.MOB_SPAWNER) {
			return 0;
		}
		if (!doDrops(tool.getType(), block.getType())) {
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
		case HUGE_MUSHROOM_1:
		case HUGE_MUSHROOM_2:
		case LAPIS_ORE:
		case QUARTZ_ORE:
		case REDSTONE_ORE:
		case SNOW_BLOCK:
		case STONE:
		case WEB:
			if (doDrops(tool.getType(), material.getItemType())) {
				drops.add(new ItemStack(material.getItemType()));
			}
			return drops;
		case GLOWING_REDSTONE_ORE:
			if (doDrops(tool.getType(), Material.GLOWING_REDSTONE_ORE)) {
				drops.add(new ItemStack(Material.REDSTONE_ORE));
			}
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

	@SuppressWarnings("deprecation")
	private static boolean doDrops(Material tool, Material block) {
		net.minecraft.server.v1_9_R1.Block nmsBlock = net.minecraft.server.v1_9_R1.Block.getById(block.getId());
		if (nmsBlock == null) {
			return false;
		}
		IBlockData data = nmsBlock.getBlockData();
		if (data.getMaterial().isAlwaysDestroyable()) {
			return true;
		}
		return tool != null && Item.getById(tool.getId()).canDestroySpecialBlock(data);
	}

	@SuppressWarnings("deprecation")
	private static Collection<ItemStack> getFortuneDrops(MaterialData material, int fortune) {
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
			drops.add(doFortune(Material.REDSTONE, 4, 5, fortune, false));
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
					: fortune < 1 ? .06 : fortune == 1 ? .0625 : fortune == 2 ? .0833 : .1;
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
		int random = RAND.nextInt(max) + min;
		int bonus = RAND.nextInt(fortune + 2);
		if (multiply) {
			bonus += 1;
			return new ItemStack(drop, random * bonus);
		}
		return new ItemStack(drop, random + bonus);
	}

	@SuppressWarnings("deprecation")
	private static Collection<ItemStack> getDefaultDrops(Material tool, Block block) {
		List<ItemStack> drops = new ArrayList<>();

		net.minecraft.server.v1_9_R1.Block nmsBlock = net.minecraft.server.v1_9_R1.Block.getById(block.getTypeId());
		net.minecraft.server.v1_9_R1.WorldServer nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
		if (nmsBlock == Blocks.AIR || !doDrops(tool, block.getType())) {
			return drops;
		}
		byte data = block.getData();

		if (nmsBlock == Blocks.NETHER_WART) {
			// Nether wart: Drop count is always 0
			drops.add(new ItemStack(Material.NETHER_STALK, 2 + RAND.nextInt(3)));
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
				net.minecraft.server.v1_9_R1.ItemStack nmsStack = new net.minecraft.server.v1_9_R1.ItemStack(
						Items.BANNER, 1, ((TileEntityBanner) localTileEntity).b());

				NBTTagCompound localNBTTagCompound = new NBTTagCompound();
				localTileEntity.save(localNBTTagCompound);
				localNBTTagCompound.remove("x");
				localNBTTagCompound.remove("y");
				localNBTTagCompound.remove("z");
				localNBTTagCompound.remove("id");
				nmsStack.a("BlockEntityTag", localNBTTagCompound);

				drops.add(CraftItemStack.asBukkitCopy(nmsStack));
				return drops;
			}
		}

		if (Blocks.SKULL == nmsBlock) {
			// Skull: Set data based on tile entity. See BlockSkull#dropNaturally
			BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
			TileEntity tileentity = nmsWorld.getTileEntity(position);
			net.minecraft.server.v1_9_R1.ItemStack nmsStack;
			if (tileentity instanceof TileEntitySkull) {
				TileEntitySkull tileEntitySkull = (TileEntitySkull) tileentity;
				int skullData = ((TileEntitySkull) tileentity).getSkullType();

				nmsStack = new net.minecraft.server.v1_9_R1.ItemStack(
						Items.SKULL, 1, skullData);

				if (skullData == 3 && tileEntitySkull.getGameProfile() != null) {
					nmsStack.setTag(new NBTTagCompound());
					NBTTagCompound nbttagcompound = new NBTTagCompound();

					GameProfileSerializer.serialize(nbttagcompound, tileEntitySkull.getGameProfile());
					nmsStack.getTag().set("SkullOwner", nbttagcompound);
				}
			} else {
				nmsStack = new net.minecraft.server.v1_9_R1.ItemStack(
						Items.SKULL, 1, 0);
			}

			drops.add(CraftItemStack.asBukkitCopy(nmsStack));
			return drops;
		}

		int count = nmsBlock.getDropCount(0, RAND);
		if (count == 0) {
			return drops;
		}

		Item item = nmsBlock.getDropType(nmsBlock.fromLegacyData(data), RAND, 0);
		if (item == null) {
			return drops;
		}

		ItemStack drop = new ItemStack(CraftMagicNumbers.getMaterial(item), count,
				(short) nmsBlock.getDropData(nmsBlock.fromLegacyData(data)));

		if (nmsBlock instanceof BlockCrops && data >= 7) {
			int seeds = 0;
			for (int i = 0; i < 3; i++) {
				if (RAND.nextInt(15) <= data) {
					++seeds;
				}
			}
			if (seeds == 0) {
				drops.add(drop);
				return drops;
			}
			if (nmsBlock == Blocks.WHEAT) {
				drops.add(new ItemStack(Material.SEEDS, seeds));
			} else {
				// Carrot/potato drop the same ripe product item as seed item
				drop.setAmount(drop.getAmount() + seeds);
			}
		}

		drops.add(drop);

		return drops;
	}
}
