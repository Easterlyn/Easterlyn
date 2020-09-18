package com.easterlyn.util;

import com.easterlyn.event.ReportableEvent;
import com.easterlyn.util.inventory.ItemUtil;
import com.easterlyn.util.reflection.ReflectionUtil;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.IBlockData;
import net.minecraft.server.v1_16_R2.Item;
import net.minecraft.server.v1_16_R2.ItemAxe;
import net.minecraft.server.v1_16_R2.ItemHoe;
import net.minecraft.server.v1_16_R2.ItemSpade;
import net.minecraft.server.v1_16_R2.ItemTool;
import net.minecraft.server.v1_16_R2.TileEntity;
import net.minecraft.server.v1_16_R2.TileEntityFurnace;
import net.minecraft.server.v1_16_R2.WorldServer;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Observer;
import org.bukkit.block.data.type.RedstoneRail;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility for getting accurate drops from a block - Block.getDrops(ItemStack) does not take into
 * account enchantments.
 *
 * @author Jikoo
 */
public class BlockUtil {

	private static final Set<BiFunction<Block, ItemStack, Boolean>> BLOCK_FUNCTIONS = new HashSet<>();
	private static final Field ITEMTOOL_A;

	static {
		Field fieldItemToolA;
		try {
			fieldItemToolA = ReflectionUtil.getField(ItemTool.class, "a", Set.class);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			fieldItemToolA = null;
		}

		ITEMTOOL_A = fieldItemToolA;
	}

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

	public static boolean isToolRequired(@NotNull Material blockType) {
		net.minecraft.server.v1_16_R2.Block block = CraftMagicNumbers.getBlock(blockType);

		if (block == null) {
			return false;
		}

		IBlockData data = block.getBlockData();

		return !data.getMaterial().isReplaceable() && data.isRequiresSpecialTool();
	}

	public static boolean isCorrectTool(@Nullable ItemStack tool, @NotNull Material blockType) {
		if (!isToolRequired(blockType)) {
			return true;
		}

		if (ITEMTOOL_A == null) {
			return isUsableTool(tool, blockType);
		}

		if (tool == null || tool.getType().isAir()) {
			return false;
		}

		Item item = CraftMagicNumbers.getItem(tool.getType());

		if (!(item instanceof ItemTool)) {
			return false;
		}

		try {
			Set<?> toolBlocks = (Set<?>) ITEMTOOL_A.get(item);
			return toolBlocks.contains(CraftMagicNumbers.getBlock(blockType));
		} catch (IllegalAccessException | ClassCastException e) {
			ReportableEvent.call("Exception fetching list of blocks breakable by tool!", e, 10);
			return isUsableTool(tool, blockType);
		}
	}

	private static boolean isUsableTool(@Nullable ItemStack tool, @NotNull Material blockType) {
		net.minecraft.server.v1_16_R2.Block block = CraftMagicNumbers.getBlock(blockType);

		if (block == null) {
			return false;
		}

		IBlockData data = block.getBlockData();

		if (data.getMaterial().isReplaceable() || !data.isRequiresSpecialTool()) {
			// Instant break or always breakable
			return true;
		}

		return tool != null && tool.getType() != Material.AIR && CraftMagicNumbers.getItem(tool.getType()).canDestroySpecialBlock(data);
	}

	public static void addRightClickFunction(@NotNull BiFunction<Block, ItemStack, Boolean> function) {
		BLOCK_FUNCTIONS.add(function);
	}

	public static boolean hasRightClickFunction(@NotNull PlayerInteractEvent event) {

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
			return false;
		}

		Block block = event.getClickedBlock();
		Material blockType = block.getType();

		if (blockType.isInteractable()) {
			return true;
		}

		ItemStack hand = ItemUtil.getHeldItem(event);

		if (blockType == Material.END_STONE) {
			// Special case: player is probably attempting to bottle dragon's breath
			return block.getWorld().getEnvironment() == World.Environment.THE_END
					&& hand.getType() == Material.GLASS_BOTTLE;
		}

		Item item = CraftMagicNumbers.getItem(hand.getType());
		if (item instanceof ItemAxe || item instanceof ItemHoe || item instanceof ItemSpade) {
			Map<?, ?> blockBlockMap = null;
			try {
				blockBlockMap = ReflectionUtil.getFieldValue(item, "a", Map.class);
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
				// TODO error -> ReportableEvent once tested
			}
			if (blockBlockMap != null && blockBlockMap.containsKey(CraftMagicNumbers.getBlock(blockType))) {
				return true;
			}
		}

		BlockState state = block.getState();
		if (state instanceof InventoryHolder || state instanceof TileState) {
			return true;
		}

		BlockData blockData = block.getBlockData();
		if (blockData instanceof Bed || blockData instanceof Levelled) {
			return true;
		}

		if (blockData instanceof Powerable) {
			return !(blockData instanceof Observer || blockData instanceof RedstoneRail);
		}

		if (blockData instanceof Waterlogged && (hand.getType() == Material.BUCKET || hand.getType() == Material.WATER_BUCKET)) {
			return true;
		}

		for (BiFunction<Block, ItemStack, Boolean> function : BLOCK_FUNCTIONS) {
			if (function.apply(block, hand)) {
				return true;
			}
		}

		if (hand.getType() == Material.GLASS_BOTTLE) {
			RayTraceResult rayTraceResult = event.getPlayer().rayTraceBlocks(4, FluidCollisionMode.ALWAYS);
			if (rayTraceResult == null) {
				return false;
			}
			Block hitBlock = rayTraceResult.getHitBlock();
			return hitBlock != null && (hitBlock.getType() == Material.WATER || hitBlock.getType() == Material.CAULDRON);
		}

		return false;

	}

	private BlockUtil() {}

}
