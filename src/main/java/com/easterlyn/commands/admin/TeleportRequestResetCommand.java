package com.easterlyn.commands.admin;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.micromodules.Cooldowns;
import com.easterlyn.users.UserRank;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * EasterlynCommand for resetting TPA timer. Intended for official business use only.
 * 
 * @author Jikoo
 */
public class TeleportRequestResetCommand extends EasterlynCommand {

	private final Cooldowns cooldowns;

	public TeleportRequestResetCommand(Easterlyn plugin) {
		super(plugin, "tpreset");
		this.setPermissionLevel(UserRank.HELPER);
		this.cooldowns = plugin.getModule(Cooldowns.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		cooldowns.clearCooldown((Player) sender, "teleportRequest");
		sender.sendMessage(getLang().getValue("command.tpreset.success"));
		return true;
	}

}
