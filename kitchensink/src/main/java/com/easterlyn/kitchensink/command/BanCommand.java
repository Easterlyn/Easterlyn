package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.easterlyn.command.CommandRank;
import com.easterlyn.user.UserRank;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class BanCommand extends BaseCommand {

	@CommandAlias("ban")
	@Description("Ban a player.")
	@CommandPermission("easterlyn.command.ban")
	@CommandRank(UserRank.MODERATOR)
	public void ban(BukkitCommandIssuer issuer, OfflinePlayer target, @Default("Big brother is watching.") String reason) {
		tempban(issuer, target, new Date(Long.MAX_VALUE), reason);
	}

	@CommandAlias("tempban")
	@CommandPermission("easterlyn.command.tempban")
	@Description("Ban a player for a time period.")
	@CommandRank(UserRank.MODERATOR)
	public void tempban(BukkitCommandIssuer issuer, OfflinePlayer target, Date date, @Default("Big brother is watching.") String reason) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm 'on' dd/MM/YY");
		if (date.getTime() < Long.MAX_VALUE) {
			reason += "\nBan expires " + dateFormat.format(date) + ".";
		}
		target.banPlayer("Banned: " + reason, date, issuer.getIssuer().getName() + " on " + dateFormat.format(new Date()), true);
		Bukkit.broadcastMessage((target.getName() == null ? target.getUniqueId() : target.getName()) + " is banned: " + reason);
	}

}
