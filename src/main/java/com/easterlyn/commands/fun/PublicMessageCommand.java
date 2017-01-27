package com.easterlyn.commands.fun;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.SblockCommand;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * [privatemessage;Username:BasementHero]:3[/privatemessage]
 * 
 * @author Jikoo
 */
public class PublicMessageCommand extends SblockCommand {

	public PublicMessageCommand(Easterlyn plugin) {
		super(plugin, "publicmessage");
		setDescription("Send a super private message.");
		setUsage("/publicmessage <name> <message content>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		if (args.length < 2) {
			return false;
		}
		((Player) sender).chat(new StringBuilder("[privatemessage;Username:").append(args[0])
				.append(']').append(StringUtils.join(args, ' ', 1, args.length))
				.append("[/privatemessage]").toString());
		return true;
	}

}
