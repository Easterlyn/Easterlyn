package co.sblock.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;

import co.sblock.Sblock;

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
		this.setPermission("group.felt");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		Command command;
		if (args.length > 0) {
			command = Sblock.getInstance().getCommandMap().getCommand(args[0]);
		} else {
			command = Sblock.getInstance().getCommandMap().getCommand("cmdinfo");
		}
		if (command == null) {
			return false;
		}
		sender.sendMessage(ChatColor.DARK_AQUA + "Primary command: " + ChatColor.YELLOW + command.getName());
		sender.sendMessage(ChatColor.DARK_AQUA + "Description: " + ChatColor.YELLOW + command.getDescription());
		sender.sendMessage(ChatColor.DARK_AQUA + "Usage: " + ChatColor.YELLOW + command.getUsage());
		sender.sendMessage(ChatColor.DARK_AQUA + "Permission: " + ChatColor.YELLOW + command.getPermission());
		if (command.getAliases().size() > 0) {
			sender.sendMessage(ChatColor.DARK_AQUA + "Aliases: " + ChatColor.YELLOW + command.getAliases());
		}
		if (command instanceof PluginIdentifiableCommand) {
			sender.sendMessage(ChatColor.DARK_AQUA + "Owning plugin: " + ChatColor.YELLOW + ((PluginIdentifiableCommand) command).getPlugin().getName());
		} else {
			sender.sendMessage(ChatColor.DARK_AQUA + "Command is most likely vanilla.");
			sender.sendMessage(ChatColor.DARK_AQUA + "Class: " + ChatColor.YELLOW + command.getClass().getName());
		}
		return true;
	}
}
