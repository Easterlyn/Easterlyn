package com.easterlyn.kitchensink.listener;

import com.easterlyn.event.ReportableEvent;
import com.github.jikoo.planarenchanting.anvil.AnvilOperation;
import com.github.jikoo.planarenchanting.anvil.AnvilResult;
import com.github.jikoo.planarenchanting.table.Enchantability;
import com.github.jikoo.planarenchanting.table.EnchantingTable;
import com.github.jikoo.planarenchanting.table.TableEnchantListener;
import com.github.jikoo.planarenchanting.util.ItemUtil;
import com.github.jikoo.planarwrappers.util.Generics;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FortuneShears extends TableEnchantListener {

  // Silk touch because heck you. Also lower enchantability for the same reason.
  private final EnchantingTable table = new EnchantingTable(
      Set.of(Enchantment.DURABILITY, Enchantment.LOOT_BONUS_BLOCKS, Enchantment.SILK_TOUCH),
      Enchantability.STONE);
  private final Plugin plugin;

  public FortuneShears(Plugin plugin) {
    super(plugin);
    this.plugin = plugin;
  }

  @Override
  protected boolean isIneligible(@NotNull Player player, @NotNull ItemStack enchanted) {
    return enchanted.getType() != Material.SHEARS;
  }

  @Override
  protected @NotNull EnchantingTable getTable(
      @NotNull Player player,
      @NotNull ItemStack enchanted) {
    return table;
  }

  @EventHandler
  void onPrepareAnvil(@NotNull PrepareAnvilEvent event) {
    var clicker = event.getView().getPlayer();
    var inventory = event.getInventory();
    var base = inventory.getItem(0);
    var addition = inventory.getItem(1);

    if (areItemsInvalid(base, addition)) {
      return;
    }

    var operation = new AnvilOperation();
    operation.setEnchantApplies(
        (enchantment, itemStack) ->
            enchantment.getItemTarget().includes(itemStack)
                || enchantment.equals(Enchantment.LOOT_BONUS_BLOCKS));
    final var result = operation.apply(inventory);

    if (result == AnvilResult.EMPTY) {
      return;
    }

    final var input = base.clone();
    final var input2 = addition.clone();
    final var resultItem = result.item();

    event.setResult(resultItem);

    plugin.getServer().getScheduler().runTask(plugin, () -> {
      // Ensure inputs have not been modified since our calculations.
      if (!input.equals(inventory.getItem(0)) || !input2.equals(inventory.getItem(1))) {
        return;
      }

      // Set result again - overrides bad enchantment plugins that always write result.
      inventory.setItem(2, resultItem);
      // Set repair cost. As vanilla has no result for our combinations, this is always set to 0
      // after the event has completed and needs to be set again.
      inventory.setRepairCost(result.levelCost());
      // Update level cost window property again just to be safe.
      clicker.setWindowProperty(InventoryView.Property.REPAIR_COST, result.levelCost());
    });
  }

  @Contract("null, _ -> true; _, null -> true")
  private boolean areItemsInvalid(@Nullable ItemStack base, @Nullable ItemStack addition) {
    return ItemUtil.isEmpty(base)
        || ItemUtil.isEmpty(addition)
        || base.getType() != Material.SHEARS;
  }

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
