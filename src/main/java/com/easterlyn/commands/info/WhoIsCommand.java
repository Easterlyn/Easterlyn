package com.easterlyn.commands.info;

import java.util.List;
import java.util.UUID;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.SblockAsynchronousCommand;
import com.easterlyn.users.UserRank;
import com.easterlyn.users.Users;

import com.google.common.collect.ImmutableList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * SblockCommand for checking a User's stored data.
 * 
 * @author Jikoo
 */
public class WhoIsCommand extends SblockAsynchronousCommand {

	private final Users users;

	public WhoIsCommand(Easterlyn plugin) {
		super(plugin, "whois");
		this.users = plugin.getModule(Users.class);
		this.setAliases("profile");
		this.setDescription("Check data stored for a player.");
		this.setUsage("/whois <player>");
		this.addExtraPermission("detail", UserRank.FELT);
	}

	@Override
	protected boolean onCommand(final CommandSender sender, final String label, final String[] args) {
		if (!(sender instanceof Player) && args.length == 0) {
			return false;
		}
		final UUID uuid = args.length >= 1 ? getUniqueId(args[0]) : ((Player) sender).getUniqueId();
		if (uuid == null) {
			sender.sendMessage(getLang().getValue("core.error.invalidUser").replace("{PLAYER}", args[0]));
			return true;
		}
		if (sender.hasPermission("sblock.command.whois.detail")) {
			sender.sendMessage(users.getUser(uuid).getWhois());
		} else {
			sender.sendMessage(users.getUser(uuid).getProfile());
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
