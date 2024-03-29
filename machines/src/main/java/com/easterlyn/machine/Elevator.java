package com.easterlyn.machine;

import com.easterlyn.EasterlynMachines;
import com.easterlyn.util.ProtectionUtil;
import com.easterlyn.util.inventory.Button;
import com.easterlyn.util.inventory.SimpleUI;
import com.github.jikoo.planarwrappers.util.Generics;
import com.github.jikoo.planarwrappers.world.Shape;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

/**
 * A machine providing brief fast levitation. power 20 * 1 second = 19 blocks up.
 *
 * @author Jikoo
 */
public class Elevator extends Machine {

  private final ItemStack drop;

  public Elevator(EasterlynMachines machines) {
    super(machines, new Shape(), "Elevator");

    getShape().set(0, 0, 0, Material.PURPUR_PILLAR);
    getShape().set(0, 1, 0, Material.HEAVY_WEIGHTED_PRESSURE_PLATE);

    drop = new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
    Generics.consumeAs(
        ItemMeta.class,
        drop.getItemMeta(),
        itemMeta -> {
          itemMeta.setDisplayName(ChatColor.WHITE + "Elevator");
          drop.setItemMeta(itemMeta);
        });

    ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(machines, "elevator"), drop);
    recipe.shape("BDB", "ACA", "BAB");
    recipe.setIngredient('A', Material.ENDER_EYE);
    recipe.setIngredient('B', Material.PHANTOM_MEMBRANE);
    recipe.setIngredient(
        'C', new RecipeChoice.MaterialChoice(Material.PURPUR_BLOCK, Material.PURPUR_PILLAR));
    recipe.setIngredient('D', Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
    machines.getServer().addRecipe(recipe);
  }

  private int getCurrentBoost(ConfigurationSection storage) {
    return storage.getInt("duration", 3);
  }

  private int adjustBlockBoost(ConfigurationSection storage, int difference) {
    int currentBoost = getCurrentBoost(storage);
    int boost = Math.min(50, Math.max(3, currentBoost + difference));
    if (currentBoost != boost) {
      storage.set("duration", boost);
    }
    return boost;
  }

  @Override
  public void handleInteract(
      @NotNull PlayerInteractEvent event, @NotNull ConfigurationSection storage) {
    super.handleInteract(event, storage);
    if (event.useInteractedBlock() == Event.Result.DENY) {
      return;
    }

    Player player = event.getPlayer();
    // Allow sneaking players to cross or place blocks
    if (player.isSneaking()) {
      return;
    }
    if (event.getClickedBlock() == null) {
      return;
    }
    if (event.getAction() == Action.PHYSICAL) {
      event
          .getClickedBlock()
          .getWorld()
          .playSound(
              event.getClickedBlock().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.2F, 0F);
      int duration = storage.getInt("duration");
      // Effect power is 0-indexed.
      player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, 19, true));
      PermissionAttachment attachment = player.addAttachment(getMachines(), (int) (duration * 1.2));
      if (attachment != null) {
        attachment.setPermission("nocheatplus.checks.moving.creativefly", true);
      }
      return;
    }
    Location interacted = event.getClickedBlock().getLocation();
    if (!ProtectionUtil.canOpenChestsAt(player, interacted)) {
      player.sendMessage(ChatColor.RED + "You do not have permission to adjust elevators here!");
      return;
    }
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      event.getPlayer().openInventory(getInventory(storage));
    }
  }

  @Override
  public double getCost() {
    return 200;
  }

  @Override
  public @NotNull ItemStack getUniqueDrop() {
    return drop;
  }

  private @NotNull Inventory getInventory(@NotNull ConfigurationSection storage) {
    SimpleUI ui = new SimpleUI(getMachines(), "Densificator Configuration");
    ItemStack itemStack1 = new ItemStack(Material.RED_WOOL);
    Generics.consumeAs(
        ItemMeta.class,
        itemStack1.getItemMeta(),
        itemMeta -> {
          itemMeta.setDisplayName(ChatColor.WHITE + "Decrease Boost");
          itemStack1.setItemMeta(itemMeta);
        });
    ui.setButton(
        3,
        new Button(
            itemStack1,
            event -> {
              int amount = adjustBlockBoost(storage, -1);
              Button display = ui.getButton(4);
              if (display != null) {
                // Item is not cloned, this is fine.
                display.item().setAmount(amount);
              }
              ui.draw(event.getView().getTopInventory());
            }));
    ItemStack itemStack2 = new ItemStack(Material.ELYTRA);
    itemStack2.setAmount(getCurrentBoost(storage));
    Generics.consumeAs(
        ItemMeta.class,
        itemStack2.getItemMeta(),
        itemMeta -> {
          itemMeta.setDisplayName(ChatColor.WHITE + "Ticks of Boost");
          itemMeta.setLore(
              Arrays.asList(
                  ChatColor.WHITE + "1 tick = 1/20 second",
                  ChatColor.WHITE + "Roughly 1 block/tick"));
          itemStack2.setItemMeta(itemMeta);
        });
    ui.setButton(4, new Button(itemStack2, event -> {}));
    ItemStack itemStack3 = new ItemStack(Material.LIME_WOOL);
    Generics.consumeAs(
        ItemMeta.class,
        itemStack3.getItemMeta(),
        itemMeta -> {
          itemMeta.setDisplayName(ChatColor.WHITE + "Increase Boost");
          itemStack3.setItemMeta(itemMeta);
        });
    ui.setButton(
        5,
        new Button(
            itemStack3,
            event -> {
              int amount = adjustBlockBoost(storage, 1);
              Button display = ui.getButton(4);
              if (display != null) {
                display.item().setAmount(amount);
              }
              ui.draw(event.getView().getTopInventory());
            }));
    return ui.getInventory();
  }
}
