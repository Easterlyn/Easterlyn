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
import com.easterlyn.EasterlynKitchenSink;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.user.PlayerUser;
import com.easterlyn.util.Request;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class TeleportRequestCommand extends BaseCommand {

  public static final String TPREQUEST_COOLDOWN = "kitchensink.tprequest";
  private static final String CONFIG_ACCEPT = "command.teleportrequest.accept";
  private static final String CONFIG_IGNORE = "command.teleportrequest.ignore";
  @Dependency EasterlynCore core;
  @Dependency EasterlynKitchenSink sink;

  @CommandAlias("tpa|tpask|tprequest")
  @Description("{@@sink.module.tprequest.to.description}")
  @CommandPermission("easterlyn.command.tpa")
  @Syntax("<player>")
  @CommandCompletion("@player")
  public void teleportRequest(
      @NotNull @Flags(CoreContexts.SELF) PlayerUser issuer,
      @NotNull @Flags(CoreContexts.ONLINE) PlayerUser target) {
    tpRequest(issuer, target, true);
  }

  @CommandAlias("tpahere|tpaskhere|call|callhere")
  @Description("{@@sink.module.tprequest.pull.description}")
  @CommandPermission("easterlyn.command.tpa")
  @Syntax("<player>")
  @CommandCompletion("@player")
  public void teleportHereRequest(
      @NotNull @Flags(CoreContexts.SELF) PlayerUser issuer,
      @NotNull @Flags(CoreContexts.ONLINE) PlayerUser target) {
    tpRequest(issuer, target, false);
  }

  private void tpRequest(@NotNull PlayerUser issuer, @NotNull PlayerUser requested, boolean to) {
    long nextTPA = issuer.getStorage().getLong(TPREQUEST_COOLDOWN);
    if (nextTPA > System.currentTimeMillis()) {
      SimpleDateFormat format = new SimpleDateFormat("m:ss");
      core.getLocaleManager()
          .sendMessage(
              issuer.getPlayer(),
              "sink.module.tprequest.error.cooldown",
              "{value}",
              format.format(new Date(nextTPA - System.currentTimeMillis())));
      return;
    }

    issuer
        .getStorage()
        .set(
            TPREQUEST_COOLDOWN,
            System.currentTimeMillis() + sink.getConfig().getLong(CONFIG_IGNORE));

    Player issuingPlayer = issuer.getPlayer();
    Player requestedPlayer = requested.getPlayer();

    if (issuingPlayer == null || requestedPlayer == null) {
      core.getLocaleManager().sendMessage(issuingPlayer, "sink.module.tprequest.error.offline");
      return;
    }

    if (requested.setPendingRequest(
        new Request() {
          @Override
          public void accept() {
            Player localIssuer = issuer.getPlayer();
            Player localRequested = requested.getPlayer();
            if (localIssuer == null || localRequested == null) {
              core.getLocaleManager()
                  .sendMessage(localRequested, "sink.module.tprequest.error.offline");
              return;
            }
            Player destination = to ? localRequested : localIssuer;
            Player teleportee = to ? localIssuer : localRequested;
            if (teleportee.teleport(
                destination.getLocation().add(0, 0.1, 0),
                PlayerTeleportEvent.TeleportCause.PLUGIN)) {
              core.getLocaleManager()
                  .sendMessage(localRequested, "sink.module.tprequest.common.accept");
              core.getLocaleManager()
                  .sendMessage(
                      localIssuer,
                      "sink.module.tprequest.common.accepted",
                      "{value}",
                      localRequested.getName());
            }
          }

          @Override
          public void decline() {
            core.getLocaleManager()
                .sendMessage(requestedPlayer, "sink.module.tprequest.common.decline");
            core.getLocaleManager()
                .sendMessage(
                    issuingPlayer,
                    "sink.module.tprequest.common.declined",
                    "{value}",
                    requestedPlayer.getName());
            issuer
                .getStorage()
                .set(
                    TPREQUEST_COOLDOWN,
                    issuer.getStorage().getLong(TPREQUEST_COOLDOWN)
                        - sink.getConfig().getLong(CONFIG_IGNORE)
                        + sink.getConfig().getLong(CONFIG_ACCEPT));
          }
        })) {
      core.getLocaleManager().sendMessage(issuingPlayer, "sink.module.tprequest.common.issued");
      String requestMessage =
          core.getLocaleManager()
              .getValue(
                  to ? "sink.module.tprequest.to.request" : "sink.module.tprequest.pull.request",
                  core.getLocaleManager().getLocale(requestedPlayer),
                  "{value}",
                  issuingPlayer.getName());
      if (requestMessage == null) {
        ReportableEvent.call("Missing required translation " + (to ? "sink.module.tprequest.to.request" : "sink.module.tprequest.pull.request"));
        issuer.sendMessage("The translation for your TP request is missing. Please notify your target manually.");
        return;
      }
      requested.sendMessage(requestMessage);
    } else {
      core.getLocaleManager()
          .sendMessage(
              issuingPlayer,
              "sink.module.tprequest.error.popular",
              "{value}",
              requestedPlayer.getName());
    }
  }

}
