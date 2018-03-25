package com.easterlyn.commands.fun;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.User;
import com.easterlyn.users.UserAffinity;
import com.easterlyn.users.UserRank;
import com.easterlyn.users.Users;
import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Donator perk command, classpect reselection on the fly.
 *
 * @author Jikoo
 */
public class AffinityCommand extends EasterlynCommand {


	private final Users users;

	public AffinityCommand(Easterlyn plugin) {
		super(plugin, "affinity");
		this.setAliases("aspect");
		this.setPermissionLevel(UserRank.DANGER_DANGER_HIGH_VOLTAGE);
		this.users = plugin.getModule(Users.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		if (args.length == 0) {
			return false;
		}

		UserAffinity userAffinity = UserAffinity.getAffinity(ChatColor.translateAlternateColorCodes('&', args[0]));
		if (userAffinity == UserAffinity.EASTERLYN && !args[0].equalsIgnoreCase("easterlyn")
				|| userAffinity.getDisplayName().length() < 2
				|| userAffinity.getDisplayName().contains(String.valueOf(ChatColor.COLOR_CHAR))
				|| userAffinity.getColor() == null) {
			sender.sendMessage(getLang().getValue("command.aspect.failure"));
			return true;
		}

		User user = users.getUser(((Player) sender).getUniqueId());
		user.setUserAffinity(userAffinity);
		sender.sendMessage(getLang().getValue("command.aspect.success")
				.replace("{ASPECT}", userAffinity.getColor() + userAffinity.getDisplayName()));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || !sender.hasPermission(this.getPermission())
				|| args.length == 0 || args.length > 1) {
			return ImmutableList.of();
		}
		args[0] = args[0].toUpperCase();
		ArrayList<String> matches = new ArrayList<>();
		for (UserAffinity userAffinity : UserAffinity.values()) {
			if (StringUtil.startsWithIgnoreCase(userAffinity.getDisplayName(), args[0])) {
				matches.add(userAffinity.getDisplayName());
			}
		}
		return matches;
	}

}
