package com.easterlyn.effect;

import com.easterlyn.EasterlynCore;
import com.easterlyn.EasterlynEffects;
import com.easterlyn.util.BlockUpdateManager;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base for multiple block break behaviors that affect adjacent blocks.
 *
 * @author Jikoo
 */
public abstract class EffectAdjacentBlockModifier extends Effect {

  private final BlockFace[] faces;

  public EffectAdjacentBlockModifier(EasterlynEffects plugin, String name, int cost) {
    super(plugin, name, EquipmentSlots.TOOL, cost, 1, 1);
    this.faces =
        new BlockFace[] {
          BlockFace.UP,
          BlockFace.DOWN,
          BlockFace.NORTH,
          BlockFace.SOUTH,
          BlockFace.EAST,
          BlockFace.WEST
        };
  }

  @Override
  public void applyEffect(@NotNull LivingEntity entity, int level, @Nullable Event event) {
    if (!(event instanceof BlockBreakEvent)) {
      return;
    }
    RegisteredServiceProvider<EasterlynCore> registration =
        getPlugin().getServer().getServicesManager().getRegistration(EasterlynCore.class);
    if (registration == null) {
      return;
    }
    BlockUpdateManager budManager = registration.getProvider().getBlockUpdateManager();
    BlockBreakEvent breakEvent = (BlockBreakEvent) event;
    Player player = breakEvent.getPlayer();
    int currentCount = 0;
    for (BlockFace face : faces) {
      Block relative = breakEvent.getBlock().getRelative(face);
      if (handleAdjacentBlock(player, relative, currentCount)) {
        budManager.queueBlock(relative);
      }
    }
  }

  protected abstract boolean handleAdjacentBlock(Player player, Block block, int currentCount);
}
