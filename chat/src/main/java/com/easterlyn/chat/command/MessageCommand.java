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
import com.easterlyn.user.AutoUser;
import com.easterlyn.user.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MessageCommand extends BaseCommand {

  // TODO timeout system
  private final HashMap<UUID, User> replies = new HashMap<>();

  @Dependency EasterlynCore core;

  @Dependency EasterlynChat chat;

  @CommandAlias("message|msg|m|whisper|w|pm|tell|t")
  @Description("{@@chat.commands.message.description}")
  @CommandPermission("easterlyn.command.message")
  @Syntax("<recipient> <message>")
  @CommandCompletion("@player")
  public void sendMessage(
      BukkitCommandIssuer sender, @Flags(CoreContexts.ONLINE) User target, String message) {
    User issuer;
    if (sender.isPlayer()) {
      issuer = core.getUserManager().getUser(sender.getUniqueId());
    } else {
      Map<String, String> userData = new HashMap<>();
      userData.put("name", sender.getIssuer().getName());
      issuer = new AutoUser(core, userData);
      // For the purpose of allowing replies to console, set target's reply target.
      replies.put(target.getUniqueId(), issuer);
    }
    replies.put(issuer.getUniqueId(), target);

    Channel channel = chat.getChannels().get("pm");

    if (channel == null) {
      ReportableEvent.call("Channel #pm not set up when executing /message!");
      core.getLocaleManager()
          .sendMessage(sender.getIssuer(), "chat.commands.message.error.pm_channel");
      return;
    }

    List<UUID> recipients = new ArrayList<>();
    if (!(issuer instanceof AutoUser)) {
      recipients.add(issuer.getUniqueId());
    }
    if (!(target instanceof AutoUser)) {
      recipients.add(target.getUniqueId());
    }

    new UserChatEvent(issuer, channel, target.getDisplayName() + ": " + message).send(recipients);
  }

  @CommandAlias("reply|r")
  @Description("{@@chat.commands.reply.description}")
  @CommandPermission("easterlyn.command.message")
  @Syntax("<message>")
  @CommandCompletion("")
  public void sendMessage(@Flags(CoreContexts.SELF) BukkitCommandIssuer sender, String message) {
    User target = replies.get(sender.getUniqueId());
    if (target == null) {
      core.getLocaleManager()
          .sendMessage(sender.getIssuer(), "chat.commands.reply.error.no_target");
      return;
    }

    sendMessage(sender, target, message);
  }
}
