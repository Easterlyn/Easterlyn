package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.command.CoreContexts;
import org.bukkit.entity.Player;

public class TeleportHereCommand extends BaseCommand {

	@CommandAlias("tphere")
	@Description("Teleport a player to you.")
	@Syntax("/tphere <target>")
	@CommandCompletion("@player")
	@CommandPermission("easterlyn.command.tphere")
	public void teleportHere(@Flags(CoreContexts.SELF) Player issuer, @Flags(CoreContexts.ONLINE) Player target) {
		target.teleport(issuer);
		target.sendMessage("Teleported you to " + issuer.getDisplayName() + ".");
		issuer.sendMessage("Teleported " + target.getDisplayName() + " to you.");
	}

}
