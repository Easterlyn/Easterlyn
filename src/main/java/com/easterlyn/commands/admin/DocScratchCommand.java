package com.easterlyn.commands.admin;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.SblockCommand;
import com.easterlyn.users.UserRank;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/**
 * SblockCommand for being Doc Scratch.
 * 
 * @author Jikoo
 */
public class DocScratchCommand extends SblockCommand {

	public DocScratchCommand(Easterlyn plugin) {
		super(plugin, "o");
		this.setDescription("&a> Be the white text guy");
		this.setPermissionLevel(UserRank.HORRORTERROR);
		this.setPermissionMessage("&f&l[o] You try to be the white text guy, but fail to be the white text guy. "
					+ "No one can be the white text guy except for the white text guy.");
		this.setUsage("/o <text>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args == null || args.length == 0) {
			return false;
		}
		Bukkit.broadcastMessage(StringUtils.join(args, ' '));
		return true;
	}
}
