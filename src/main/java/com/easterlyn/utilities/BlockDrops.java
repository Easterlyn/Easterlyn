package com.easterlyn.utilities;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.Effects;
import com.easterlyn.effects.effect.Effect;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.server.v1_15_R1.IBlockData;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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

	private static Collection<ItemStack> getDrops(@Nullable ItemStack tool, @NotNull Block block, int fortuneBonus) {
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

		if (fortuneBonus != 0) {
			tool = tool.clone();
			tool.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + fortuneBonus);
		}

		return block.getDrops(tool);
	}

	public static int getExp(ItemStack tool, Block block) {
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

	private static boolean isUsableTool(ItemStack tool, Material broken) {
		net.minecraft.server.v1_15_R1.Block block = CraftMagicNumbers.getBlock(broken);
		if (block == null) {
			return false;
		}
		IBlockData data = block.getBlockData();
		return data.getMaterial().isAlwaysDestroyable() || tool != null && tool.getType() != Material.AIR
				&& CraftMagicNumbers.getItem(tool.getType()).canDestroySpecialBlock(data);
	}

}
