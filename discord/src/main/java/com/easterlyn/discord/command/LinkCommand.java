package com.easterlyn.discord.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Private;
import com.easterlyn.EasterlynDiscord;

@CommandAlias("link")
@Description("Generate a code to link your Discord account with.")
@CommandPermission("easterlyn.command.link")
public class LinkCommand extends BaseCommand {

	@Dependency
	private EasterlynDiscord discord;

	@Default
	@Private
	public void link(BukkitCommandIssuer issuer) {
		//TODO
		issuer.sendMessage("Not yet implemented. Whoops.");
	}

}
