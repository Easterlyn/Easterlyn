package com.easterlyn.chat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynChat;
import com.easterlyn.EasterlynCore;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.event.UserChatEvent;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.user.ServerUser;
import com.easterlyn.user.User;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MessageCommand extends BaseCommand {

  private final HashMap<UUID, User> replies = new HashMap<>();

  @Dependency EasterlynCore core;

  @Dependency EasterlynChat chat;

  @CommandAlias("message|msg|m|whisper|w|pm|dm|tell|t")
  @Description("{@@chat.commands.message.description}")
  @CommandPermission("easterlyn.command.message")
  @Syntax("<recipient> <message>")
  @CommandCompletion("@player")
  public void sendMessage(
      @NotNull BukkitCommandIssuer sender,
      @NotNull @Flags(CoreContexts.ONLINE) User target,
      @NotNull String message) {

    Channel channel = chat.getChannels().get("dm");

    if (channel == null) {
      ReportableEvent.call("Channel #dm not set up when executing /message!");
      core.getLocaleManager()
          .sendMessage(sender.getIssuer(), "chat.commands.message.error.pm_channel");
      return;
    }


    if (sender.isPlayer()) {
      core.getUserManager().getPlayer(sender.getUniqueId())
          .thenAccept(optional ->
              optional.ifPresent(playerUser ->
                  sendMessageLater(playerUser, target, channel, message)));
    } else {
      Map<String, String> userData = new HashMap<>();
      userData.put("name", sender.getIssuer().getName());
      User issuer = new ServerUser(core, userData);
      sendMessageLater(issuer, target, channel, message);
    }
  }

  private void sendMessageLater(
      @NotNull User issuer,
      @NotNull User target,
      @NotNull Channel channel,
      @NotNull String message) {

    replies.put(target.getUniqueId(), issuer);
    replies.put(issuer.getUniqueId(), target);

    List<UUID> recipients = new ArrayList<>();
    if (!(issuer instanceof ServerUser)) {
      recipients.add(issuer.getUniqueId());
    }
    if (!(target instanceof ServerUser)) {
      recipients.add(target.getUniqueId());
    }

    new UserChatEvent(issuer, channel, target.getDisplayName() + ": " + message).send(recipients);
  }

  @CommandAlias("reply|r")
  @Description("{@@chat.commands.reply.description}")
  @CommandPermission("easterlyn.command.message")
  @Syntax("<message>")
  @CommandCompletion("")
  public void sendMessage(
      @NotNull @Flags(CoreContexts.SELF) BukkitCommandIssuer sender,
      @NotNull String message) {
    User target = replies.get(sender.getUniqueId());
    if (target == null) {
      core.getLocaleManager()
          .sendMessage(sender.getIssuer(), "chat.commands.reply.error.no_target");
      return;
    }

    sendMessage(sender, target, message);
  }
}
