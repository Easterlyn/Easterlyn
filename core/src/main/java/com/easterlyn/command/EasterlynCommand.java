package com.easterlyn.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.BukkitRootCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.Easterlyn;
import com.easterlyn.users.UserRank;
import com.easterlyn.util.Colors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;

/**
 * Root command for Easterlyn's core functions.
 *
 * @author Jikoo
 */
@CommandAlias("easterlyn")
@CommandPermission("easterlyn.command.easterlyn")
public class EasterlynCommand extends BaseCommand {

	@Dependency
	private Easterlyn plugin;

	public EasterlynCommand() {}

	@Subcommand("reload")
	@Description("Reload core configurations.")
	@CommandPermission("easterlyn.command.reload")
	@CommandRank(UserRank.HEAD_ADMIN)
	public void reload() {
		plugin.reloadConfig();
		Colors.load(plugin);
	}

	@CommandAlias("colour|color")
	@Description("Taste the rainbow!")
	@CommandPermission("easterlyn.command.colour")
	public void colour(CommandIssuer issuer) {
		StringBuilder builder = new StringBuilder();
		for (ChatColor color : ChatColor.values()) {
			builder.append(color).append('&').append(color.toString().substring(1)).append(' ');
			builder.append(color.name().toLowerCase()).append(ChatColor.RESET).append(' ');
		}
		issuer.sendMessage(builder.toString());
	}

	@Subcommand("lel")
	@Description("【ＴＡＳＴＥ　ＴＨＥ　ＰＡＩＮＢＯＷ】")
	@Syntax("[painful to view sentence]")
	@CommandPermission("easterlyn.command.lel")
	@CommandRank(UserRank.ADMIN)
	public void requestLordEnglishEyeFuck(String message) {
		ChatColor[] rainbow = { ChatColor.DARK_RED, ChatColor.RED, ChatColor.GOLD,
			ChatColor.YELLOW, ChatColor.GREEN, ChatColor.DARK_GREEN, ChatColor.AQUA,
			ChatColor.DARK_AQUA, ChatColor.BLUE, ChatColor.DARK_BLUE, ChatColor.LIGHT_PURPLE,
			ChatColor.DARK_PURPLE };
		StringBuilder lelOut = new StringBuilder();
		for (int i = 0; i < message.length();) {
			for (int j = 0; i < message.length() && j < rainbow.length; ++i, ++j) {
				char charAt = message.charAt(i);
				if (Character.isWhitespace(charAt)) {
					--j;
					lelOut.append(i);
					continue;
				}
				lelOut.append(rainbow[j]).append(ChatColor.MAGIC).append(charAt);
			}
		}
		Bukkit.broadcastMessage(lelOut.substring(0, lelOut.length() - 1 > 0 ? lelOut.length() - 1 : 0));
	}

	@Subcommand("cmdinfo")
	@CommandAlias("cmdinfo|commandinfo")
	@Syntax("<commandName>")
	@CommandPermission("easterlyn.command.commandinfo")
	@CommandRank(UserRank.STAFF)
	public void commandInfo(CommandIssuer issuer, @Default("cmdinfo") String commandName) {

		SimpleCommandMap simpleCommandMap = plugin.getSimpleCommandMap();
		if (simpleCommandMap == null) {
			issuer.sendError(MessageKeys.ERROR_PREFIX, "{message}", "SimpleCommandMap is null! Please check console.");
			return;
		}
		Command command = simpleCommandMap.getCommand(commandName);
		if (command == null) {
			issuer.sendSyntax(MessageKeys.NO_COMMAND_MATCHED_SEARCH, "{search}", commandName);
			throw new InvalidCommandArgument(false);
		}
		issuer.sendInfo(MessageKeys.INFO_MESSAGE, "{message}", "<c1>Primary command:</c1> <c2>" + command.getName() + "</c2>");
		issuer.sendInfo(MessageKeys.INFO_MESSAGE, "{message}", "<c1>Description:</c1> <c2>" + command.getDescription() + "</c2>");
		issuer.sendInfo(MessageKeys.INFO_MESSAGE, "{message}", "<c1>Usage:</c1> <c2>" + command.getUsage() + "</c2>");
		issuer.sendInfo(MessageKeys.INFO_MESSAGE, "{message}", "<c1>Permission:</c1> <c2>" + command.getPermission() + "</c2>");
		if (command.getAliases().size() > 0) {
			issuer.sendInfo(MessageKeys.INFO_MESSAGE, "{message}", "<c1>Aliases:</c1> <c2>" + command.getAliases() + "</c2>");
		}
		String pluginName;
		if (command instanceof BukkitRootCommand) {
			pluginName = ((BukkitCommandManager) ((BukkitRootCommand) command).getManager()).getPlugin().getName();
		} else if (command instanceof PluginIdentifiableCommand) {
			pluginName = ((PluginIdentifiableCommand) command).getPlugin().getName();
		} else {
			issuer.sendInfo(MessageKeys.INFO_MESSAGE, "{message}", "<c1>Command is most likely vanilla.</c1>");
			issuer.sendInfo(MessageKeys.INFO_MESSAGE, "{message}", "<c1>Class:</c1> <c2>" + command.getClass().getName() + "</c2>");
			return;
		}
		issuer.sendInfo(MessageKeys.INFO_MESSAGE, "{message}", "<c1>Owning plugin:</c1> <c2>" + pluginName + "</c2>");
	}

}
