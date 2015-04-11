package co.sblock.commands.chat;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for most manipulation of chat features.
 * 
 * @author Jikoo
 */
public class SblockChatCommand extends SblockCommand {

	public SblockChatCommand() {
		super("sc");
		this.setDescription("SblockChat's main command");
		this.setUsage(ChatColor.YELLOW + "Chat-related commands:\n"
						+ ChatColor.AQUA + "/focus <channel>"
						+ ChatColor.YELLOW + ": Talking will send messages to <channel>.\n"
						+ ChatColor.AQUA + "/listen <channel>"
						+ ChatColor.YELLOW + ": Recieve messages from <channel>.\n"
						+ ChatColor.AQUA + "/leave <channel>"
						+ ChatColor.YELLOW + ": Stop listening to <channel>.\n"
						+ ChatColor.AQUA + "/channel list"
						+ ChatColor.YELLOW + ": List all channels you are listening to.\n"
						+ ChatColor.AQUA + "/channel listall"
						+ ChatColor.YELLOW + ": List all channels.\n"
						+ ChatColor.AQUA + "/nick remove|list|<nick choice>"
						+ ChatColor.YELLOW + ": Set a nick in a Nick/RP channel.\n"
						+ ChatColor.AQUA + "/suppress"
						+ ChatColor.YELLOW + ": Toggle ignoring global channels.\n"
						+ ChatColor.AQUA + "/channel"
						+ ChatColor.YELLOW + ": Channel creation/moderation commands.");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args == null || args.length == 0) {
			sender.sendMessage(this.getUsage());
			return true;
		}

		sender.sendMessage(ChatColor.RED + "/sc is being phased out! Please check /chat for alternatives!");

		String command;
		args[0] = args[0].toLowerCase();
		switch (args[0]) {
		case "c":
			args[0] = "focus";
			command = StringUtils.join(args, ' ');
			break;
		case "l":
			args[0] = "listen";
		case "listen":
		case "leave":
		case "nick":
		case "suppress":
		case "channel":
			command = StringUtils.join(args, ' ');
			break;
		case "list":
		case "listall":
		case "new":
			command = "channel " + StringUtils.join(args, ' ');
			break;
		default:
			sender.sendMessage(this.usageMessage);
			return true;
		}
		sender.sendMessage(ChatColor.RED + "Running " + ChatColor.AQUA + "/" + command);
		Bukkit.dispatchCommand(sender, command);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
