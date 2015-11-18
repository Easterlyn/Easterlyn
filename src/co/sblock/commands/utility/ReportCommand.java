package co.sblock.commands.utility;

import org.apache.commons.lang.StringUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.discord.Discord;
import co.sblock.users.BukkitSerializer;

/**
 * Command allowing users to report issues directly to moderators, even if none are online.
 * 
 * @author Jikoo
 */
public class ReportCommand extends SblockCommand {

	public ReportCommand() {
		super("report");
		this.setDescription("Report an issue to the moderators. Be descriptive!");
		this.setUsage("/report Your issue here. Be descriptive!");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args.length == 0) {
			return false;
		}
		if (args.length < 4) {
			sender.sendMessage(Color.BAD + "More descriptive, please!");
			return true;
		}
		Discord discord = Discord.getInstance();
		if (!discord.isEnabled()) {
			sender.sendMessage(Color.BAD + "Reporting is disabled at this time, sorry! Please /mail an admin instead.");
			return true;
		}
		Player player = (Player) sender;
		/* 
		 * Report format:
		 * Report by Name at Earth,x,y,z
		 * All parameters here
		 */
		StringBuilder sb = new StringBuilder("Report by ").append(player.getName()).append(" at ")
				.append(BukkitSerializer.locationToBlockCenterString(player.getLocation()))
				.append('\n').append(StringUtils.join(args, ' '));
		discord.postReport(player.getName(), sb.toString());
		player.sendMessage(Color.GOOD + "Report sent! Thanks for alerting us.");
		return true;
	}

}
