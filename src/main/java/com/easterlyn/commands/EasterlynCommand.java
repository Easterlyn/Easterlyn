package com.easterlyn.commands;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.discord.Discord;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.TextUtils;
import com.easterlyn.utilities.player.PermissionUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Base to be extended by all commands registered by Easterlyn.
 *
 * @author Jikoo
 */
public abstract class EasterlynCommand extends Command implements PluginIdentifiableCommand {

	private UserRank permissionLevel;
	private final Easterlyn plugin;
	private final Language lang;

	protected EasterlynCommand(Easterlyn plugin, String name) {
		super(name);
		this.plugin = plugin;
		this.lang = plugin.getModule(Language.class);
		this.setDescription(lang.getValue("command." + name + ".description", "A Easterlyn command."));
		this.setUsage(lang.getValue("command." + name + ".usage", "/" + name));
		this.setPermission("easterlyn.command." + name);
		this.setPermissionLevel(UserRank.MEMBER);
		this.setPermissionMessage("By the order of the Jarl, stop right there!");
	}

	@NotNull
	@Override
	public Command setUsage(@NotNull String usage) {
		return super.setUsage(Language.getColor("bad") + ChatColor.translateAlternateColorCodes('&', usage));
	}

	@NotNull
	@Override
	public Command setDescription(@NotNull String description) {
		return super.setDescription(Language.getColor("neutral") + ChatColor.translateAlternateColorCodes('&', description));
	}

	public void setPermissionLevel(UserRank rank) {
		this.permissionLevel = rank;
	}

	public UserRank getPermissionLevel() {
		return this.permissionLevel;
	}

	public void addExtraPermission(String permissionSegment, UserRank rank) {
		permissionSegment = this.getPermission() + '.' + permissionSegment;
		Permission permission = PermissionUtils.getOrCreate(permissionSegment, PermissionDefault.FALSE);
		permission.addParent("easterlyn.command.*", true).recalculatePermissibles();
		permission.addParent(rank.getPermission(), true).recalculatePermissibles();
	}

	@NotNull
	@Override
	public Command setPermissionMessage(String permissionMessage) {
		return super.setPermissionMessage(Language.getColor("bad") + ChatColor.translateAlternateColorCodes('&', permissionMessage));
	}

	public void setAliases(String... aliases) {
		this.setAliases(Arrays.asList(aliases));
	}

	@Override
	public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (this.getPermission() != null && !sender.hasPermission(this.getPermission())) {
			if (this.getPermissionMessage() != null) {
				sender.sendMessage(this.getPermissionMessage());
			}
			return true;
		}
		try {
			if (onCommand(sender, label, args)) {
				return true;
			}
		} catch (Exception e) {
			sender.sendMessage(Language.getColor("bad") + "An error occurred processing this command. Please make sure your parameters are correct.");
			plugin.getModule(Discord.class).postReport("Error processing command by " + sender.getName()
					+ ": /" + getName() + " " + TextUtils.join(args, ' ') + '\n' + TextUtils.getTrace(e, 5));
			e.printStackTrace();
		}
		sender.sendMessage(this.getUsage());
		return true;
	}

	protected abstract boolean onCommand(CommandSender sender, String label, String[] args);

	// TODO revisit tab completion, implement location tab completion
	@NotNull
	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args)
			throws IllegalArgumentException {
		if (this.getPermission() != null && this.getPermission().isEmpty()
				&& !sender.hasPermission(getPermission())) {
			return com.google.common.collect.ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}

	@NotNull
	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, Location location) throws IllegalArgumentException {
		return this.tabComplete(sender, alias, args);
	}

	protected List<String> completeArgument(String argument, String... completions) {
		List<String> validCompletions = new ArrayList<>();
		for (String completion : completions) {
			if (StringUtil.startsWithIgnoreCase(completion, argument)) {
				validCompletions.add(completion);
			}
		}
		return validCompletions;
	}

	@NotNull
	@Override
	public final Plugin getPlugin() {
		return plugin;
	}

	protected final Language getLang() {
		return this.lang;
	}

}
