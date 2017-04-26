package com.easterlyn.commands.info;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.PlayerUtils;

import com.google.common.collect.ImmutableList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * EasterlynCommand for getting a player's ping.
 * 
 * @author Jikoo
 */
public class PingCommand extends EasterlynCommand {

	public PingCommand(Easterlyn plugin) {
		super(plugin, "ping");
		this.setDescription("Get your ping.");
		this.setUsage("/ping <player>");
		this.addExtraPermission("other", UserRank.HELPER);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player) && args.length == 0) {
			return false;
		}
		// future: couple samples over a short period
		Player target;
		if (args.length == 0 || !sender.hasPermission("easterlyn.command.ping.other")) {
			target = (Player) sender;
		} else {
			target = PlayerUtils.matchOnlinePlayer(sender, args[0]);
		}
		if (target == null) {
			sender.sendMessage(this.getLang().getValue("core.error.invalidUser").replace("{PLAYER}", args[0]));
			return true;
		}
		sender.sendMessage(Language.getColor("player.good") + target.getName()
				+ Language.getColor("good") + "'s ping is "
				+ ((org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer) target).getHandle().ping + "ms!");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (args.length != 1 || !sender.hasPermission("easterlyn.command.ping.other")) {
			return ImmutableList.of();
		} else {
			return super.tabComplete(sender, alias, args);
		}
	}
}
