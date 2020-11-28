package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreContexts;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class HealCommand extends BaseCommand {

  @Dependency EasterlynCore core;

  @CommandAlias("heal")
  @Description("{@@sink.module.heal.description}")
  @CommandPermission("easterlyn.command.heal.self")
  @CommandCompletion("@player")
  @Syntax("[player]")
  public void heal(@Flags(CoreContexts.ONLINE_WITH_PERM) Player player) {
    AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
    CommandIssuer issuer = getCurrentCommandIssuer();
    if (attribute == null) {
      core.getLocaleManager().sendMessage(issuer.getIssuer(), "sink.module.heal.attribute_missing");
      return;
    }
    player.setHealth(attribute.getValue());
    core.getLocaleManager().sendMessage(player, "sink.module.heal.success.target");
    if (!issuer.getUniqueId().equals(player.getUniqueId())) {
      core.getLocaleManager()
          .sendMessage(
              issuer.getIssuer(), "sink.module.heal.success.other", "{target}", player.getName());
    }
  }

  @CommandAlias("feed")
  @Description("{@@sink.module.feed.description}")
  @CommandPermission("easterlyn.command.feed.self")
  @CommandCompletion("@player")
  @Syntax("[player]")
  public void feed(@Flags(CoreContexts.ONLINE_WITH_PERM) Player player) {
    player.setFoodLevel(20);
    player.setSaturation(20);
    core.getLocaleManager().sendMessage(player, "sink.module.feed.success.target");
    CommandIssuer issuer = getCurrentCommandIssuer();
    if (!issuer.getUniqueId().equals(player.getUniqueId())) {
      core.getLocaleManager()
          .sendMessage(
              issuer.getIssuer(), "sink.module.feed.success.other", "{target}", player.getName());
    }
  }
}
