package com.easterlyn.commands.fun;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;

import com.easterlyn.utilities.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * [privatemessage;Username:BasementHero]:3[/privatemessage]
 *
 * @author Jikoo
 */
public class PublicMessageCommand extends EasterlynCommand {

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
		((Player) sender).chat("[privatemessage;Username:" + args[0] + ']'
				+ TextUtils.join(args, ' ', 1, args.length) + "[/privatemessage]");
		return true;
	}

}
