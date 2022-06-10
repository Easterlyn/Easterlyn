package com.easterlyn.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Syntax;
import co.aikar.locales.MessageKey;
import com.easterlyn.event.ReportableEvent;
import org.bukkit.Location;

@CommandAlias("report|mail @staff")
@Description("{@@core.commands.report.description}")
@CommandPermission("easterlyn.command.report")
public class ReportCommand extends BaseCommand {

  @Default
  @Private
  @Syntax("<descriptive details>")
  public void report(BukkitCommandIssuer issuer, String args) {
    if (args.indexOf(' ') == -1) {
      issuer.sendInfo(MessageKey.of("core.commands.report.error.length"));
      return;
    }

    String message = "Report by " + issuer.getIssuer().getName();
    if (issuer.isPlayer()) {
      Location location = issuer.getPlayer().getLocation();
      message +=
          String.format(
              " - /wtp @p %s %.2f %.2f %.2f %.2f %.2f",
              issuer.getPlayer().getWorld().getName(),
              location.getX(),
              location.getY(),
              location.getZ(),
              location.getPitch(),
              location.getYaw());
    }
    message += '\n' + args;

    if (ReportableEvent.getHandlerList().getRegisteredListeners().length <= 1) {
      // Default report logging listener doesn't count
      issuer.sendInfo(MessageKey.of("core.commands.report.error.no_handlers"));
    } else {
      issuer.sendInfo(MessageKey.of("core.commands.report.success"));
    }

    ReportableEvent.call(message);
  }
}
