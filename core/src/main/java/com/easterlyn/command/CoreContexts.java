package com.easterlyn.command;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.contexts.ContextResolver;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import com.easterlyn.EasterlynCore;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.user.User;
import com.easterlyn.util.Colors;
import com.easterlyn.util.NumberUtil;
import com.easterlyn.util.PlayerUtil;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CoreContexts {

	public static final String SELF = "self";
	public static final String OFFLINE = "offline";
	public static final String ONLINE = "online";
	public static final String ONLINE_WITH_PERM = "otherWithPerm";

	private static final Pattern INTEGER_PATTERN = Pattern.compile("(-?\\d+)[dl]?", Pattern.CASE_INSENSITIVE);

	public static void register(EasterlynCore plugin) {
		ContextResolver<Long, BukkitCommandExecutionContext> longResolver = context -> {
			String firstArg = context.popFirstArg();
			Matcher matcher = INTEGER_PATTERN.matcher(firstArg);
			if (matcher.find()) {
				try {
					return Long.valueOf(matcher.group(1));
				} catch (NumberFormatException e) {
					throw new InvalidCommandArgument(CoreLang.WHOLE_NUMBER);
				}
			}

			firstArg = firstArg.toUpperCase();
			if (firstArg.matches("[IVXLCDM]+")) {
				return (long) NumberUtil.intFromRoman(firstArg);
			}

			throw new InvalidCommandArgument(CoreLang.WHOLE_NUMBER);
		};

		plugin.getCommandManager().getCommandContexts().registerContext(long.class, longResolver);
		plugin.getCommandManager().getCommandContexts().registerContext(Long.class, longResolver);

		ContextResolver<Integer, BukkitCommandExecutionContext> intResolver = context -> Math.toIntExact(longResolver.getContext(context));

		plugin.getCommandManager().getCommandContexts().registerContext(int.class, intResolver);
		plugin.getCommandManager().getCommandContexts().registerContext(Integer.class, intResolver);

		plugin.getCommandManager().getCommandContexts().registerContext(UUID.class, context -> {
			String firstArg = context.popFirstArg();
			try {
				return UUID.fromString(firstArg);
			} catch (IllegalArgumentException e) {
				throw new InvalidCommandArgument("UUID required"); // TODO lang
			}
			// TODO allow fetching by player after
		});

		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(BukkitCommandIssuer.class,
				CommandExecutionContext::getIssuer);

		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(Player.class, new IssuerAwareContextResolver<>() {

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

		plugin.getCommandManager().getCommandContexts().registerContext(ChatColor.class, new ContextResolver<>() {
			@Override
			public ChatColor getContext(BukkitCommandExecutionContext context1) throws InvalidCommandArgument {
				ChatColor matched = Colors.getOrDefault(context1.popFirstArg(), null);
				if (matched == null) {
					invalid(context1);
				}
				if (matched == ChatColor.RESET || !context1.hasFlag("colour") && !context1.hasFlag("format")) {
					// Reset is a special case - used to clear colour settings
					return matched;
				}
				boolean format = matched == ChatColor.BOLD || matched == ChatColor.UNDERLINE
						|| matched == ChatColor.STRIKETHROUGH || matched == ChatColor.MAGIC;
				if (context1.hasFlag("colour") && format || context1.hasFlag("format") && !format) {
					invalid(context1);
				}
				return matched;
			}

			private void invalid(BukkitCommandExecutionContext context1) {
				throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}",
						Arrays.stream(org.bukkit.ChatColor.values()).filter(chatColor -> context1.hasFlag("format")
								? chatColor.isFormat() : !context1.hasFlag("colour") || chatColor.isColor())
								.map(Enum::name).collect(Collectors.joining(", ", "[", "]")));
			}
		});

		// TODO lang for invalid args
		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(World.class, context -> {
			String worldName = context.getFirstArg();
			if (worldName == null) {
				if (context.isOptional() && context.getIssuer().isPlayer()) {
					return context.getIssuer().getPlayer().getWorld();
				}
				throw new InvalidCommandArgument("No world specified!");
			}

			World world = plugin.getServer().getWorld(worldName.toLowerCase());
			if (world == null) {
				if (context.isOptional() && context.getIssuer().isPlayer()) {
					return context.getIssuer().getPlayer().getWorld();
				}
				throw new InvalidCommandArgument("No world specified!");
			}
			context.popFirstArg();
			return world;
		});
	}

	private CoreContexts() {}

}
