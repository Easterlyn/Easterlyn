package com.easterlyn.command;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.ContextResolver;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import com.easterlyn.EasterlynCore;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.user.User;
import com.easterlyn.util.NumberUtil;
import com.easterlyn.util.PlayerUtil;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CoreContexts {

	public static final String SELF = "self";
	public static final String OFFLINE = "offline";
	public static final String ONLINE = "online";
	public static final String ONLINE_WITH_PERM = "otherWithPerm";

	private static final Pattern INTEGER_PATTERN = Pattern.compile("$(-?\\d+)[dl]?^", Pattern.CASE_INSENSITIVE);

	public static void register(EasterlynCore plugin) {
		ContextResolver<Integer, BukkitCommandExecutionContext> integerResolver = context -> {
			String firstArg = context.popFirstArg();
			Matcher matcher = INTEGER_PATTERN.matcher(firstArg);
			if (matcher.find()) {
				try {
					return Integer.valueOf(matcher.group(1));
				} catch (NumberFormatException e) {
					throw new InvalidCommandArgument(CoreLang.WHOLE_NUMBER);
				}
			}

			firstArg = firstArg.toUpperCase();
			if (firstArg.matches("[IVXLCDM]+")) {
				return NumberUtil.intFromRoman(firstArg);
			}

			throw new InvalidCommandArgument(CoreLang.WHOLE_NUMBER);
		};

		plugin.getCommandManager().getCommandContexts().registerContext(int.class, integerResolver);
		plugin.getCommandManager().getCommandContexts().registerContext(Integer.class, integerResolver);


		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(BukkitCommandIssuer.class,
				CommandExecutionContext::getIssuer);

		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(Player.class,
				new IssuerAwareContextResolver <Player, BukkitCommandExecutionContext>() {

			@Override
			public Player getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
				//noinspection unchecked // Type erasure is caused by command context providing raw RegisteredCommand
				if (context.hasFlag(SELF) || context.hasFlag(ONLINE_WITH_PERM) && context.getIssuer().isPlayer()
						&& context.getCmd().getRequiredPermissions().stream().noneMatch(perm ->
						context.getIssuer().hasPermission(perm.toString().replace(".self", ".other")))) {
					return getSelf(context.getIssuer());
				}

				if (context.hasFlag(ONLINE_WITH_PERM)) {
					Player player = null;
					String firstArg = context.getFirstArg();
					if (firstArg != null && firstArg.length() > 3) {
						try {
							player = getOnline(context.getIssuer(), firstArg);
						} catch (InvalidCommandArgument ignored) {
						}
					}
					if (player != null) {
						context.popFirstArg();
						return player;
					}
					return getSelf(context.getIssuer());
				}

				if (context.hasFlag(ONLINE)) {
					return getOnline(context.getIssuer(), context.popFirstArg());
				}

				if (context.hasFlag(OFFLINE)) {
					return getOffline(context.getIssuer(), context.popFirstArg());
				}

				try {
					String firstArg = context.getFirstArg();
					Player other = getOnline(context.getIssuer(), firstArg);
					context.popFirstArg();
					return other;
				} catch (InvalidCommandArgument ignored) {}
				return getSelf(context.getIssuer());
			}

			private @NotNull Player getSelf(@NotNull BukkitCommandIssuer issuer) throws InvalidCommandArgument {
				if (issuer.isPlayer()) {
					return issuer.getPlayer();
				}
				throw new InvalidCommandArgument(CoreLang.NO_CONSOLE);
			}

			private @NotNull Player getOnline(@NotNull BukkitCommandIssuer issuer, @NotNull String argument) throws InvalidCommandArgument {
				Player player = PlayerUtil.matchOnlinePlayer(issuer.getIssuer(), argument);
				if (player == null) {
					throw new InvalidCommandArgument(CoreLang.INVALID_PLAYER, "{value}", argument);
				}
				return player;
			}

			private @NotNull Player getOffline(@NotNull BukkitCommandIssuer issuer,
					@NotNull String argument) throws InvalidCommandArgument {
				Player player;
				try {
					player = PlayerUtil.matchPlayer(issuer.getIssuer(), argument, true, plugin);
				} catch (IllegalAccessException e) {
					ReportableEvent.call("Called PlayerUtil#matchPlayer on the main thread while executing!", e, 5);
					player = PlayerUtil.matchOnlinePlayer(issuer.getIssuer(), argument);
				}
				if (player == null) {
					throw new InvalidCommandArgument(CoreLang.INVALID_PLAYER, "{value}", argument);
				}
				return player;
			}
		});

		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(ConsoleCommandSender.class, context -> {
			if (!(context.getIssuer().getIssuer() instanceof ConsoleCommandSender)) {
				throw new InvalidCommandArgument(CoreLang.ONLY_CONSOLE);
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

	private CoreContexts() {}

}
