package com.easterlyn.util;

import com.easterlyn.event.ReportableEvent;
import com.easterlyn.util.inventory.ItemUtil;
import com.easterlyn.util.reflection.ReflectionUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
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
 * Utility for block-related functions.
 *
 * @author Jikoo
 */
public final class BlockUtil {

  private static final Set<BiFunction<Block, ItemStack, Boolean>> BLOCK_FUNCTIONS = new HashSet<>();

  private BlockUtil() {}

  public static Collection<ItemStack> getDrops(@Nullable ItemStack tool, @NotNull Block block) {
    if (tool == null) {
      return block.getDrops();
    }

    // Block#getDrops does not properly support silk touch for coral
    if (tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0
        && Tag.CORALS.isTagged(block.getType())) {
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

  public static int getExp(
      @Nullable Player player, @Nullable ItemStack tool, @NotNull Block block) {
    // TODO Block#getExpDrop
    if (tool != null
        && tool.containsEnchantment(Enchantment.SILK_TOUCH)
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
        ServerLevel serverLevel = ((CraftWorld) block.getWorld()).getHandle();
        BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());
        BlockEntity tileEntity =
            serverLevel.getBlockEntity(blockPos);
        if (!(tileEntity instanceof AbstractFurnaceBlockEntity)) {
          return 0;
        }
        // Fire extraction logic using tool as extracted item.
        // N.B. amount < 1 will still drop experience but not call a FurnaceExtractEvent.
        ((AbstractFurnaceBlockEntity) tileEntity)
            .awardUsedRecipesAndPopExperience(
                ((CraftPlayer) player).getHandle(),
                CraftItemStack.asNMSCopy(tool),
                1);
        return 0;
      default:
        return 0;
    }
  }

  public static boolean isToolRequired(@NotNull Material blockType) {
    net.minecraft.world.level.block.Block block = CraftMagicNumbers.getBlock(blockType);

    if (block == null) {
      return false;
    }

    net.minecraft.world.level.block.state.BlockState data = block.defaultBlockState();

    return !data.getMaterial().isReplaceable() && data.requiresCorrectToolForDrops();
  }

  public static boolean isCorrectTool(@Nullable ItemStack tool, @NotNull Material blockType) {
    if (tool == null
        || tool.getType().isAir()
        || !(CraftMagicNumbers.getItem(tool.getType()) instanceof DiggerItem diggerItem)) {
      return !isToolRequired(blockType);
    }

    // NMSREF double check this on updates
    return 1.0F != diggerItem.getDestroySpeed(
        CraftItemStack.asNMSCopy(tool),
        CraftMagicNumbers.getBlock(blockType).defaultBlockState());
  }

  private static boolean isUsableTool(@Nullable ItemStack tool, @NotNull Material blockType) {
    net.minecraft.world.level.block.Block block = CraftMagicNumbers.getBlock(blockType);

    if (block == null) {
      return false;
    }

    net.minecraft.world.level.block.state.BlockState data = block.defaultBlockState();

    if (data.getMaterial().isReplaceable() || !data.requiresCorrectToolForDrops()) {
      // Instant break or always breakable
      return true;
    }

    return tool != null
        && tool.getType() != Material.AIR
        && CraftMagicNumbers.getItem(tool.getType()).isCorrectToolForDrops(data);
  }

  public static void addRightClickFunction(
      @NotNull BiFunction<Block, ItemStack, Boolean> function) {
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
    if (item instanceof AxeItem || item instanceof HoeItem || item instanceof ShovelItem) {
      Map<?, ?> blockBlockMap = null;
      try {
        blockBlockMap = ReflectionUtil.getFieldValue(item, "a", Map.class);
      } catch (ReflectiveOperationException e) {
        ReportableEvent.call("Exception fetching list of blocks modifiable by tool!", e, 10);
      }
      if (blockBlockMap != null
          && blockBlockMap.containsKey(CraftMagicNumbers.getBlock(blockType))) {
        return true;
      }
    }

    BlockData blockData = block.getBlockData();
    if (blockData instanceof Bed || blockData instanceof Levelled) {
      return true;
    }

    if (blockData instanceof Powerable) {
      return !(blockData instanceof Observer || blockData instanceof RedstoneRail);
    }

    if (blockData instanceof Waterlogged
        && (hand.getType() == Material.BUCKET || hand.getType() == Material.WATER_BUCKET)) {
      return true;
    }

    BlockState state = block.getState();
    if (state instanceof InventoryHolder || state instanceof TileState) {
      return true;
    }

    for (BiFunction<Block, ItemStack, Boolean> function : BLOCK_FUNCTIONS) {
      if (function.apply(block, hand)) {
        return true;
      }
    }

    if (hand.getType() == Material.GLASS_BOTTLE) {
      RayTraceResult rayTraceResult =
          event.getPlayer().rayTraceBlocks(4, FluidCollisionMode.ALWAYS);
      if (rayTraceResult == null) {
        return false;
      }
      Block hitBlock = rayTraceResult.getHitBlock();
      return hitBlock != null
          && (hitBlock.getType() == Material.WATER || hitBlock.getType() == Material.CAULDRON);
    }

    return false;
  }
}
