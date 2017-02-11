package com.easterlyn.commands.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.discord.DiscordPlayer;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;

/**
 * Command to fix mistakes in the name of the last issued command.
 * 
 * @author Jikoo
 */
public class OopsCommand extends EasterlynCommand {

	// The reason for not using UUIDs is that this storage is trivial and should work for console.
	private final HashMap<String, String> oopsCommands;
	private final List<String> aliases, reusable;

	public OopsCommand(Easterlyn plugin) {
		super(plugin, "oops");
		this.setAliases("opps");
		oopsCommands = new HashMap<>();
		reusable = new ArrayList<>();
		aliases = new ArrayList<>(this.getAllAliases(this));
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (oopsCommands.containsKey(sender.getName())) {
			Bukkit.dispatchCommand(sender, oopsCommands.remove(sender.getName()));
		}
		return true;
	}

	// TODO allow for console too?
	public boolean handleFailedCommand(CommandSender sender, String commandName, String commandLine) {
		String command = getMatchingCommand(sender, commandName);
		if (command == null) {
			// Valid or severely invalid command
			if (!aliases.contains(commandName) && oopsCommands.containsKey(sender.getName())) {
				oopsCommands.remove(sender.getName());
			}
			return false;
		}
		sender.sendMessage(getLang().getValue("command.oops.message").replace("{COMMAND}", command));
		if (aliases.contains(command)) {
			// Don't store /oops as anyone's /oops
			return true;
		}
		if (commandLine != null) {
			command += ' ' + commandLine;
		}
		oopsCommands.put(sender.getName(), command);
		return true;
	}

	private String getMatchingCommand(CommandSender sender, String commandName) {
		SimpleCommandMap commandMap = ((Easterlyn) getPlugin()).getCommandMap();
		if (commandMap.getCommand(commandName) != null) {
			// Valid command, nothing to oops.
			return null;
		}

		List<String> discordWhitelist;
		if (sender instanceof DiscordPlayer) {
			discordWhitelist = getPlugin().getConfig().getStringList("discord.command-whitelist");
		} else {
			discordWhitelist = null;
		}

		int matchLevel = Integer.MAX_VALUE;
		String correctCommandName = null;
		for (Command command : commandMap.getCommands()) {
			String permission = command.getPermission();
			if (permission != null && !sender.hasPermission(permission)) {
				// Can't use the command, don't check.
				continue;
			}
			if (discordWhitelist != null && !discordWhitelist.contains(command.getName())) {
				continue;
			}
			for (String alias : getAllAliases(command)) {
				int current = StringUtils.getLevenshteinDistance(commandName, alias);
				if (current == 0) {
					return null;
				}
				if (current < matchLevel) {
					matchLevel = current;
					correctCommandName = alias;
				}
			}
		}

		return correctCommandName;
	}

	private List<String> getAllAliases(Command command) {
		reusable.clear();
		reusable.addAll(command.getAliases());
		reusable.add(command.getName());
		return reusable;
	}
}
