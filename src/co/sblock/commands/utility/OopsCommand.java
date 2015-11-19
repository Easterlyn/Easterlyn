package co.sblock.commands.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.discord.DiscordPlayer;

import net.md_5.bungee.api.ChatColor;

/**
 * Command to fix mistakes in the name of the last issued command.
 * 
 * @author Jikoo
 */
public class OopsCommand extends SblockCommand {

	// The reason for not using UUIDs is that this storage is trivial and should work for console.
	private final HashMap<String, String> oopsCommands;
	private final String oopsPrefix;
	private final List<String> aliases, reusable;

	public OopsCommand() {
		super("oops");
		this.setAliases("fuck", "opps");
		this.setDescription("Suggests fixes to commands.");
		this.setUsage("/oops");
		oopsCommands = new HashMap<>();
		oopsPrefix = Color.GOOD_EMPHASIS.toString() + ChatColor.BOLD + "Oops! " + Color.GOOD;
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
		sender.sendMessage(oopsPrefix + "Did you mean " + Color.COMMAND + '/' + command
				+ Color.GOOD + "? Run " + Color.COMMAND + "/oops" + Color.GOOD + '!');
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
		SimpleCommandMap commandMap = Sblock.getInstance().getCommandMap();
		if (commandMap.getCommand(commandName) != null) {
			// Valid command, nothing to oops.
			return null;
		}

		List<String> discordWhitelist;
		if (sender instanceof DiscordPlayer) {
			discordWhitelist = Sblock.getInstance().getConfig().getStringList("discord.command-whitelist");
		} else {
			discordWhitelist = null;
		}

		int matchLevel = Integer.MAX_VALUE;
		String correctCommandName = null;
		for (Command command : commandMap.getCommands()) {
			String permission = command.getPermission();
			// future support Essentials' terrible command system?
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

		// Allow more fuzziness for longer commands
		if (matchLevel < (3 + correctCommandName.length() / 4)) {
			return correctCommandName;
		}
		return null;
	}

	private List<String> getAllAliases(Command command) {
		reusable.clear();
		reusable.addAll(command.getAliases());
		reusable.add(command.getName());
		return reusable;
	}
}
