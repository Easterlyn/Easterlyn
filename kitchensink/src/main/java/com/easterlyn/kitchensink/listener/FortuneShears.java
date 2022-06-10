package com.easterlyn.kitchensink.listener;

import com.easterlyn.event.ReportableEvent;
import com.github.jikoo.planarwrappers.util.Generics;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FortuneShears implements Listener {

  // TODO allow anvil enchanting via book

  @EventHandler(ignoreCancelled = true)
  public void onPlayerShearEntity(@NotNull PlayerShearEntityEvent event) {
    ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
    if (hand.getType() == Material.SHEARS) {
      hand = event.getPlayer().getInventory().getItemInMainHand();
    } else {
      hand = event.getPlayer().getInventory().getItemInOffHand();
    }
    if (!hand.hasItemMeta()
        || !hand.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)
        || hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) < 1) {
      return;
    }
    ItemStack is = getDrop(event.getEntity());
    if (is == null) {
      return;
    }
    int total =
        ThreadLocalRandom.current()
            .nextInt(hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + 1);
    if (total == 0) {
      return;
    }
    is.setAmount(total);
    event
        .getEntity()
        .getWorld()
        .dropItemNaturally(event.getEntity().getLocation().add(0, .5, 0), is);
  }

  private @Nullable ItemStack getDrop(Entity entity) {
    if (entity instanceof Sheep) {
      DyeColor color = Generics.orDefault(((Sheep) entity).getColor(), DyeColor.WHITE);
      Material material = Material.matchMaterial(color.name() + "_WOOL");
      if (material != null) {
        return new ItemStack(material);
      }

      ReportableEvent.call("Unable to match wool for DyeColor: " + color.name());
      return new ItemStack(Material.WHITE_WOOL);
    }

    if (entity instanceof MushroomCow) {
      switch (((MushroomCow) entity).getVariant()) {
        case RED:
          return new ItemStack(Material.RED_MUSHROOM);
        case BROWN:
          return new ItemStack(Material.BROWN_MUSHROOM);
        default:
          ReportableEvent.call(
              "Unhandled MushroomCow variant: " + ((MushroomCow) entity).getVariant().name());
          break;
      }
    }

    ReportableEvent.call("Unhandled shearable entity: " + entity.getType().name());
    return null;
  }
}
