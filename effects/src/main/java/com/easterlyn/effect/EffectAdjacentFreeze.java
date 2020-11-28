package com.easterlyn.effect;

import com.easterlyn.EasterlynEffects;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Freeze all adjacent water source blocks. Not useful against the ground without silk touch, but
 * otherwise nice.
 *
 * @author Jikoo
 */
public class EffectAdjacentFreeze extends EffectAdjacentBlockPlacement {

  public EffectAdjacentFreeze(EasterlynEffects plugin) {
    super(plugin, "Eternally Frozen", 400);
  }

  @Override
  protected boolean handleAdjacentBlock(Player player, Block block, int currentCount) {
    if (block.getType() == Material.WATER) {
      if (handleBlockSet(player, block, Material.ICE)) {
        if (currentCount == 0) {
          block
              .getWorld()
              .playSound(
                  block.getLocation().add(.5, 0, .5),
                  Sound.ENTITY_GENERIC_SWIM,
                  SoundCategory.BLOCKS,
                  16,
                  1);
        }
        return true;
      }
    }
    return false;
  }
}
