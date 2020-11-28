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
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.kitchensink.combo.BackCommand;
import com.easterlyn.kitchensink.combo.DeathPointCommand;
import com.easterlyn.user.User;

@CommandAlias("resetcooldown|rcd")
@Description("{@@sink.module.cooldown.reset.description}")
@CommandPermission("easterlyn.command.cooldownreset")
public class CooldownReset extends BaseCommand {

  @Dependency EasterlynCore core;

  @Default
  @Private
  public void resetAll(@Flags(CoreContexts.ONLINE_WITH_PERM) User user) {
    core.getLocaleManager().sendMessage(user.getPlayer(), "sink.module.cooldown.reset.all");
    user.getStorage().set(BackCommand.BACK_COOLDOWN, null);
    user.getStorage().set(DeathPointCommand.DEATH_COOLDOWN, null);
    user.getStorage().set(TeleportRequestCommand.TPREQUEST_COOLDOWN, null);
  }

  @Subcommand("back")
  @Description("{@@sink.module.cooldown.reset.specific.description}")
  @Syntax("")
  @CommandCompletion("@player")
  public void resetBack(@Flags(CoreContexts.ONLINE_WITH_PERM) User user) {
    core.getLocaleManager()
        .sendMessage(user.getPlayer(), "sink.module.cooldown.reset.single", "{target}", "/back");
    user.getStorage().set(BackCommand.BACK_COOLDOWN, null);
  }

  @Subcommand("death")
  @Description("{@@sink.module.cooldown.reset.specific.description}")
  @Syntax("")
  @CommandCompletion("@player")
  public void resetDeath(@Flags(CoreContexts.ONLINE_WITH_PERM) User user) {
    core.getLocaleManager()
        .sendMessage(user.getPlayer(), "sink.module.cooldown.reset.single", "{target}", "/death");
    user.getStorage().set(DeathPointCommand.DEATH_COOLDOWN, null);
  }

  @Subcommand("tpa")
  @Description("{@@sink.module.cooldown.reset.specific.description}")
  @Syntax("")
  @CommandCompletion("@player")
  public void resetTpa(@Flags(CoreContexts.ONLINE_WITH_PERM) User user) {
    core.getLocaleManager()
        .sendMessage(user.getPlayer(), "sink.module.cooldown.reset.single", "{target}", "/tpa");
    user.getStorage().set(TeleportRequestCommand.TPREQUEST_COOLDOWN, null);
  }
}
