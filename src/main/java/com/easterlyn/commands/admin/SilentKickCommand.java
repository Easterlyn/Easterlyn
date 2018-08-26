package com.easterlyn.commands.admin;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.player.PlayerUtils;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * EasterlynCommand for silently kicking a player.
 *
 * @author Jikoo
 */
public class SilentKickCommand extends EasterlynCommand {

	public SilentKickCommand(Easterlyn plugin) {
		super(plugin, "silentkick");
		this.setPermissionLevel(UserRank.ADMIN);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			return false;
		}
		Player player = PlayerUtils.matchOnlinePlayer(sender, args[0]);
		if (player == null) {
			return false;
		}
		player.kickPlayer(StringUtils.join(args, ' ', 1, args.length));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission()) || args.length != 1) {
			return com.google.common.collect.ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}
}
