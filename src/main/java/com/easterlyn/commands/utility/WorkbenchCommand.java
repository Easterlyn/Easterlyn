package com.easterlyn.commands.utility;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * EasterlynCommand for opening a workbench.
 * 
 * @author Jikoo
 */
public class WorkbenchCommand extends EasterlynCommand {

	public WorkbenchCommand(Easterlyn plugin) {
		super(plugin, "workbench");
		this.setAliases("craft", "wb");
		this.setDescription("Open a workbench.");
		this.setPermissionLevel(UserRank.MOD);
		this.setUsage("/craft");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player player = (Player) sender;
		player.openWorkbench(player.getLocation(), true);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return com.google.common.collect.ImmutableList.of();
	}

}
