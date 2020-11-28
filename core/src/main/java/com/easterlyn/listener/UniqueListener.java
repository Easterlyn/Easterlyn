package com.easterlyn.listener;

import com.easterlyn.util.inventory.ItemUtil;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class UniqueListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onPrepareItemCraft(PrepareItemCraftEvent event) {
    if (event.getRecipe() instanceof Keyed
        && ((Keyed) event.getRecipe()).getKey().getKey().startsWith(ItemUtil.UNIQUE_KEYED_PREFIX)) {
      // Allow custom recipes using unique items
      return;
    }

    for (ItemStack itemStack : event.getInventory().getMatrix()) {
      if (ItemUtil.isUniqueItem(itemStack)) {
        event.getInventory().setResult(ItemUtil.AIR);
        return;
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onCraftItem(CraftItemEvent event) {
    if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE
        && !event.getWhoClicked().hasPermission("easterlyn.events.creative.unfiltered")) {
      event.setCurrentItem(ItemUtil.cleanNBT(event.getCurrentItem()));
    }

    if (event.getRecipe() instanceof Keyed
        && ((Keyed) event.getRecipe()).getKey().getKey().startsWith(ItemUtil.UNIQUE_KEYED_PREFIX)) {
      // Allow custom recipes using unique items if key is prefixed correctly
      return;
    }

    for (ItemStack itemStack : event.getInventory().getMatrix()) {
      if (ItemUtil.isUniqueItem(itemStack)) {
        event
            .getWhoClicked()
            .sendMessage("events.craft.unique".replace("{ITEM}", ItemUtil.getItemName(itemStack)));
        event.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {

    boolean top = event.getRawSlot() == event.getView().convertSlot(event.getRawSlot());
    switch (event.getClick()) {
      case NUMBER_KEY:
        ItemStack hotbar = event.getView().getBottomInventory().getItem(event.getHotbarButton());
        if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
            && (ItemUtil.isUniqueItem(event.getCursor()) || ItemUtil.isUniqueItem(hotbar))) {
          event.setCancelled(true);
        }
        break;
      case LEFT:
      case RIGHT:
        if (top
            && event.getView().getTopInventory().getType() == InventoryType.ANVIL
            && ItemUtil.isUniqueItem(event.getCursor())) {
          event.setCancelled(true);
        }
        break;
      case SHIFT_LEFT:
      case SHIFT_RIGHT:
        if (!top
            && event.getCurrentItem() != null
            && event.getCurrentItem().getType() != Material.AIR
            && event.getView().getTopInventory().getType() == InventoryType.ANVIL
            && ItemUtil.isUniqueItem(event.getCurrentItem())) {
          event.setCancelled(true);
        }
        break;
      default:
        break;
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryDrag(InventoryDragEvent event) {
    if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
        && ItemUtil.isUniqueItem(event.getOldCursor())) {
      event.setCancelled(true);
    }
  }
}
