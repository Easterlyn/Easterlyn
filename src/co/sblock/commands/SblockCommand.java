package co.sblock.commands;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import co.sblock.Sblock;

/**
 * Base to be extended by all commands registered by Sblock.
 * 
 * @author Jikoo
 */
public abstract class SblockCommand extends Command implements PluginIdentifiableCommand {

	private String permissionLevel;

	public SblockCommand(String name) {
		super(name);
		this.setDescription("A Sblock command.");
		this.setUsage("/<command>");
		this.setPermission("sblock.command." + name);
		this.setPermissionLevel("default");
		this.setPermissionMessage("By the order of the Jarl, stop right there!");
	}

	@Override
	public Command setUsage(String usage) {
		return super.setUsage(ChatColor.RED + ChatColor.translateAlternateColorCodes('&', usage));
	}

	@Override
	public Command setDescription(String description) {
		return super.setDescription(ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', description));
	}

	public Command setPermissionLevel(String group) {
		this.permissionLevel = "sblock." + group;
		return this;
	}

	public String getPermissionLevel() {
		return this.permissionLevel;
	}

	@Override
	public Command setPermissionMessage(String permissionMessage) {
		return super.setPermissionMessage(ChatColor.RED + ChatColor.translateAlternateColorCodes('&', permissionMessage));
	}

	public Command setAliases(String... aliases) {
		return this.setAliases(Arrays.asList(aliases));
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (this.getPermission() != null && !sender.hasPermission(this.getPermission())) {
			sender.sendMessage(this.getPermissionMessage());
			return true;
		}
		return onCommand(sender, label, args);
	}

	protected abstract boolean onCommand(CommandSender sender, String label, String[] args);

	@Override
	public java.util.List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (getPermission() != null && !sender.hasPermission(getPermission())) {
			return com.google.common.collect.ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}

	@Override
	public Plugin getPlugin() {
		return Sblock.getInstance();
	}
}
