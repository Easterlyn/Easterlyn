package co.sblock.commands;

import java.util.Arrays;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.discord.Discord;
import co.sblock.utilities.PermissionUtils;
import co.sblock.utilities.TextUtils;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

/**
 * Base to be extended by all commands registered by Sblock.
 * 
 * @author Jikoo
 */
public abstract class SblockCommand extends Command implements PluginIdentifiableCommand {

	private String permissionLevel;
	private final Sblock plugin;
	private final Language lang;

	public SblockCommand(Sblock plugin, String name) {
		super(name);
		this.plugin = plugin;
		this.lang = plugin.getModule(Language.class);
		this.setDescription(lang.getValue("command." + name + ".description", "A Sblock command."));
		this.setUsage(lang.getValue("command." + name + ".usage", "/" + name));
		this.setPermission("sblock.command." + name);
		this.setPermissionLevel("default");
		this.setPermissionMessage("By the order of the Jarl, stop right there!");
	}

	@Override
	public Command setUsage(String usage) {
		return super.setUsage(Language.getColor("bad") + ChatColor.translateAlternateColorCodes('&', usage));
	}

	@Override
	public Command setDescription(String description) {
		return super.setDescription(Language.getColor("neutral") + ChatColor.translateAlternateColorCodes('&', description));
	}

	public Command setPermissionLevel(String group) {
		this.permissionLevel = "sblock." + group;
		return this;
	}

	public String getPermissionLevel() {
		return this.permissionLevel;
	}

	public void addExtraPermission(String permissionSegment, String group) {
		permissionSegment = this.getPermission() + '.' + permissionSegment;
		Permission permission = PermissionUtils.getOrCreate(permissionSegment, PermissionDefault.OP);
		permission.addParent("sblock.command.*", true).recalculatePermissibles();
		permission.addParent("sblock." + group, true).recalculatePermissibles();
	}

	@Override
	public Command setPermissionMessage(String permissionMessage) {
		return super.setPermissionMessage(Language.getColor("bad") + ChatColor.translateAlternateColorCodes('&', permissionMessage));
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
		try {
			if (onCommand(sender, label, args)) {
				return true;
			}
		} catch (Exception e) {
			sender.sendMessage(Language.getColor("bad") + "An error occurred processing this command. Please make sure your parameters are correct.");
			sender.sendMessage(this.getUsage());
			plugin.getModule(Discord.class).postReport("Error processing command by " + sender.getName()
					+ ": /" + getName() + " " + StringUtils.join(args, ' ') + '\n' + TextUtils.getTrace(e, 5));
			e.printStackTrace();
		}
		sender.sendMessage(this.getUsage());
		return true;
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
	public final Plugin getPlugin() {
		return plugin;
	}

	protected final Language getLang() {
		return this.lang;
	}

}
