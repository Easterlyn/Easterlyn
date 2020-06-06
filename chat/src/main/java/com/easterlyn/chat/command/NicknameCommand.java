package com.easterlyn.chat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.user.User;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class NicknameCommand extends BaseCommand {

	@Dependency
	EasterlynCore core;

	@CommandAlias("nick|nickname|name")
	@Description("{@@chat.commands.nickname.description}")
	@CommandPermission("easterlyn.command.nick")
	@Syntax("[target] <nickname|off>")
	@CommandCompletion("@player")
	public void setNick(BukkitCommandIssuer issuer, User user, String nickname) {
		nickname = ChatColor.translateAlternateColorCodes('&', nickname);
		String oldName = user.getDisplayName();
		user.getStorage().set("displayName", nickname);
		Player player = user.getPlayer();
		if (player != null) {
			player.setDisplayName(nickname);
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.commands.nickname.success.self",
					"{value}", nickname);
		}
		if (!issuer.getUniqueId().equals(user.getUniqueId())) {
			core.getLocaleManager().sendMessage(issuer.getIssuer(), "chat.commands.nickname.success.other",
					"{target}", oldName, "{value}", nickname);
		}
		if (nickname.indexOf(' ') > -1) {
			core.getLocaleManager().sendMessage(issuer.getIssuer(), "chat.commands.nickname.warning.spaces");
		}
	}

}
