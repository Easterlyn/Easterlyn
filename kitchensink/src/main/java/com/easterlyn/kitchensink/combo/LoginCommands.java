package com.easterlyn.kitchensink.combo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.locales.MessageKey;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.command.CoreLang;
import com.easterlyn.user.User;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("onlogin")
@Description("{@@sink.module.onlogin.description}")
@CommandPermission("easterlyn.command.onlogin")
public class LoginCommands extends BaseCommand implements Listener {

  private static final String ONLOGIN = "kitchensink.onlogin";

  @Dependency EasterlynCore core;

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
    core.getUserManager().getPlayer(event.getPlayer().getUniqueId())
            .thenAccept(opt -> opt.ifPresent(
                user -> user.getStorage()
                    .getStringList(ONLOGIN)
                    .forEach(string -> event.getPlayer().chat(string))));
  }

  @Subcommand("list")
  @Description("{@@sink.module.onlogin.list.description}")
  @Syntax("[player]")
  @CommandCompletion("@player")
  public void list(@NotNull @Flags(CoreContexts.ONLINE_WITH_PERM) User user) {
    CommandIssuer issuer = getCurrentCommandIssuer();
    List<String> list = user.getStorage().getStringList(ONLOGIN);
    if (list.isEmpty()) {
      issuer.sendInfo(MessageKey.of("sink.module.onlogin.error.none"));
      return;
    }
    for (int i = 0; i < list.size(); ++i) {
      issuer.sendMessage((i + 1) + ": " + list.get(i));
    }
  }

  @Subcommand("add")
  @Description("{@@sink.module.onlogin.add.description}")
  @Syntax("</command parameters>")
  @CommandCompletion("@player")
  public void add(
      @NotNull @Flags(CoreContexts.ONLINE_WITH_PERM) User user,
      @Nullable @Optional String command) {
    CommandIssuer issuer = getCurrentCommandIssuer();
    List<String> list = new ArrayList<>(user.getStorage().getStringList(ONLOGIN));
    if (!issuer.hasPermission("easterlyn.command.onlogin.more") && list.size() >= 2
        || list.size() >= 5) {
      issuer.sendInfo(MessageKey.of("sink.module.onlogin.error.capped"));
      return;
    }
    if (command == null
        || command.length() < 2
        || command.charAt(0) != '/'
        || command.matches("/(\\w+:)?me .*")) {
      issuer.sendInfo(MessageKey.of("sink.module.onlogin.error.not_command"));
      return;
    }
    list.add(command);
    user.getStorage().set(ONLOGIN, list);
    issuer.sendInfo(MessageKey.of("sink.module.onlogin.add.success"), "{value}", command);
  }

  @Subcommand("remove")
  @Description("{@@sink.module.onlogin.remove.description}")
  @Syntax("<index>")
  @CommandCompletion("@player @integer")
  public void remove(@NotNull @Flags(CoreContexts.ONLINE_WITH_PERM) User user, int commandIndex) {
    CommandIssuer issuer = getCurrentCommandIssuer();
    List<String> list = new ArrayList<>(user.getStorage().getStringList(ONLOGIN));
    if (list.size() == 0) {
      issuer.sendInfo(MessageKey.of("sink.module.onlogin.error.none"));
      return;
    }
    if (commandIndex < 1 || commandIndex > list.size()) {
      issuer.sendInfo(CoreLang.NUMBER_WITHIN, "{min}", "1", "max", String.valueOf(list.size()));
      return;
    }
    String command = list.remove(commandIndex - 1);
    user.getStorage().set(ONLOGIN, list);
    issuer.sendInfo(MessageKey.of("sink.module.onlogin.remove.success"), "{value}", command);
  }
}
