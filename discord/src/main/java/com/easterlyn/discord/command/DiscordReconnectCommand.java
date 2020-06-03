package com.easterlyn.discord.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;

public class DiscordReconnectCommand extends BaseCommand {

	@CommandAlias("dc-reconnect")
	@Description("Restart the Discord client.")
	@CommandPermission("easterlyn.command.discordreconnect")
	public void reconnect() {
		// TODO
	}

}
