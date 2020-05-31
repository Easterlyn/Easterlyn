package com.easterlyn.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.BukkitRootCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.locales.MessageKey;
import com.easterlyn.EasterlynCore;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.Colors;
import com.easterlyn.util.PermissionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
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
		PermissionUtil.addParent("easterlyn.command.ping.self", UserRank.MEMBER);
		// TODO an addOther or some such method would be useful
		// Should we just dispose of the preset nodes?
		PermissionUtil.addParent("easterlyn.command.ping.other", UserRank.MODERATOR);
	}

	@Subcommand("reload")
	@Description("{commands.reload.description}")
	@CommandPermission("easterlyn.command.reload")
	public void reload() {
		plugin.reloadConfig();
		Colors.load(plugin);
		getCurrentCommandIssuer().sendInfo(CoreLang.SUCCESS);
	}

	@Subcommand("cmdinfo")
	@CommandAlias("commandinfo")
	@Description("{commands.commandinfo.description}")
	@Syntax("<commandName>")
	@CommandPermission("easterlyn.command.commandinfo")
	@CommandCompletion("@commands")
	public void commandInfo(CommandIssuer issuer, @Default("commandinfo") @Single String commandName) {

		SimpleCommandMap simpleCommandMap = plugin.getSimpleCommandMap();
		if (simpleCommandMap == null) {
			issuer.sendInfo(MessageKey.of("commands.commandinfo.error.null_map"));
			return;
		}
		Command command = simpleCommandMap.getCommand(commandName);
		if (command == null) {
			issuer.sendSyntax(MessageKeys.NO_COMMAND_MATCHED_SEARCH, "{search}", commandName);
			throw new InvalidCommandArgument(false);
		}
		issuer.sendInfo(MessageKey.of("commands.commandinfo.info.primary"), "{value}", command.getName());
		if (command.getAliases().size() > 0) {
			issuer.sendInfo(MessageKey.of("commands.commandinfo.info.aliases"), "{value}", String.join(", ", command.getAliases()));
		}
		issuer.sendInfo(MessageKey.of("commands.commandinfo.info.description"), "{value}", command.getDescription());
		issuer.sendInfo(MessageKey.of("commands.commandinfo.info.usage"), "{value}", command.getUsage());
		issuer.sendInfo(MessageKey.of("commands.commandinfo.info.permission"), "{value}", command.getPermission());
		String pluginName;
		if (command instanceof BukkitRootCommand) {
			pluginName = ((BukkitCommandManager) ((BukkitRootCommand) command).getManager()).getPlugin().getName();
		} else if (command instanceof PluginIdentifiableCommand) {
			pluginName = ((PluginIdentifiableCommand) command).getPlugin().getName();
		} else {
			issuer.sendInfo(MessageKey.of("commands.commandinfo.info.plugin_unknown"), "{value}", command.getClass().getName());
			return;
		}
		issuer.sendInfo(MessageKey.of("commands.commandinfo.info.plugin_known"), "{value}", pluginName);
	}

	@CommandAlias("ping")
	@Description("{commands.ping.description}")
	@CommandPermission("easterlyn.command.ping.self")
	@CommandCompletion("@playerOnlineIfPerm")
	public void ping(BukkitCommandIssuer issuer, @Flags(CoreContexts.ONLINE_WITH_PERM + "," + CoreContexts.ONLINE) Player player) {
		if (player.getLastLogin() > System.currentTimeMillis() - 15000) {
			issuer.sendInfo(MessageKey.of("commands.ping.error.small_sample"));
			return;
		}

		if (!(player instanceof CraftPlayer)) {
			issuer.sendInfo(MessageKey.of("commands.ping.error.implementation"));
			return;
		}

		CraftPlayer obcPlayer = (CraftPlayer) player;
		issuer.sendInfo(MessageKey.of("commands.ping.message"), "{player}", player.getName(), "{value}", String.valueOf(obcPlayer.getHandle().ping));

	}

}
