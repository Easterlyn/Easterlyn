package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.command.CommandRank;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.UserRank;
import org.bukkit.entity.Player;

public class WorkbenchCommand extends BaseCommand {

	@CommandAlias("workbench|craft")
	@Description("Open a crafting table.")
	@Syntax("/workbench")
	@CommandCompletion("@none")
	@CommandPermission("easterlyn.command.workbench")
	@CommandRank(UserRank.MODERATOR)
	public void workbench(@Flags(CoreContexts.SELF) Player player) {
		player.openWorkbench(player.getLocation(), true);
	}

}
