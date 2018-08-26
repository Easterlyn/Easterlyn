package com.easterlyn.commands.info;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynAsynchronousCommand;
import com.easterlyn.users.User;
import com.easterlyn.users.UserRank;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.player.PlayerUtils;

import com.google.common.collect.ImmutableList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * EasterlynCommand for checking a User's stored data.
 *
 * @author Jikoo
 */
public class WhoIsCommand extends EasterlynAsynchronousCommand {

	private final Users users;

	public WhoIsCommand(Easterlyn plugin) {
		super(plugin, "whois");
		this.users = plugin.getModule(Users.class);
		this.setAliases("profile");
		this.setDescription("Check data stored for a player.");
		this.setUsage("/whois <player>");
		this.addExtraPermission("detail", UserRank.MOD);
	}

	@Override
	protected boolean onCommand(final CommandSender sender, final String label, final String[] args) {
		if (!(sender instanceof Player) && args.length == 0) {
			return false;
		}

		Player target;
		if (args.length > 0) {
			target = PlayerUtils.matchPlayer(args[0], true, getPlugin());
		} else {
			target = (Player) sender;
		}

		if (target == null) {
			sender.sendMessage(getLang().getValue("core.error.invalidUser").replace("{PLAYER}", args[0]));
			return true;
		}

		User user = users.getUser(target.getUniqueId());

		if (sender.hasPermission("easterlyn.command.whois.detail")) {
			sender.sendMessage(user.getWhois());
		} else {
			sender.sendMessage(user.getProfile());
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (args.length != 1) {
			return ImmutableList.of();
		} else {
			return super.tabComplete(sender, alias, args);
		}
	}
}
