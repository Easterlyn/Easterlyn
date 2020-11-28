package com.easterlyn.kitchensink.listener;

import com.easterlyn.user.UserRank;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.inventory.ItemUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;

public class RestrictCreativeItems implements Listener {

  public RestrictCreativeItems() {
    PermissionUtil.addParent(
        "easterlyn.events.creative.unfiltered", UserRank.MODERATOR.getPermission());
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryCreative(InventoryCreativeEvent event) {
    if (event.getWhoClicked().hasPermission("easterlyn.events.creative.unfiltered")) {
      return;
    }

    if (event.getCursor().getType() == Material.AIR) {
      return;
    }

    /* TODO blacklist
    if (blacklist.contains(event.getCursor().getType())) {
      event.setCancelled(true);
      return;
    }*/

    ItemStack cleanedItem = ItemUtil.cleanNBT(event.getCursor());

    if (cleanedItem == event.getCursor()) {
      return;
    }

    // No overstacking, no negative amounts
    if (cleanedItem.getAmount() > cleanedItem.getMaxStackSize()) {
      cleanedItem.setAmount(cleanedItem.getMaxStackSize());
    } else if (cleanedItem.getAmount() < 1) {
      cleanedItem.setAmount(1);
    }

    event.setCursor(cleanedItem);
  }

  /* TODO
  *  - BlockPlaceEvent
  *  - PrepareItemCraftEvent
  *  - CraftItemEvent
  *  - ItemPickupEvent
  *  - ItemDropEvent
  * creativeBlacklist = EnumSet.of(BARRIER, BEACON, BEDROCK,
       COMMAND_BLOCK, CHAIN_COMMAND_BLOCK, COMMAND_BLOCK_MINECART, REPEATING_COMMAND_BLOCK,
       END_CRYSTAL, END_GATEWAY, END_PORTAL, END_PORTAL_FRAME, TNT_MINECART, FIRE, SPAWNER,
       NETHER_PORTAL, STRUCTURE_BLOCK, STRUCTURE_VOID, TNT)
  */

}
