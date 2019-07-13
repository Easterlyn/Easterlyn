package com.easterlyn.chat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Private;
import com.easterlyn.EasterlynChat;
import com.easterlyn.command.CommandRank;
import com.easterlyn.user.User;
import com.easterlyn.user.UserRank;
import java.text.SimpleDateFormat;
import java.util.Date;

@CommandAlias("channel")
public class MuteCommand extends BaseCommand {

	@CommandAlias("mute")
	@Default
	@Private
	@Description("Mute a player.")
	@CommandPermission("easterlyn.command.mute")
	@CommandRank(UserRank.MODERATOR)
	public void mute(BukkitCommandIssuer issuer, User target) {
		mute(issuer, target, new Date(Long.MAX_VALUE));
	}

	@CommandAlias("mute")
	@Default
	@Private
	@CommandPermission("easterlyn.command.mute")
	@Description("Mute a player for a time period.")
	@CommandRank(UserRank.MODERATOR)
	public void mute(BukkitCommandIssuer issuer, User target, Date date) {
		target.getStorage().set(EasterlynChat.USER_MUTE, date.getTime());
		String message;
		if (date.getTime() == Long.MAX_VALUE) {
			message = " been muted.";
		} else {
			SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm 'on' dd/MM/YY");
			message = " been muted until " + dateFormat.format(date) + ".";
		}
		target.sendMessage("You have" + message);
		issuer.sendMessage(target.getDisplayName() + " has" + message);
	}

}
