package com.easterlyn.kitchensink.listener;

import com.easterlyn.plugin.EasterlynPlugin;
import com.easterlyn.user.User;
import com.easterlyn.util.BlockUtil;
import com.easterlyn.util.inventory.ItemUtil;
import com.github.jikoo.planarwrappers.util.Experience;
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

  private final String keyBottleCreate = "kitchensink:expBottleCreate";
  private final String keyBottleThrow = "kitchensink:expBottleThrow";
  private final EasterlynPlugin plugin;

  public BottleExperience(@NotNull EasterlynPlugin plugin) {
    this.plugin = plugin;
  }

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
    // TODO bad, need separate in-memory store
    User user = plugin.getCore().getUserManager().getOrLoadNow(player.getUniqueId());

    Object cooldown = user.getTemporaryStorage().get(keyBottleCreate);
    if (cooldown instanceof Long && (Long) cooldown >= System.currentTimeMillis()) {
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
      user.getTemporaryStorage().put(keyBottleThrow, System.currentTimeMillis() + 2000);
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
    plugin.getCore().getUserManager().getPlayer(event.getPlayer().getUniqueId())
        .thenAccept(
            optional ->
                optional.ifPresent(
                    user ->
                        user
                            .getTemporaryStorage()
                            .put(keyBottleCreate, System.currentTimeMillis() + 2000)));
  }

  @EventHandler(ignoreCancelled = true)
  private void onProjectileLaunch(@NotNull ProjectileLaunchEvent event) {
    if (!(event.getEntity() instanceof ThrownExpBottle)
        || !(event.getEntity().getShooter() instanceof Player player)) {
      return;
    }

    // TODO bad
    User user = plugin.getCore().getUserManager().getOrLoadNow(player.getUniqueId());

    Object cooldown = user.getTemporaryStorage().get(keyBottleThrow);
    if (cooldown instanceof Long && (Long) cooldown >= System.currentTimeMillis()) {
      event.setCancelled(true);
    }
  }
}
