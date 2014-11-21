package co.sblock.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

/**
 * Base to be extended by all commands registered by Sblock.
 * 
 * @author Jikoo
 */
public abstract class SblockCommand extends Command implements PluginIdentifiableCommand {

	public SblockCommand(String name) {
		super(name);
		this.setDescription("A Sblock command.");
		this.setUsage("/<command>");
		this.setPermission(null);
		this.setPermissionMessage("By the order of the Jarl, stop right there!");
	}

	@Override
	public Command setUsage(String usage) {
		usage = ChatColor.RED + ChatColor.translateAlternateColorCodes('&', usage);
		return super.setUsage(usage);
	}

	@Override
	public Command setDescription(String description) {
		description = ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', description);
		return super.setDescription(description);
	}

	@Override
	public Command setPermissionMessage(String permissionMessage) {
		permissionMessage = ChatColor.RED + ChatColor.translateAlternateColorCodes('&', permissionMessage);
		return super.setPermissionMessage(permissionMessage);
	}

	@Override
	public abstract boolean execute(CommandSender sender, String label, String[] args);

	@Override
	public Plugin getPlugin() {
		return Bukkit.getPluginManager().getPlugin("Sblock");
	}
}
