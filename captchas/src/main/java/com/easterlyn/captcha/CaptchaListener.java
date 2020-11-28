package com.easterlyn.captcha;

import com.easterlyn.EasterlynCaptchas;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.util.BlockUtil;
import com.easterlyn.util.inventory.ItemUtil;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class CaptchaListener implements Listener {

  private final EasterlynCaptchas captcha;

  public CaptchaListener(EasterlynCaptchas captcha) {
    this.captcha = captcha;
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void handleCaptcha(InventoryClickEvent event) {
    boolean hotbar = false;
    switch (event.getClick()) {
      case NUMBER_KEY:
        hotbar = true;
        break;
      case LEFT:
      case RIGHT:
        if (event.getCursor() == null
            || event.getCursor().getType() == Material.AIR
            || event.getCurrentItem() == null
            || event.getCurrentItem().getType() == Material.AIR) {
          return;
        }
        break;
      case CONTROL_DROP:
      case CREATIVE:
      case DOUBLE_CLICK:
      case DROP:
      case MIDDLE:
      case SHIFT_LEFT:
      case SHIFT_RIGHT:
      case WINDOW_BORDER_LEFT:
      case WINDOW_BORDER_RIGHT:
      case UNKNOWN:
      default:
        return;
    }
    ItemStack blankCaptcha;
    ItemStack toCaptcha;
    if (hotbar) {
      blankCaptcha = event.getView().getBottomInventory().getItem(event.getHotbarButton());
      toCaptcha = event.getCurrentItem();
    } else {
      blankCaptcha = event.getCurrentItem();
      toCaptcha = event.getCursor();
    }

    if (blankCaptcha == null
        || toCaptcha == null
        || !EasterlynCaptchas.isBlankCaptcha(blankCaptcha)
        || captcha.canNotCaptcha(toCaptcha)
        || EasterlynCaptchas.isBlankCaptcha(toCaptcha)) {
      return;
    }

    ItemStack captchaItem = captcha.getCaptchaForItem(toCaptcha);
    event.setResult(Event.Result.DENY);

    if (captchaItem == null) {
      return;
    }

    // Decrement captcha stack
    if (hotbar) {
      event
          .getView()
          .getBottomInventory()
          .setItem(event.getHotbarButton(), ItemUtil.decrement(blankCaptcha, 1));
      event.setCurrentItem(null);
    } else {
      event.setCurrentItem(ItemUtil.decrement(blankCaptcha, 1));
      //noinspection deprecation // No alternative. Functions fine due to inventory update.
      event.setCursor(null);
    }

    // Add to bottom inventory first
    int leftover =
        ItemUtil.getAddFailures(event.getView().getBottomInventory().addItem(captchaItem));
    if (leftover > 0) {
      // Add to top, bottom was full.
      leftover = ItemUtil.getAddFailures(event.getView().getTopInventory().addItem(captchaItem));
    }
    if (leftover > 0) {
      if (hotbar) {
        // Drop rather than delete (Items can be picked up before event completes, thanks Bukkit.)
        event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), captchaItem);
      } else {
        // Set cursor to captcha
        //noinspection deprecation
        event.setCursor(captchaItem);
      }
    }
    ((Player) event.getWhoClicked()).updateInventory();
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_AIR
        && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    ItemStack hand = ItemUtil.getHeldItem(event);
    if (!EasterlynCaptchas.isUsedCaptcha(hand) || BlockUtil.hasRightClickFunction(event)) {
      return;
    }

    ItemStack captchaStack = captcha.getItemByCaptcha(hand);
    if (captchaStack == null || captchaStack.isSimilar(hand)) {
      String hash = EasterlynCaptchas.getHashFromCaptcha(hand);
      ReportableEvent.call(
          "Invalid captcha belonging to "
              + event.getPlayer().getName()
              + ": "
              + (hash == null ? ItemUtil.getAsText(hand) : hash));
      return;
    }

    ItemUtil.decrementHeldItem(event, 1);
    if (ItemUtil.hasSpaceFor(captchaStack, event.getPlayer().getInventory())) {
      event.getPlayer().getInventory().addItem(captchaStack);
    } else {
      event
          .getPlayer()
          .getWorld()
          .dropItem(event.getPlayer().getEyeLocation(), captchaStack)
          .setVelocity(event.getPlayer().getLocation().getDirection().multiply(0.4));
    }

    event.getPlayer().updateInventory();
  }

  @EventHandler
  public void onPrepareItemCraft(PrepareItemCraftEvent event) {
    if (event.getRecipe() instanceof Keyed
        && ((Keyed) event.getRecipe()).getKey().getKey().equals(EasterlynCaptchas.RECIPE_KEY)) {
      for (ItemStack itemStack : event.getInventory().getMatrix()) {
        //noinspection ConstantConditions
        if (itemStack == null || itemStack.getType() == Material.AIR) {
          continue;
        }
        if (!EasterlynCaptchas.isUsedCaptcha(itemStack)) {
          event.getInventory().setResult(ItemUtil.AIR);
        } else {
          event.getInventory().setResult(captcha.getItemByCaptcha(itemStack));
        }
        return;
      }
    }
  }

  @EventHandler
  public void onItemCraft(CraftItemEvent event) {
    if (event.getRecipe() instanceof Keyed
        && ((Keyed) event.getRecipe()).getKey().getKey().equals(EasterlynCaptchas.RECIPE_KEY)) {
      for (ItemStack itemStack : event.getInventory().getMatrix()) {
        //noinspection ConstantConditions
        if (itemStack == null || itemStack.getType() == Material.AIR) {
          continue;
        }
        if (!EasterlynCaptchas.isUsedCaptcha(itemStack)) {
          event.setCurrentItem(ItemUtil.AIR);
        } else {
          event.setCurrentItem(captcha.getItemByCaptcha(itemStack));
        }
        return;
      }
    }
  }

  @EventHandler
  public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
    if (EasterlynCaptchas.isCaptcha(event.getItem())) {
      event.setCancelled(true);
    }
  }
}
