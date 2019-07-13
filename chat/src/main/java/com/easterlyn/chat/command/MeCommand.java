package com.easterlyn.chat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.event.UserChatEvent;
import com.easterlyn.user.User;

public class MeCommand extends BaseCommand {

	@CommandAlias("me")
	@Description("Do an action!")
	@CommandPermission("easterlyn.command.me")
	public void me(@Flags("self") User sender, Channel channel, String args) {
		new UserChatEvent(sender, channel, args, true).send();
	}

}
