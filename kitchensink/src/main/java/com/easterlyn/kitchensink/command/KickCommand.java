package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.command.CoreContexts;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class KickCommand extends BaseCommand {

	@CommandAlias("kick")
	@Description("Kick a player.")
	@Syntax("/kick <player> <reason>")
	@CommandPermission("easterlyn.command.kick")
	@CommandCompletion("@player")
	public void kick(@Flags(CoreContexts.ONLINE) Player target, @Default("Big brother is watching.") String reason) {
		target.kickPlayer(reason);
		Bukkit.broadcastMessage("Kicked " + target.getName() + ": " + reason);
	}

	@CommandAlias("silentkick")
	@Description("Kick a player without a broadcast.")
	@Syntax("/silentkick <player> <reason>")
	@CommandPermission("easterlyn.command.silentkick")
	@CommandCompletion("@player")
	public void silentKick(@Flags(CoreContexts.ONLINE) Player target, @Default("Connection lost.") String reason) {
		target.kickPlayer(reason);
	}

	@CommandAlias("kickall")
	@Description("Kick all players.")
	@Syntax("/kickall <reason>")
	@CommandPermission("easterlyn.command.kickall")
	@CommandCompletion("@player")
	public void kickAll(@Default("Come back in a minute!") String reason) {
		Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer(reason));
	}

}
