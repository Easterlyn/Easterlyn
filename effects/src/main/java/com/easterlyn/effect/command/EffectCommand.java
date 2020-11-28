package com.easterlyn.effect.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import co.aikar.locales.MessageKey;
import com.easterlyn.EasterlynEffects;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.util.NumberUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EffectCommand extends BaseCommand {

  @Dependency private EasterlynEffects effects;

  @CommandAlias("effects|fx")
  @Description("{@@effects.commands.effect.description}")
  @CommandPermission("easterlyn.command.effects")
  @Syntax("<effect> <level>")
  @CommandCompletion("@effect @integer")
  public void applyEffect(@Flags(CoreContexts.SELF) Player player, String effectName, int level) {
    ItemStack hand = player.getInventory().getItemInMainHand();
    if (hand.getType() == Material.AIR) {
      getCurrentCommandIssuer().sendInfo(MessageKey.of("core.common.no_item"));
      return;
    }

    ItemMeta meta = hand.getItemMeta();

    if (meta == null) {
      getCurrentCommandIssuer().sendInfo(MessageKey.of("core.common.no_item"));
      return;
    }

    String loreString = ChatColor.GRAY + effectName + ' ' + NumberUtil.romanFromInt(level);

    if (effects.getEffectFromLore(loreString, true) == null) {
      getCurrentCommandIssuer()
          .sendInfo(MessageKey.of("effects.commands.effect.invalid"), "{value}", effectName);
      return;
    }

    List<String> lore = meta.getLore();
    if (lore == null) {
      lore = new ArrayList<>();
    }

    meta.setLore(effects.organizeEffectLore(lore, true, true, false, loreString));
    hand.setItemMeta(meta);
    getCurrentCommandIssuer()
        .sendInfo(MessageKey.of("effects.commands.effect.success"), "{value}", loreString);
  }
}
