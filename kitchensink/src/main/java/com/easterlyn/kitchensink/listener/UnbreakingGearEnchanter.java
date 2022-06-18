package com.easterlyn.kitchensink.listener;

import com.github.jikoo.planarenchanting.table.Enchantability;
import com.github.jikoo.planarenchanting.table.EnchantingTable;
import com.github.jikoo.planarenchanting.table.TableEnchantListener;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnbreakingGearEnchanter extends TableEnchantListener {

  private final EnchantingTable table = new EnchantingTable(
      Set.of(Enchantment.DURABILITY),
      Enchantability.BOOK);

  public UnbreakingGearEnchanter(@NotNull Plugin plugin) {
    super(plugin);
  }

  @Override
  protected boolean isIneligible(@NotNull Player player, @NotNull ItemStack enchanted) {
    Material type = enchanted.getType();
    return type != Material.ELYTRA && type != Material.SHIELD;
  }

  @Override
  protected @Nullable EnchantingTable getTable(@NotNull Player player,
      @NotNull ItemStack enchanted) {
    return table;
  }

}
