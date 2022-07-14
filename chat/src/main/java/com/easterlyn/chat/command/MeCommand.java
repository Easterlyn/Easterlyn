package com.easterlyn.chat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.event.UserChatEvent;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.User;
import org.jetbrains.annotations.NotNull;

public class MeCommand extends BaseCommand {

  @CommandAlias("me")
  @Description("{@@chat.commands.me.description}")
  @CommandPermission("easterlyn.command.me")
  @Syntax("[#channel] <action>")
  @CommandCompletion("")
  public void me(
      @NotNull @Flags(CoreContexts.SELF) User sender,
      @NotNull @Flags(ChannelFlag.LISTENING_OR_CURRENT) Channel channel,
      @NotNull String args) {
    new UserChatEvent(sender, channel, args, true).send();
  }

}
