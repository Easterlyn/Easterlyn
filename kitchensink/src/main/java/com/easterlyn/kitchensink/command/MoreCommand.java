package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreContexts;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("more")
@Description("{@@sink.module.more.description}")
@CommandPermission("easterlyn.command.more")
public class MoreCommand extends BaseCommand {

  @Dependency EasterlynCore core;

  @Default
  @Private
  public void more(@Flags(CoreContexts.SELF) Player player) {
    more(player, 0);
  }

  @CommandAlias("more")
  @Description("{@@sink.module.more.description}")
  @Syntax("[count]")
  @CommandCompletion("@integer")
  public void more(@Flags(CoreContexts.SELF) Player player, int count) {
    ItemStack hand = player.getInventory().getItemInMainHand();
    if (hand.getType() == Material.AIR) {
      showSyntax(
          getCurrentCommandIssuer(), getLastCommandOperationContext().getRegisteredCommand());
      return;
    }
    if (count < 1 || hand.getAmount() + count > 64) {
      hand.setAmount(64);
    } else {
      hand.setAmount(hand.getAmount() + count);
    }
    core.getLocaleManager()
        .sendMessage(
            player, "sink.module.more.success", "{value}", String.valueOf(hand.getAmount()));
  }
}
