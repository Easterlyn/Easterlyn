package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreContexts;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeleportHereCommand extends BaseCommand {

  @Dependency EasterlynCore core;

  @CommandAlias("tphere")
  @Description("{@@sink.module.tphere.description}")
  @Syntax("<target>")
  @CommandCompletion("@player")
  @CommandPermission("easterlyn.command.tphere")
  public void teleportHere(
      @NotNull @Flags(CoreContexts.SELF) Player issuer,
      @NotNull @Flags(CoreContexts.ONLINE) Player target) {
    target.teleport(issuer);
    core.getLocaleManager()
        .sendMessage(target, "sink.module.tphere.target", "{value}", issuer.getName());
    core.getLocaleManager()
        .sendMessage(issuer, "sink.module.tphere.other", "{value}", target.getName());
  }

}
