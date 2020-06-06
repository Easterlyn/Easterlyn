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
	@CommandPermission("easterlyn.command.kick")
	@Syntax("<player> [reason]")
	@CommandCompletion("@player")
	public void kick(@Flags(CoreContexts.ONLINE) Player target, @Default("Big brother is watching.") String reason) {
		target.kickPlayer(reason);
		Bukkit.broadcastMessage("Kicked " + target.getName() + ": " + reason);
	}

	@CommandAlias("silentkick")
	@Description("Kick a player without a broadcast.")
	@CommandPermission("easterlyn.command.silentkick")
	@Syntax("<player> [reason]")
	@CommandCompletion("@player")
	public void silentKick(@Flags(CoreContexts.ONLINE) Player target, @Default("Connection lost.") String reason) {
		target.kickPlayer(reason);
	}

	@CommandAlias("kickall")
	@Description("Kick all players.")
	@CommandPermission("easterlyn.command.kickall")
	@Syntax("[reason]")
	@CommandCompletion("@player")
	public void kickAll(@Default("Come back in a minute!") String reason) {
		Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer(reason));
	}

}
