package com.easterlyn.command;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import com.easterlyn.util.PlayerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerContextResolver implements
      IssuerAwareContextResolver<Player, BukkitCommandExecutionContext> {

  @Override
  public Player getContext(@NotNull BukkitCommandExecutionContext context)
      throws InvalidCommandArgument {
    if (context.hasFlag(CoreContexts.SELF)) {
      return getSelf(context.getIssuer());
    }

    if (context.hasFlag(CoreContexts.ONLINE_WITH_PERM) && context.getIssuer().isPlayer()) {
      boolean hasOtherPerm = false;
      for (Object permissionObj : context.getCmd().getRequiredPermissions()) {
        String permission = permissionObj.toString();
        if (permission.endsWith(".self")
            && context.getIssuer().hasPermission(permission.substring(0, permission.length() - 5) + ".other")) {
          hasOtherPerm = true;
          break;
        }
      }

      if (!hasOtherPerm) {
        return getSelf(context.getIssuer());
      }
    }

    if (context.hasFlag(CoreContexts.ONLINE)) {
      return getOnline(context.getIssuer(), context.popFirstArg());
    }

    // Default/ONLINE_WITH_PERM behavior: Attempt to match online, fall through to self.
    Player player = null;
    String firstArg = context.getFirstArg();
    if (firstArg != null && firstArg.length() > 3) {
      try {
        player = getOnline(context.getIssuer(), firstArg);
      } catch (InvalidCommandArgument ignored) {
        // If user is not specified, fall through to self.
      }
    }

    if (player != null) {
      context.popFirstArg();
      return player;
    }

    return getSelf(context.getIssuer());
  }

  private @NotNull Player getSelf(@NotNull BukkitCommandIssuer issuer)
      throws InvalidCommandArgument {
    if (issuer.isPlayer()) {
      return issuer.getPlayer();
    }
    throw new InvalidCommandArgument(CoreLang.NO_CONSOLE);
  }

  private @NotNull Player getOnline(
      @NotNull BukkitCommandIssuer issuer, @Nullable String argument)
      throws InvalidCommandArgument {
    if (argument == null) {
      throw new InvalidCommandArgument(CoreLang.INVALID_PLAYER, "{value}", "null");
    }
    Player player = PlayerUtil.matchOnlinePlayer(issuer.getIssuer(), argument);
    if (player == null) {
      throw new InvalidCommandArgument(CoreLang.INVALID_PLAYER, "{value}", argument);
    }
    return player;
  }

}