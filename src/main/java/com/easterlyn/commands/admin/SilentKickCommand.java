package com.easterlyn.commands.admin;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.SblockCommand;
import com.easterlyn.users.UserRank;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * SblockCommand for silently kicking a player.
 * 
 * @author Jikoo
 */
public class SilentKickCommand extends SblockCommand {

	public SilentKickCommand(Easterlyn plugin) {
		super(plugin, "silentkick");
		this.setPermissionLevel(UserRank.DENIZEN);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			return false;
		}
		List<Player> players = Bukkit.matchPlayer(args[0]);
		if (players.isEmpty()) {
			return false;
		}
		players.get(0).kickPlayer(StringUtils.join(args, ' ', 1, args.length));
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
