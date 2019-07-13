package com.easterlyn.chat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.easterlyn.command.CommandRank;
import com.easterlyn.user.User;
import com.easterlyn.user.UserRank;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class NicknameCommand extends BaseCommand {

	@CommandAlias("nick|nickname|name")
	@Description("Set a user's display name.")
	@CommandPermission("easterlyn.command.nick")
	@CommandRank(UserRank.MODERATOR)
	public void setNick(BukkitCommandIssuer issuer, User user, String nickname) {
		nickname = ChatColor.translateAlternateColorCodes('&', nickname);
		user.getStorage().set("displayName", nickname);
		Player player = user.getPlayer();
		// TODO rich messages
		if (player != null) {
			player.setDisplayName(nickname);
			player.sendMessage("Nickname set to " + nickname);
		}
		if (!issuer.getUniqueId().equals(user.getUniqueId())) {
			issuer.sendMessage("Set " + user.getDisplayName() + "'s nickname to " + nickname);
		}
		if (nickname.indexOf(' ') > -1) {
			issuer.sendMessage("Please note that nicknames with spaces will make it hard to target users by nickname.");
		}
	}

}
