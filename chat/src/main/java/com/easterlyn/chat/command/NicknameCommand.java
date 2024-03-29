package com.easterlyn.chat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.PlayerUser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NicknameCommand extends BaseCommand {

  @Dependency EasterlynCore core;

  @CommandAlias("nick|nickname|name")
  @Description("{@@chat.commands.nickname.description}")
  @CommandPermission("easterlyn.command.nick.self")
  @Syntax("[target] <nickname|off>")
  @CommandCompletion("@player")
  public void setNick(
      @NotNull BukkitCommandIssuer issuer,
      @NotNull @Flags(CoreContexts.ONLINE_WITH_PERM) PlayerUser user,
      @Nullable @Optional String nickname) {
    if (nickname == null
        || nickname.isEmpty()
        || nickname.equalsIgnoreCase("off")
        || nickname.equalsIgnoreCase("null")
        || nickname.equalsIgnoreCase("remove")) {
      nickname = null;
    } else {
      nickname = ChatColor.translateAlternateColorCodes('&', nickname);
    }

    user.getStorage().set("displayName", nickname);
    user.reloadMention();

    Player player = user.getPlayer();
    if (player != null) {
      player.setDisplayName(nickname);
      if (nickname == null) {
        core.getLocaleManager().sendMessage(player, "chat.commands.nickname.success.remove.self");
      } else {
        core.getLocaleManager()
            .sendMessage(player, "chat.commands.nickname.success.self", "{value}", nickname);
      }
    }
    if (!issuer.getUniqueId().equals(user.getUniqueId())) {
      if (nickname == null) {
        core.getLocaleManager()
            .sendMessage(
                issuer.getIssuer(),
                "chat.commands.nickname.success.remove.other",
                "{target}",
                user.getUniqueId().toString());
      } else {
        core.getLocaleManager()
            .sendMessage(
                issuer.getIssuer(),
                "chat.commands.nickname.success.other",
                "{target}",
                user.getUniqueId().toString(),
                "{value}",
                nickname);
      }
    }
    if (nickname != null && nickname.indexOf(' ') > -1) {
      core.getLocaleManager()
          .sendMessage(issuer.getIssuer(), "chat.commands.nickname.warning.spaces");
    }
  }
}
