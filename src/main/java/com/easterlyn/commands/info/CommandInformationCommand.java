package com.easterlyn.commands.info;

import java.util.ArrayList;
import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.SblockCommand;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;

/**
 * SblockCommand for printing out information about another command.
 * 
 * @author Jikoo
 */
public class CommandInformationCommand extends SblockCommand {

	public CommandInformationCommand(Easterlyn plugin) {
		super(plugin, "cmdinfo");
		this.setDescription("Prints out information about the specified command.");
		this.setPermissionLevel(UserRank.FELT);
		this.setUsage("/cmdinfo <command>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		Command command;
		if (args.length > 0) {
			command = ((Easterlyn) getPlugin()).getCommandMap().getCommand(args[0]);
		} else {
			command = ((Easterlyn) getPlugin()).getCommandMap().getCommand("cmdinfo");
		}
		if (command == null) {
			sender.sendMessage(Language.getColor("bad") + "Invalid command! /cmdinfo <command>");
			return true;
		}
		sender.sendMessage(Language.getColor("emphasis.neutral") + "Primary command: " + Language.getColor("neutral") + command.getName());
		sender.sendMessage(Language.getColor("emphasis.neutral") + "Description: " + Language.getColor("neutral") + command.getDescription());
		sender.sendMessage(Language.getColor("emphasis.neutral") + "Usage: " + Language.getColor("neutral") + command.getUsage());
		sender.sendMessage(Language.getColor("emphasis.neutral") + "Permission: " + Language.getColor("neutral") + command.getPermission());
		if (command.getAliases().size() > 0) {
			sender.sendMessage(Language.getColor("emphasis.neutral") + "Aliases: " + Language.getColor("neutral") + command.getAliases());
		}
		if (command instanceof PluginIdentifiableCommand) {
			sender.sendMessage(Language.getColor("emphasis.neutral") + "Owning plugin: " + Language.getColor("neutral") + ((PluginIdentifiableCommand) command).getPlugin().getName());
		} else {
			sender.sendMessage(Language.getColor("emphasis.neutral") + "Command is most likely vanilla.");
			sender.sendMessage(Language.getColor("emphasis.neutral") + "Class: " + Language.getColor("neutral") + command.getClass().getName());
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
			for (String command : ((Easterlyn) getPlugin()).getAllCommandAliases()) {
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
