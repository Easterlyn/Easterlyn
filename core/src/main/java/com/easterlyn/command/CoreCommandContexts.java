package com.easterlyn.command;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import com.easterlyn.EasterlynCore;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.user.User;
import com.easterlyn.util.NumberUtil;
import com.easterlyn.util.PlayerUtil;
import java.util.Date;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CoreCommandContexts {

	public static void register(EasterlynCore plugin) {
		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(BukkitCommandIssuer.class,
				CommandExecutionContext::getIssuer);

		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(Player.class,
				new IssuerAwareContextResolver <Player, BukkitCommandExecutionContext>() {

			@Override
			public Player getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
				//noinspection unchecked // Type erasure is caused by command context providing raw RegisteredCommand
				if (context.hasFlag(CommandFlags.SELF) || context.hasFlag(CommandFlags.OTHER_WITH_PERM) && context.getIssuer().isPlayer()
						&& context.getCmd().getRequiredPermissions().stream().anyMatch(perm -> context.getIssuer().hasPermission(perm + ".other"))) {
					return getSelf(context.getIssuer());
				}

				if (context.hasFlag(CommandFlags.ONLINE)) {
					return getOnline(context.getIssuer(), context.popFirstArg());
				}

				if (context.hasFlag(CommandFlags.OFFLINE)) {
					return getOffline(context.getIssuer(), context.popFirstArg());
				}

				if (context.hasFlag(CommandFlags.OTHER)) {
					return getOther(context.getIssuer(), context.popFirstArg());
				}

				try {
					String firstArg = context.getFirstArg();
					Player other = getOther(context.getIssuer(), firstArg);
					context.popFirstArg();
					return other;
				} catch (InvalidCommandArgument ignored) {}
				return getSelf(context.getIssuer());
			}

			@NotNull
			Player getSelf(@NotNull BukkitCommandIssuer issuer) throws InvalidCommandArgument {
				if (issuer.isPlayer()) {
					return issuer.getPlayer();
				}
				throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE);
			}

			@NotNull
			Player getOther(@NotNull BukkitCommandIssuer issuer, @NotNull String argument) throws InvalidCommandArgument {
				Player player;
				try {
					player = PlayerUtil.matchPlayer(issuer.getIssuer(), argument, false, plugin);
				} catch (IllegalAccessException e) {
					plugin.getServer().getPluginManager().callEvent(new ReportableEvent(
							"Called PlayerUtil#matchPlayer on the main thread while executing!", e, 5));
					player = PlayerUtil.matchOnlinePlayer(issuer.getIssuer(), argument);
				}
				if (player == null) {
					throw new InvalidCommandArgument("Invalid player " + argument + "!");
				}
				return player;
			}

			@NotNull
			Player getOnline(@NotNull BukkitCommandIssuer issuer, @NotNull String argument) throws InvalidCommandArgument {
				Player player = PlayerUtil.matchOnlinePlayer(issuer.getIssuer(), argument);
				if (player == null) {
					throw new InvalidCommandArgument("Invalid player " + argument + "!");
				}
				return player;
			}

			@NotNull
			Player getOffline(@NotNull BukkitCommandIssuer issuer,
					@NotNull String argument) throws InvalidCommandArgument {
				Player player;
				try {
					player = PlayerUtil.matchPlayer(issuer.getIssuer(), argument, true, plugin);
				} catch (IllegalAccessException e) {
					plugin.getServer().getPluginManager().callEvent(new ReportableEvent(
							"Called PlayerUtil#matchPlayer on the main thread while executing!", e, 5));
					player = PlayerUtil.matchOnlinePlayer(issuer.getIssuer(), argument);
				}
				if (player == null) {
					throw new InvalidCommandArgument("Invalid player " + argument + "!");
				}
				return player;
			}
		});

		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(ConsoleCommandSender.class, context -> {
			if (!(context.getIssuer().getIssuer() instanceof ConsoleCommandSender)) {
				throw new InvalidCommandArgument("Only allowed on console.");
			}
			return (ConsoleCommandSender) context.getIssuer().getIssuer();
		});

		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(User.class, context -> {
			Player player = (Player) plugin.getCommandManager().getCommandContexts().getResolver(Player.class).getContext(context);
			return plugin.getUserManager().getUser(player.getUniqueId());
		});

		plugin.getCommandManager().getCommandContexts().registerContext(Date.class, context -> {
			String firstArg = context.popFirstArg();
			long duration = NumberUtil.parseDuration(firstArg);
			return new Date(Math.addExact(System.currentTimeMillis(), duration));
		});
	}

	private CoreCommandContexts() {}

}
