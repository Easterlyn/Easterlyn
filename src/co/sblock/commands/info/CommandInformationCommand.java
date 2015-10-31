package co.sblock.commands.info;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for printing out information about another command.
 * 
 * @author Jikoo
 */
public class CommandInformationCommand extends SblockCommand {

	public CommandInformationCommand() {
		super("cmdinfo");
		this.setDescription("Prints out information about the specified command.");
		this.setUsage("/cmdinfo <command>");
		this.setPermissionLevel("felt");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		Command command;
		if (args.length > 0) {
			command = Sblock.getInstance().getCommandMap().getCommand(args[0]);
		} else {
			command = Sblock.getInstance().getCommandMap().getCommand("cmdinfo");
		}
		if (command == null) {
			sender.sendMessage(Color.BAD + "Invalid command! /cmdinfo <command>");
			return true;
		}
		sender.sendMessage(Color.GOOD + "Primary command: " + Color.GOOD_EMPHASIS + command.getName());
		sender.sendMessage(Color.GOOD + "Description: " + Color.GOOD_EMPHASIS + command.getDescription());
		sender.sendMessage(Color.GOOD + "Usage: " + Color.GOOD_EMPHASIS + command.getUsage());
		sender.sendMessage(Color.GOOD + "Permission: " + Color.GOOD_EMPHASIS + command.getPermission());
		if (command.getAliases().size() > 0) {
			sender.sendMessage(Color.GOOD + "Aliases: " + Color.GOOD_EMPHASIS + command.getAliases());
		}
		if (command instanceof PluginIdentifiableCommand) {
			sender.sendMessage(Color.GOOD + "Owning plugin: " + Color.GOOD_EMPHASIS + ((PluginIdentifiableCommand) command).getPlugin().getName());
		} else {
			sender.sendMessage(Color.GOOD + "Command is most likely vanilla.");
			sender.sendMessage(Color.GOOD + "Class: " + Color.GOOD_EMPHASIS + command.getClass().getName());
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission()) || args.length > 1) {
			return ImmutableList.of();
		}
		if (args.length == 0) {
			return super.tabComplete(sender, alias, args);
		}
		args[0] = args[0].toLowerCase();
		List<String> matches = new ArrayList<>();
		try {
			for (String command : Sblock.getInstance().getAllCommandAliases()) {
				if (command.startsWith(args[0])) {
					matches.add(command);
				}
			}
			return matches;
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			return ImmutableList.of();
		}
	}
}
