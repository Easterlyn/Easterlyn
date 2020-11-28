package com.easterlyn.effect;

import com.easterlyn.EasterlynCore;
import com.easterlyn.EasterlynEffects;
import com.easterlyn.effect.event.IndirectBreakEvent;
import com.easterlyn.util.BlockUpdateManager;
import com.easterlyn.util.BlockUtil;
import com.easterlyn.util.ExperienceUtil;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mine or dig a 3x3 area at once.
 *
 * @author Jikoo
 */
public class EffectTunnelBore extends Effect {

  private final BlockFace[] faces;
  private final BlockFace[] levels;

  public EffectTunnelBore(EasterlynEffects plugin) {
    super(plugin, "Tunnel Bore", EquipmentSlots.TOOL, 1500, 1, 1);
    this.faces =
        new BlockFace[] {
          BlockFace.NORTH,
          BlockFace.SOUTH,
          BlockFace.EAST,
          BlockFace.WEST,
          BlockFace.NORTH_WEST,
          BlockFace.NORTH_EAST,
          BlockFace.SOUTH_WEST,
          BlockFace.SOUTH_EAST
        };
    this.levels = new BlockFace[] {BlockFace.DOWN, BlockFace.SELF, BlockFace.UP};
  }

  @Override
  public void applyEffect(@NotNull LivingEntity entity, int level, @Nullable Event event) {
    if (!(event instanceof BlockBreakEvent) || event instanceof IndirectBreakEvent) {
      return;
    }

    BlockBreakEvent breakEvent = (BlockBreakEvent) event;
    Player player = breakEvent.getPlayer();

    if (player.isSneaking()) {
      // Sneak to mine single blocks
      return;
    }

    Block block = breakEvent.getBlock();

    if (!BlockUtil.isCorrectTool(
        player.getInventory().getItemInMainHand(), breakEvent.getBlock().getType())) {
      // Require correct tool used
      return;
    }

    RegisteredServiceProvider<EasterlynCore> registration =
        getPlugin().getServer().getServicesManager().getRegistration(EasterlynCore.class);
    if (registration == null) {
      return;
    }
    BlockUpdateManager budManager = registration.getProvider().getBlockUpdateManager();
    /*
     * Forgiveness of .25 hardness allows indirect breaking of most common related materials
     * while not excessively increasing risk of accidents inside builds. Allows for mining gravel
     * via sand and dirt, etc.
     */
    float hardness = block.getType().getHardness() + 0.25F;
    for (BlockFace yLevel : levels) {
      if (block.getY() == 0 && yLevel == BlockFace.DOWN) {
        continue;
      }
      Block relativeCenter = block.getRelative(yLevel);
      if (yLevel != BlockFace.SELF) {
        if (handleBlock(hardness, relativeCenter, player, budManager)) {
          return;
        }
      }
      for (BlockFace face : faces) {
        if (handleBlock(hardness, relativeCenter.getRelative(face), player, budManager)) {
          return;
        }
      }
    }
    player.updateInventory();
  }

  private boolean handleBlock(
      float requiredHardness, Block block, Player player, BlockUpdateManager budManager) {
    float blockHardness = block.getType().getHardness();
    if (blockHardness > requiredHardness
        || blockHardness < 0
        || block.getState() instanceof InventoryHolder
        || !BlockUtil.isCorrectTool(player.getInventory().getItemInMainHand(), block.getType())) {
      return false;
    }

    IndirectBreakEvent event = new IndirectBreakEvent(block, player);
    Bukkit.getServer().getPluginManager().callEvent(event);
    if (event.isCancelled() || block.isLiquid()) {
      return false;
    }
    if (player.getGameMode() == GameMode.CREATIVE) {
      block.setType(Material.AIR, false);
      budManager.queueBlock(block);
      return false;
    }
    // Item breaking blocks has to be in main hand
    ItemStack hand = player.getInventory().getItemInMainHand();
    Collection<ItemStack> drops = BlockUtil.getDrops(hand, block);
    int exp = BlockUtil.getExp(player, hand, block);
    block.setType(Material.AIR, false);
    for (ItemStack is : drops) {
      player.getWorld().dropItem(player.getLocation(), is).setPickupDelay(0);
    }
    if (exp > 0) {
      ExperienceUtil.changeExp(player, exp);
    }
    budManager.queueBlock(block);
    ItemMeta handMeta = hand.getItemMeta();
    if (handMeta != null
        && !handMeta.isUnbreakable()
        && handMeta instanceof Damageable
        && (!hand.containsEnchantment(Enchantment.DURABILITY)
            || ThreadLocalRandom.current().nextDouble()
                < 1.0 / (hand.getEnchantmentLevel(Enchantment.DURABILITY) + 2))) {
      Damageable damageable = (Damageable) handMeta;
      damageable.setDamage(damageable.getDamage() + 1);
      hand.setItemMeta(handMeta);
      return damageable.getDamage() > hand.getType().getMaxDurability();
    }
    return false;
  }
}
