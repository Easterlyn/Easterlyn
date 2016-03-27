package co.sblock.commands.utility;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;
import co.sblock.discord.Discord;

/**
 * Command allowing users to report issues directly to moderators, even if none are online.
 * 
 * @author Jikoo
 */
public class ReportCommand extends SblockCommand {

	private final Discord discord;

	public ReportCommand(Sblock plugin) {
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
