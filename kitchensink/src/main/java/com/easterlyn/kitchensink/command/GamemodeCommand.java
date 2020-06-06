package com.easterlyn.kitchensink.command;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import com.easterlyn.EasterlynCore;
import com.easterlyn.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

@CommandAlias("gamemode|gm")
@Description("{@@sink.module.gamemode.description}")
@CommandPermission("easterlyn.command.gamemode")
public class GamemodeCommand {

	@Dependency
	EasterlynCore core;

	@CommandAlias("0|surv|survival")
	@Description("Set game mode to survival!")
	public void survival(BukkitCommandIssuer issuer, Player target) {
		setGameMode(issuer, GameMode.SURVIVAL, target);
	}

	@CommandAlias("1|creative")
	@Description("Set game mode to creative!")
	public void creative(BukkitCommandIssuer issuer, Player target) {
		setGameMode(issuer, GameMode.CREATIVE, target);
	}

	@CommandAlias("2|adventure")
	@Description("Set game mode to adventure!")
	public void adventure(BukkitCommandIssuer issuer, Player target) {
		setGameMode(issuer, GameMode.ADVENTURE, target);
	}

	@CommandAlias("3|spectator")
	@Description("Set game mode to spectator!")
	public void spectator(BukkitCommandIssuer issuer, Player target) {
		setGameMode(issuer, GameMode.SPECTATOR, target);
	}

	private void setGameMode(BukkitCommandIssuer issuer, GameMode gameMode, Player target) {
		if (target.getGameMode() != gameMode) {
			target.setGameMode(gameMode);
		}

		if (target.getGameMode() != gameMode) {
			core.getLocaleManager().sendMessage(issuer.getIssuer(), "sink.module.gamemode.prevented");
			return;
		}

		String gameModeName = StringUtil.getFriendlyName(gameMode);
		if (!target.hasPermission("easterlyn.command.gamemode")) {
			core.getLocaleManager().sendMessage(target, "sink.module.gamemode.success",
					"{value}", gameModeName);
		}

		Bukkit.broadcast(ChatColor.GRAY + "[" + issuer.getIssuer().getName() + "] Set " + target.getName()
						+ "'s game mode to " + gameModeName,
				"easterlyn.command.gamemode");

	}
}
