package co.sblock.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * SblockCommand for performing an emote.
 * 
 * @author Jikoo
 */
public class MeCommand extends SblockCommand {

	public MeCommand() {
		super("me");
		this.setDescription("#>does an action");
		this.setUsage("YOU FOOKIN WOT M8? /me (@channel) <message> Channel optional, defaults current.");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args.length == 0) {
			return false;
		}
		((Player) sender).chat("#>" + StringUtils.join(args, ' '));
		return true;
	}

	// TODO if arg == 0 && char at 0 is @ tab complete with channels
}
