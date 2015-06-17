package co.sblock.commands.chat;

import org.apache.commons.lang.StringUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.commands.SblockCommand;

/**
 * [privatemessage;Username:BasementHero]:3[/privatemessage]
 * 
 * @author Jikoo
 */
public class PublicMessageCommand extends SblockCommand {

	public PublicMessageCommand() {
		super("publicmessage");
		setDescription("Send a super private message.");
		setUsage("/publicmessage <name> <message content>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args.length < 2) {
			return false;
		}
		((Player) sender).chat(new StringBuilder("[privatemessage;Username:").append(args[0])
				.append(']').append(StringUtils.join(args, ' ', 1, args.length - 1))
				.append("[/privatemessage]").toString());
		return true;
	}

}
