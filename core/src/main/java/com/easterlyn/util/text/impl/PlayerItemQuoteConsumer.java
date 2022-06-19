package com.easterlyn.util.text.impl;

import com.easterlyn.util.inventory.ItemUtil;
import com.easterlyn.util.text.ParsedText;
import com.easterlyn.util.text.StaticQuoteConsumer;
import com.github.jikoo.planarwrappers.util.StringConverters;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerItemQuoteConsumer extends StaticQuoteConsumer {

  private final Player player;

  public PlayerItemQuoteConsumer(Player player) {
    super(Pattern.compile("\\{ITEM:(\\w+)}"));
    this.player = player;
  }
  @Override
  public void addComponents(
      @NotNull ParsedText components,
      @NotNull Supplier<Matcher> matcherSupplier) {
    ItemStack itemStack = getItem(matcherSupplier.get().group(1));
    if (itemStack == null) {
      itemStack = ItemUtil.AIR;
    }

    components.addComponents(
        new TextComponent("["),
        ItemUtil.getItemComponent(itemStack),
        new TextComponent("]"));
  }

  private @Nullable ItemStack getItem(String slotName) {
    EquipmentSlot equipmentSlot = StringConverters.toEnum(EquipmentSlot.class, slotName);
    if (equipmentSlot != null) {
      return player.getInventory().getItem(equipmentSlot);
    }

    try {
      int slotId = Integer.parseInt(slotName);

      if (slotId < 0 || slotId >= player.getInventory().getSize()) {
        return null;
      }

      return player.getInventory().getItem(slotId);

    } catch (NumberFormatException e) {
      return null;
    }
  }

}
