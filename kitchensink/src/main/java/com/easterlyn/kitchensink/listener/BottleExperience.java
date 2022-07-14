package com.easterlyn.kitchensink.listener;

import com.easterlyn.util.BlockUtil;
import com.easterlyn.util.inventory.ItemUtil;
import com.github.jikoo.planarwrappers.util.Experience;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BottleExperience implements Listener {

  private final Map<UUID, Long> bottleCreate = new HashMap<>();
  private final Map<UUID, Long> bottleThrow = new HashMap<>();

  @EventHandler
  private void onPlayerInteract(@NotNull PlayerInteractEvent event) {
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      if (BlockUtil.hasRightClickFunction(event)) {
        return;
      }
    } else if (event.getAction() != Action.RIGHT_CLICK_AIR) {
      return;
    }

    if (event.getPlayer().isSneaking()) {
      return;
    }

    ItemStack held = ItemUtil.getHeldItem(event);

    if (held.getType() != Material.GLASS_BOTTLE) {
      return;
    }

    Player player = event.getPlayer();
    Long cooldown = bottleCreate.get(player.getUniqueId());

    if (cooldown != null && cooldown >= System.currentTimeMillis()) {
      return;
    }

    int exp = Experience.getExp(player);
    if (exp >= 11) {
      Experience.changeExp(player, -11);
      ItemUtil.setHeldItem(
          player.getInventory(),
          event.getHand() == EquipmentSlot.HAND,
          ItemUtil.decrement(held, 1));
      player
          .getWorld()
          .dropItem(player.getLocation(), new ItemStack(Material.EXPERIENCE_BOTTLE, 1))
          .setPickupDelay(0);
      bottleThrow.put(player.getUniqueId(), System.currentTimeMillis() + 2000);
      event.setUseItemInHand(Event.Result.DENY);
    }
  }

  @EventHandler(ignoreCancelled = true)
  private void onExpBottle(@NotNull ExpBottleEvent event) {
    event.setExperience(10);
    event.setShowEffect(false);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerItemConsume(@NotNull PlayerItemConsumeEvent event) {
    bottleCreate.put(event.getPlayer().getUniqueId(), System.currentTimeMillis() + 2000);
  }

  @EventHandler(ignoreCancelled = true)
  private void onProjectileLaunch(@NotNull ProjectileLaunchEvent event) {
    if (!(event.getEntity() instanceof ThrownExpBottle)
        || !(event.getEntity().getShooter() instanceof Player player)) {
      return;
    }

    Long cooldown = bottleThrow.get(player.getUniqueId());
    if (cooldown != null && cooldown >= System.currentTimeMillis()) {
      event.setCancelled(true);
    }
  }

}
