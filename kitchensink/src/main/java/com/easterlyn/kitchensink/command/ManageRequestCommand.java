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
import com.easterlyn.user.PlayerUser;
import com.easterlyn.util.Request;
import org.jetbrains.annotations.NotNull;

public class ManageRequestCommand extends BaseCommand {

  @Dependency EasterlynCore core;

  @CommandAlias("accept|yes|y")
  @Description("{@@sink.module.request.accept.description}")
  @CommandCompletion("")
  @Syntax("")
  @CommandPermission("easterlyn.command.request")
  public void accept(@NotNull @Flags(CoreContexts.SELF) PlayerUser user) {
    Request request = user.pollPendingRequest();
    if (request == null) {
      core.getLocaleManager().sendMessage(user.getPlayer(), "sink.module.request.error.no_pending");
      return;
    }
    request.accept();
  }

  @CommandAlias("decline|no|n")
  @Description("{@@sink.module.request.decline.description}")
  @CommandCompletion("")
  @Syntax("")
  @CommandPermission("easterlyn.command.request")
  public void decline(@NotNull @Flags(CoreContexts.SELF) PlayerUser user) {
    Request request = user.pollPendingRequest();
    if (request == null) {
      core.getLocaleManager().sendMessage(user.getPlayer(), "sink.module.request.error.no_pending");
      return;
    }
    request.decline();
  }

}
