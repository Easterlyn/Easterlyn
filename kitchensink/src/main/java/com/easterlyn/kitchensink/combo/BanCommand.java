package com.easterlyn.kitchensink.combo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreLang;
import com.easterlyn.util.wrapper.PlayerFuture;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class BanCommand extends BaseCommand implements Listener {
  // TODO IP bans

  @Dependency EasterlynCore core;

  @CommandAlias("ban")
  @Description("{@@sink.module.ban.description}")
  @CommandPermission("easterlyn.command.ban")
  @Syntax("<player> [reason]")
  @CommandCompletion("@player")
  public void ban(
      @NotNull BukkitCommandIssuer issuer,
      @NotNull PlayerFuture target,
      @NotNull @Default("Big brother is watching.") String reason) {
    tempban(issuer, target, new Date(Long.MAX_VALUE), reason);
  }

  @CommandAlias("tempban")
  @CommandPermission("easterlyn.command.tempban")
  @Description("{@@sink.module.ban.tempban.description}")
  @Syntax("<player> <date> [reason]")
  @CommandCompletion("@player @date")
  public void tempban(
      @NotNull BukkitCommandIssuer issuer,
      @NotNull PlayerFuture target,
      @NotNull Date date,
      @NotNull @Default("Big brother is watching.") String reason) {

    target
        .future()
        .thenAccept(
            targetUser -> {
              if (targetUser.isPresent()) {
                banLater(issuer, targetUser.get(), date, reason);
              } else {
                core.getLocaleManager()
                    .sendMessage(
                        issuer.getIssuer(),
                        CoreLang.INVALID_PLAYER.getMessageKey().getKey(),
                        "{value}",
                        target.id());
              }
            });
  }

  private void banLater(
      @NotNull BukkitCommandIssuer issuer,
      @NotNull Player target,
      @NotNull Date date,
      @NotNull String reason) {

    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm 'on' dd MMM yyyy");
    String locale = core.getLocaleManager().getLocale(target);
    String listReason = core.getLocaleManager().getValue("sink.module.ban.banned", locale);
    if (listReason == null) {
      listReason = "Banned: ";
    }
    listReason += reason;
    if (date.getTime() < Long.MAX_VALUE) {
      String value =
          core.getLocaleManager()
              .getValue("sink.module.ban.expiration", locale, "{value}", dateFormat.format(date));
      if (value != null) {
        listReason += '\n' + value;
      }
    }
    if (target.isOnline()) {
      String finalListReason = listReason;
      core.getServer().getScheduler().callSyncMethod(core, () -> {
        target.kickPlayer(finalListReason);
        return true;
      });
    }

    Bukkit.getBanList(Type.NAME)
        .addBan(
            Objects.requireNonNull(target.getName()),
            listReason,
            date,
            issuer.getIssuer().getName() + " on " + dateFormat.format(new Date()));
    target.getName();
    core.getLocaleManager()
        .broadcast(
            "sink.module.ban.announcement",
            "{target}",
            target.getName(),
            "{reason}",
            reason);

  }

  @EventHandler
  public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
    // Silence banned quits, we broadcast it ourselves.
    if (event.getPlayer().isBanned()) {
      event.setQuitMessage(null);
    }
  }

}
