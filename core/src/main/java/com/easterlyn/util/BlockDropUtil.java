package com.easterlyn.util;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.TileEntity;
import net.minecraft.server.v1_14_R1.TileEntityFurnace;
import net.minecraft.server.v1_14_R1.WorldServer;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_14_R1.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility for getting accurate drops from a block - Block.getDrops(ItemStack) does not take into
 * account enchantments.
 *
 * @author Jikoo
 */
public class BlockDropUtil {

	public static Collection<ItemStack> getDrops(@Nullable ItemStack tool, @NotNull Block block) {
		if (tool == null) {
			return block.getDrops();
		}

		// Block#getDrops does not properly support silk touch for coral
		if (tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0 && Tag.CORALS.isTagged(block.getType())) {
			Material type = block.getType();
			if (Tag.WALL_CORALS.isTagged(type)) {
				type = Material.matchMaterial(type.name().replace("WALL_", ""));
			}
			if (type != null) {
				ItemStack itemStack = new ItemStack(type);
				if (itemStack.getType() != Material.AIR) {
					return Collections.singleton(new ItemStack(type));
				}
			}
		}

		return block.getDrops(tool);
	}

	public static int getExp(@Nullable Player player, @Nullable ItemStack tool, @NotNull Block block) {
		// TODO Block#getExpDrop
		if (tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH)
				&& block.getType() != Material.SPAWNER) {
			return 0;
		}
		if (!isUsableTool(tool, block.getType())) {
			return 0;
		}
		switch (block.getType()) {
			case COAL_ORE:
				return ThreadLocalRandom.current().nextInt(3);
			case DIAMOND_ORE:
			case EMERALD_ORE:
				return 3 + ThreadLocalRandom.current().nextInt(5);
			case LAPIS_ORE:
			case NETHER_QUARTZ_ORE:
				return 2 + ThreadLocalRandom.current().nextInt(4);
			case REDSTONE_ORE:
				return 1 + ThreadLocalRandom.current().nextInt(5);
			case SPAWNER:
				return 15 + ThreadLocalRandom.current().nextInt(29);
			case FURNACE:
			case BLAST_FURNACE:
			case SMOKER:
				if (!(block.getWorld() instanceof CraftWorld) || !(player instanceof CraftPlayer)) {
					return 0;
				}
				WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
				TileEntity tileEntity = worldServer.getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
				if (!(tileEntity instanceof TileEntityFurnace)) {
					return 0;
				}
				// Fire extraction logic using tool as extracted item. N.B. amount < 1 will still drop experience but not call a FurnaceExtractEvent.
				((TileEntityFurnace) tileEntity).d(((CraftPlayer) player).getHandle(), CraftItemStack.asNMSCopy(tool), 1);
				return 0;
			default:
				return 0;
		}
	}

	private static boolean isUsableTool(ItemStack tool, Material broken) {
		net.minecraft.server.v1_14_R1.Block block = CraftMagicNumbers.getBlock(broken);
		if (block == null) {
			return false;
		}
		IBlockData data = block.getBlockData();
		return data.getMaterial().isAlwaysDestroyable() || tool != null && tool.getType() != Material.AIR
				&& CraftMagicNumbers.getItem(tool.getType()).canDestroySpecialBlock(data);
	}

}
