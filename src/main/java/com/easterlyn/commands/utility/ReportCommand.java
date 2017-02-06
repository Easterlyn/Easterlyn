package com.easterlyn.commands.utility;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.discord.Discord;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command allowing users to report issues directly to moderators, even if none are online.
 * 
 * @author Jikoo
 */
public class ReportCommand extends EasterlynCommand {

	private final Discord discord;

	public ReportCommand(Easterlyn plugin) {
		super(plugin, "report");
		this.discord = plugin.getModule(Discord.class);
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
		if (args.length < 4) {
			sender.sendMessage(getLang().getValue("command.report.more"));
			return true;
		}
		if (!discord.isEnabled()) {
			sender.sendMessage(getLang().getValue("command.report.discord"));
			return true;
		}
		Player player = (Player) sender;
		Location location = player.getLocation();
		discord.postReport(getLang().getValue("command.report.send").replace("{PLAYER}", player.getDisplayName())
				.replace("{X}", String.valueOf(location.getBlockX()))
				.replace("{Y}", String.valueOf(location.getBlockY()))
				.replace("{Z}", String.valueOf(location.getBlockZ()))
				.replace("{WORLD}", location.getWorld().getName())
				.replace("{PARAMETER}", StringUtils.join(args, ' ')));
		player.sendMessage(getLang().getValue("command.report.success"));
		return true;
	}

}
