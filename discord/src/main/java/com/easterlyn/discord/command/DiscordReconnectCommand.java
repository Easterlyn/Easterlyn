package com.easterlyn.discord.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.easterlyn.command.CommandRank;
import com.easterlyn.user.UserRank;

public class DiscordReconnectCommand extends BaseCommand {

	@CommandAlias("dc-reconnect")
	@Description("Restart the Discord client.")
	@CommandPermission("easterlyn.command.discordreconnect")
	@CommandRank(UserRank.DANGER_DANGER_HIGH_VOLTAGE)
	public void reconnect() {
		// TODO
	}

}
