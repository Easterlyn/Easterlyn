package com.easterlyn.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
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
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.Colors;
import com.easterlyn.util.PermissionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Root command for Easterlyn's core functions.
 *
 * @author Jikoo
 */
@CommandAlias("easterlyn")
@CommandPermission("easterlyn.command.easterlyn")
public class EasterlynCommand extends BaseCommand {

	@Dependency
	private EasterlynCore plugin;

	public EasterlynCommand() {
		PermissionUtil.addParent("easterlyn.command.ping.other", UserRank.MODERATOR.getPermission());
	}

	@Subcommand("reload")
	@Description("Reload core configurations.")
	@CommandPermission("easterlyn.command.reload")
	@CommandRank(UserRank.HEAD_ADMIN)
	public void reload() {
		plugin.reloadConfig();
		Colors.load(plugin);
	}

	@Subcommand("cmdinfo")
	@CommandAlias("commandinfo")
	@Syntax("<commandName>")
	@CommandPermission("easterlyn.command.commandinfo")
	@CommandRank(UserRank.STAFF)
	public void commandInfo(CommandIssuer issuer, @Default("commandinfo") @Single String commandName) {

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

	@CommandAlias("ping")
	@Description("Check your connection to the server!")
	@CommandPermission("easterlyn.command.ping")
	public void ping(BukkitCommandIssuer issuer, Player player) {
		if (issuer.isPlayer() && !issuer.hasPermission("easterlyn.command.ping.other")) {
			player = issuer.getPlayer();
		}

		if (player.getLastLogin() > System.currentTimeMillis() - 15000) {
			issuer.sendError(MessageKeys.ERROR_PREFIX, "{message}", "Ping is wildly inaccurate just after login!");
			return;
		}

		if (!(player instanceof CraftPlayer)) {
			issuer.sendError(MessageKeys.ERROR_PREFIX, "{message}", "Unknown player implementation!");
			return;
		}

		CraftPlayer obcPlayer = (CraftPlayer) player;
		issuer.sendInfo(MessageKeys.INFO_MESSAGE, "{message}", player.getName() + "'s ping is " + obcPlayer.getHandle().ping + "ms!");

	}

}
