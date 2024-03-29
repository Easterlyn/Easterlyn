package com.easterlyn.chat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynChat;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreLang;
import com.easterlyn.user.PlayerUser;
import com.easterlyn.util.wrapper.PlayerUserFuture;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MuteCommand extends BaseCommand {

  @Dependency EasterlynCore core;

  @CommandAlias("mute")
  @Description("{@@chat.commands.mute.description}")
  @CommandPermission("easterlyn.command.mute")
  @CommandCompletion("@player")
  @Syntax("<player>")
  public void mute(@NotNull BukkitCommandIssuer issuer, @NotNull PlayerUserFuture target) {
    mute(issuer, target, new Date(Long.MAX_VALUE));
  }

  @CommandAlias("mute")
  @Description("{@@chat.commands.mute.description}")
  @CommandPermission("easterlyn.command.mute")
  @CommandCompletion("@player @date")
  @Syntax("<player> <duration>")
  public void mute(
      @NotNull BukkitCommandIssuer issuer,
      @NotNull PlayerUserFuture target,
      @NotNull Date date) {

    target
        .future()
        .thenAccept(
            targetUser -> {
              if (targetUser.isPresent()) {
                muteLater(issuer, targetUser.get(), date);
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

  private void muteLater(
      @NotNull BukkitCommandIssuer issuer,
      @NotNull PlayerUser target,
      @NotNull Date date) {
    target.getStorage().set(EasterlynChat.USER_MUTE, date.getTime());

    boolean isInfinite = date.getTime() == Long.MAX_VALUE;
    String dateString = new SimpleDateFormat("HH:mm 'on' dd MMM yyyy").format(date);

    core.getLocaleManager()
        .sendMessage(
            issuer.getIssuer(),
            isInfinite
                ? "chat.commands.mute.issuer.indefinite"
                : "chat.commands.mute.issuer.duration",
            "{time}",
            dateString);

    Player player = target.getPlayer();
    if (player != null) {
      core.getLocaleManager()
          .sendMessage(
              player,
              isInfinite
                  ? "chat.commands.mute.target.indefinite"
                  : "chat.commands.mute.target.duration",
              "{time}",
              dateString);
    }
  }

}
