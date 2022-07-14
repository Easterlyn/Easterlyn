package com.easterlyn.command;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import com.easterlyn.EasterlynCore;
import com.easterlyn.util.PlayerUtil;
import com.easterlyn.util.wrapper.PlayerFuture;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class PlayerFutureContextResolver implements
    IssuerAwareContextResolver<PlayerFuture, BukkitCommandExecutionContext> {

  private final EasterlynCore plugin;

  public PlayerFutureContextResolver(EasterlynCore plugin) {
    this.plugin = plugin;
  }

  @Override
  public PlayerFuture getContext(
      @NotNull BukkitCommandExecutionContext context)
      throws InvalidCommandArgument {
    String argument = context.popFirstArg();
    CommandSender issuer = context.getIssuer().getIssuer();
    if (argument == null) {
      throw new InvalidCommandArgument(CoreLang.INVALID_PLAYER, "{value}", "null");
    }
    return new PlayerFuture(argument, PlayerUtil.matchPlayer(plugin, issuer, argument, true));
  }

}
