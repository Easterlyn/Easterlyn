package com.easterlyn.spectators.command;

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
import com.easterlyn.EasterlynSpectators;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.user.User;
import com.easterlyn.util.Request;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

@CommandAlias("spectate|spec")
@CommandPermission("easterlyn.command.spectate")
@Description("{@@spectate.description}")
public class SpectateCommand extends BaseCommand {

  @Dependency EasterlynCore core;

  @Default
  @Private
  public void spectate(@Flags(CoreContexts.SELF) User user) {
    Player player = user.getPlayer();
    if (player == null) {
      core.getLocaleManager().sendMessage(user.getPlayer(), "spectate.not_online");
      return;
    }

    Location spectateReturn =
        user.getStorage().getSerializable(EasterlynSpectators.USER_SPECTATE_RETURN, Location.class);

    if (spectateReturn == null) {
      if (player.getGameMode() != GameMode.SURVIVAL) {
        core.getLocaleManager().sendMessage(user.getPlayer(), "spectate.not_survival");
        return;
      }
      player.setGameMode(GameMode.SPECTATOR);
      user.getStorage().set(EasterlynSpectators.USER_SPECTATE_RETURN, player.getLocation());
      String rawOptions =
          core.getLocaleManager()
              .getValue(
                  "spectate.exit_mortal_coil", core.getLocaleManager().getLocale(user.getPlayer()));
      String[] options = rawOptions != null ? rawOptions.split("\n") : new String[] {""};
      String selected = options[ThreadLocalRandom.current().nextInt(options.length)];
      user.sendMessage(selected);
      return;
    }

    if (player.getGameMode() != GameMode.SPECTATOR) {
      core.getLocaleManager().sendMessage(player, "spectate.illegal_activity_woop_woop");
      ReportableEvent.call(
          player.getName()
              + " attempted to exit spectator mode while in "
              + player.getGameMode().name());
    }

    user.getStorage().set(EasterlynSpectators.USER_SPECTATE_RETURN, null);
    user.getStorage()
        .set(EasterlynSpectators.USER_SPECTATE_COOLDOWN, System.currentTimeMillis() + 480000L);
    player.teleport(spectateReturn);
    player.setGameMode(GameMode.SURVIVAL);
    user.getStorage().set(EasterlynSpectators.USER_SPECTATE_RETURN, player.getLocation());
    String rawOptions =
        core.getLocaleManager()
            .getValue("spectate.enter_body", core.getLocaleManager().getLocale(user.getPlayer()));
    String[] options = rawOptions != null ? rawOptions.split("\n") : new String[] {""};
    String selected = options[ThreadLocalRandom.current().nextInt(options.length)];
    user.sendMessage(selected);
  }

  @CommandAlias("spectate|spec|spectpa")
  @Subcommand("request")
  @Description("{@@spectate.request.description}")
  @CommandCompletion("@player")
  @Syntax("/spectpa <player>")
  @CommandPermission("easterlyn.command.spectpa")
  public void spectateTPA(
      @Flags(CoreContexts.SELF) User user, @Flags(CoreContexts.ONLINE) User target) {
    Player player = user.getPlayer();
    Player targetPlayer = target.getPlayer();

    if (player == null || targetPlayer == null) {
      core.getLocaleManager().sendMessage(user.getPlayer(), "sink.module.tprequest.error.offline");
      return;
    }

    if (player.getGameMode() != GameMode.SPECTATOR) {
      core.getLocaleManager().sendMessage(player, "spectate.not_spectator");
      return;
    }

    if (target.setPendingRequest(
        new Request() {
          @Override
          public void accept() {
            Player issuer = user.getPlayer();
            Player recipient = target.getPlayer();
            if (issuer == null || recipient == null) {
              core.getLocaleManager().sendMessage(recipient, "sink.module.tprequest.error.offline");
              return;
            }
            if (issuer.getGameMode() != GameMode.SPECTATOR) {
              core.getLocaleManager()
                  .sendMessage(
                      user.getPlayer(),
                      "spectate.request.not_spectator",
                      "{value}",
                      issuer.getName());
              return;
            }

            user.getStorage().set(EasterlynSpectators.USER_SPECTPA, true);
            if (issuer.teleport(
                recipient.getLocation().add(0, 0.1, 0),
                PlayerTeleportEvent.TeleportCause.SPECTATE)) {
              core.getLocaleManager().sendMessage(recipient, "sink.module.tprequest.common.accept");
              core.getLocaleManager()
                  .sendMessage(
                      issuer,
                      "sink.module.tprequest.common.accepted",
                      "{target}",
                      recipient.getName());
            }
            user.getStorage().set(EasterlynSpectators.USER_SPECTPA, null);
          }

          @Override
          public void decline() {
            core.getLocaleManager()
                .sendMessage(targetPlayer, "sink.module.tprequest.common.decline");
            core.getLocaleManager()
                .sendMessage(
                    player,
                    "sink.module.tprequest.common.declined",
                    "{target}",
                    targetPlayer.getName());
            // TODO same anti-spam ignore?
          }
        })) {
      String message =
          core.getLocaleManager()
              .getValue(
                  "spectate.request.request",
                  core.getLocaleManager().getLocale(targetPlayer),
                  "{value}",
                  player.getName());
      if (message != null) {
        target.sendMessage(player.getUniqueId(), message);
      }

    } else {
      core.getLocaleManager()
          .sendMessage(
              player, "sink.module.tprequest.error.popular", "{value}", targetPlayer.getName());
    }
  }
}
