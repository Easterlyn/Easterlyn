package com.easterlyn.commands.admin;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynAsynchronousCommand;
import com.easterlyn.users.User;
import com.easterlyn.users.UserAffinity;
import com.easterlyn.users.UserClass;
import com.easterlyn.users.UserRank;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.PlayerUtils;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * EasterlynCommand for setting User data.
 *
 * @author Jikoo
 */
public class SetPlayerCommand extends EasterlynAsynchronousCommand {

	private final Users users;
	private final String[] primaryArgs;

	public SetPlayerCommand(Easterlyn plugin) {
		super(plugin, "setplayer");
		this.setDescription("Set player data manually.");
		this.setPermissionLevel(UserRank.ADMIN);
		this.setUsage("/setplayer <playername> <class|affinity> <value>");
		this.users = plugin.getModule(Users.class);
		primaryArgs = new String[] {"class", "aspect"};
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args == null || args.length < 3) {
			return false;
		}
		Player player = PlayerUtils.matchPlayer(args[0], true, getPlugin());
		if (player == null) {
			sender.sendMessage(getLang().getValue("core.error.invalidUser").replace("{PLAYER}", args[0]));
			return true;
		}
		User user = users.getUser(player.getUniqueId());
		args[1] = args[1].toLowerCase();
		switch (args[1]) {
			case "class":
				user.setUserClass(UserClass.getClass(ChatColor.translateAlternateColorCodes('&', args[2])));
				break;
			case "aspect":
			case "affinity":
				user.setUserAffinity(UserAffinity.getAffinity(ChatColor.translateAlternateColorCodes('&', args[2])));
				break;
			default:
				return false;
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission())) {
			return ImmutableList.of();
		}
		if (args.length < 2) {
			return super.tabComplete(sender, alias, args);
		}
		ArrayList<String> matches = new ArrayList<>();
		args[1] = args[1].toLowerCase();
		if (args.length == 2) {
			for (String argument : primaryArgs) {
				if (argument.startsWith(args[1])) {
					matches.add(argument);
				}
			}
			return matches;
		}
		if (args[1].equals("class") && args.length == 3) {
			args[2] = args[2].toUpperCase();
			for (UserClass userclass : UserClass.values()) {
				if (StringUtil.startsWithIgnoreCase(userclass.getDisplayName(), args[2])) {
					matches.add(userclass.getDisplayName());
				}
			}
			return matches;
		}
		if ((args[1].equals("affinity") || args[1].equals("aspect")) && args.length == 3) {
			args[2] = args[2].toUpperCase();
			for (UserAffinity aspect : UserAffinity.values()) {
				if (StringUtil.startsWithIgnoreCase(aspect.getDisplayName(), args[2])) {
					matches.add(aspect.getDisplayName());
				}
			}
			return matches;
		}
		return ImmutableList.of();
	}
}
