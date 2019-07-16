package com.easterlyn.kitchensink.command;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.easterlyn.command.CommandRank;
import com.easterlyn.user.UserRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

@CommandAlias("gamemode|gm")
@Description("Change game modes!")
@CommandPermission("easterlyn.command.gamemode")
@CommandRank(UserRank.MODERATOR)
public class GamemodeCommand {

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
		if (target.getGameMode() == gameMode) {
			issuer.sendMessage("Game mode is already " + gameMode.name().toLowerCase());
			return;
		}

		target.setGameMode(gameMode);

		if (target.getGameMode() != gameMode) {
			issuer.sendMessage("Game mode change prevented!");
			return;
		}

		if (!target.hasPermission("easterlyn.command.gamemode")) {
			target.sendMessage("Game mode set to " + gameMode.name().toLowerCase() + "!");
		}

		Bukkit.broadcast(ChatColor.GRAY + "[" + issuer.getIssuer().getName() + "] Set " + target.getName()
						+ "'s game mode to " + gameMode.name().toLowerCase(),
				"easterlyn.command.gamemode");

	}
}
