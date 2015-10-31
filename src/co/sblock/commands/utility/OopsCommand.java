package co.sblock.commands.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;

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

	public OopsCommand() {
		super("oops");
		this.setAliases("fuck", "opps");
		this.setDescription("Suggests fixes to commands.");
		this.setUsage("/oops");
		oopsCommands = new HashMap<>();
		oopsPrefix = Color.GOOD_EMPHASIS.toString() + ChatColor.BOLD + "Oops! " + Color.GOOD;
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
			if (!commandName.equals("oops") && !commandName.equals("fuck")
					&& oopsCommands.containsKey(sender.getName())) {
				oopsCommands.remove(sender.getName());
			}
			return false;
		}
		sender.sendMessage(oopsPrefix + "Did you mean " + Color.COMMAND + '/' + command
				+ Color.GOOD + "? Run " + Color.COMMAND + "/oops" + Color.GOOD + '!');
		if (commandLine != null) {
			command += ' ' + commandLine;
		}
		oopsCommands.put(sender.getName(), command);
		return true;
	}

	private String getMatchingCommand(CommandSender sender, String commandName) {
		int matchLevel = Integer.MAX_VALUE;
		String correctCommandName = null;
		for (Command command : Sblock.getInstance().getCommandMap().getCommands()) {
			String permission = command.getPermission();
			// future support Essentials' terrible command system?
			if (permission != null && !sender.hasPermission(permission)) {
				// Can't use the command, don't check.
				continue;
			}
			for (String alias : getAllAliases(command)) {
				int current = StringUtils.getLevenshteinDistance(commandName, alias);
				if (current == 0) {
					// Valid command, abort abort
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
		ArrayList<String> aliases = new ArrayList<>(command.getAliases());
		aliases.add(command.getName());
		return aliases;
	}
}
