package com.easterlyn.kitchensink.combo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreContexts;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import org.bukkit.BanList;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class BanCommand extends BaseCommand implements Listener {
	// TODO IP bans

	@Dependency
	EasterlynCore core;

	@CommandAlias("ban")
	@Description("{@@sink.module.ban.description}")
	@CommandPermission("easterlyn.command.ban")
	@Syntax("<player> [reason]")
	@CommandCompletion("@player")
	public void ban(BukkitCommandIssuer issuer, @Flags(CoreContexts.OFFLINE) OfflinePlayer target, @Default("Big brother is watching.") String reason) {
		tempban(issuer, target, new Date(Long.MAX_VALUE), reason);
	}

	@CommandAlias("tempban")
	@CommandPermission("easterlyn.command.tempban")
	@Description("{@@sink.module.ban.tempban.description}")
	@Syntax("<player> <date> [reason]")
	@CommandCompletion("@player @date")
	public void tempban(BukkitCommandIssuer issuer, @Flags(CoreContexts.OFFLINE) OfflinePlayer target, Date date, @Default("Big brother is watching.") String reason) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm 'on' dd MMM yyyy");
		Player player = target.getPlayer();
		String locale = core.getLocaleManager().getLocale(player);
		String listReason = core.getLocaleManager().getValue("sink.module.ban.banned", locale);
		if (listReason == null) {
			listReason = "Banned: ";
		}
		listReason += reason;
		if (date.getTime() < Long.MAX_VALUE) {
			String value = core.getLocaleManager().getValue("sink.module.ban.expiration", locale,
					"{value}", dateFormat.format(date));
			if (value != null) {
				listReason += '\n' + value;
			}
		}
		core.getServer().getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(target.getName()), listReason, date,
				issuer.getIssuer().getName() + " on " + dateFormat.format(new Date()));
		if (player != null) {
			player.kickPlayer(listReason);
		}
		//target.banPlayer(listReason, date, issuer.getIssuer().getName() + " on " + dateFormat.format(new Date()), true);// TODO Paper
		core.getLocaleManager().broadcast("sink.module.ban.announcement",
				"{target}", target.getName() == null ? target.getUniqueId().toString() : target.getName(),
				"{reason}", reason);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		// Silence banned quits, we broadcast it ourselves.
		if (event.getPlayer().isBanned()) {
			event.setQuitMessage(null);
		}
	}

}
