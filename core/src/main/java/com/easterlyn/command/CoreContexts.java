package com.easterlyn.command;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.contexts.ContextResolver;
import com.easterlyn.EasterlynCore;
import com.easterlyn.user.PlayerUser;
import com.easterlyn.user.User;
import com.easterlyn.util.Colors;
import com.easterlyn.util.NumberUtil;
import com.easterlyn.util.wrapper.PlayerFuture;
import com.easterlyn.util.wrapper.PlayerUserFuture;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class CoreContexts {

  public static final String SELF = "self";
  public static final String OFFLINE = "offline";
  public static final String ONLINE = "online";
  public static final String ONLINE_WITH_PERM = "otherWithPerm";

  private static final Pattern INTEGER_PATTERN =
      Pattern.compile("(-?\\d+)[dl]?", Pattern.CASE_INSENSITIVE);

  private CoreContexts() {}

  public static void register(EasterlynCore plugin) {
    ContextResolver<Long, BukkitCommandExecutionContext> longResolver =
        context -> {
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

    ContextResolver<Integer, BukkitCommandExecutionContext> intResolver =
        context -> Math.toIntExact(longResolver.getContext(context));

    plugin.getCommandManager().getCommandContexts().registerContext(int.class, intResolver);
    plugin.getCommandManager().getCommandContexts().registerContext(Integer.class, intResolver);

    plugin
        .getCommandManager()
        .getCommandContexts()
        .registerContext(
            UUID.class,
            context -> {
              String firstArg = context.popFirstArg();
              if (firstArg == null) {
                throw new InvalidCommandArgument("UUID required"); // TODO lang
              }
              try {
                return UUID.fromString(firstArg);
              } catch (IllegalArgumentException e) {
                throw new InvalidCommandArgument("UUID required");
              }
              // TODO allow fetching by player after
            });

    plugin
        .getCommandManager()
        .getCommandContexts()
        .registerIssuerAwareContext(BukkitCommandIssuer.class, CommandExecutionContext::getIssuer);

    plugin
        .getCommandManager()
        .getCommandContexts()
        .registerIssuerAwareContext(
            Player.class,
            new PlayerContextResolver());

    plugin
        .getCommandManager()
        .getCommandContexts()
        .registerIssuerAwareContext(
            ConsoleCommandSender.class,
            context -> {
              if (!(context.getIssuer().getIssuer() instanceof ConsoleCommandSender)) {
                throw new InvalidCommandArgument(CoreLang.ONLY_CONSOLE);
              }
              return (ConsoleCommandSender) context.getIssuer().getIssuer();
            });

    plugin
        .getCommandManager()
        .getCommandContexts()
        .registerIssuerAwareContext(
            User.class,
            context -> {
              Player player =
                  (Player)
                      plugin
                          .getCommandManager()
                          .getCommandContexts()
                          .getResolver(Player.class)
                          .getContext(context);
              User loaded = plugin.getUserManager().getLoadedPlayer(player.getUniqueId());
              if (loaded == null) {
                throw new InvalidCommandArgument(CoreLang.INVALID_PLAYER, "{value}", player.getName());
              }
              return loaded;
            });

    plugin
        .getCommandManager()
        .getCommandContexts()
        .registerIssuerAwareContext(
            PlayerUser.class,
            context -> {
              User user =
                  (User)
                      plugin
                          .getCommandManager()
                          .getCommandContexts()
                          .getResolver(User.class)
                          .getContext(context);
              if (user instanceof PlayerUser playerUser) {
                return playerUser;
              }
              throw new InvalidCommandArgument(CoreLang.INVALID_PLAYER, "{value}", user.getDisplayName());
            });

    plugin
        .getCommandManager()
        .getCommandContexts()
        .registerIssuerAwareContext(
            PlayerFuture.class,
            new PlayerFutureContextResolver(plugin));

    plugin
        .getCommandManager()
        .getCommandContexts()
        .registerIssuerAwareContext(
            PlayerUserFuture.class,
            context -> {
              PlayerFuture playerFuture =
                  (PlayerFuture)
                      plugin
                          .getCommandManager()
                          .getCommandContexts()
                          .getResolver(PlayerFuture.class)
                          .getContext(context);
              return new PlayerUserFuture(
                  playerFuture.id(),
                  playerFuture.future()
                      .thenCompose(optionalPlayer ->
                          optionalPlayer
                              .map(player -> plugin.getUserManager().getPlayer(player.getUniqueId()))
                              .orElseGet(() ->
                                  CompletableFuture.completedFuture(Optional.empty()))));
            });

    plugin
        .getCommandManager()
        .getCommandContexts()
        .registerContext(
            Date.class,
            context -> {
              String firstArg = context.popFirstArg();
              long duration = NumberUtil.parseDuration(firstArg);
              return new Date(Math.addExact(System.currentTimeMillis(), duration));
            });

    plugin
        .getCommandManager()
        .getCommandContexts()
        .registerContext(
            ChatColor.class,
            new ContextResolver<>() {
              @Override
              public ChatColor getContext(BukkitCommandExecutionContext context1)
                  throws InvalidCommandArgument {
                ChatColor matched = Colors.getOrDefault(context1.popFirstArg(), null);
                if (matched == null) {
                  invalid(context1);
                }
                if (matched == ChatColor.RESET
                    || !context1.hasFlag("colour") && !context1.hasFlag("format")) {
                  // Reset is a special case - used to clear colour settings
                  return matched;
                }
                boolean format =
                    matched == ChatColor.BOLD
                        || matched == ChatColor.UNDERLINE
                        || matched == ChatColor.STRIKETHROUGH
                        || matched == ChatColor.MAGIC;
                if (context1.hasFlag("colour") && format || context1.hasFlag("format") && !format) {
                  invalid(context1);
                }
                return matched;
              }

              private void invalid(BukkitCommandExecutionContext context1) {
                throw new InvalidCommandArgument(
                    MessageKeys.PLEASE_SPECIFY_ONE_OF,
                    "{valid}",
                    Arrays.stream(org.bukkit.ChatColor.values())
                        .filter(
                            chatColor ->
                                context1.hasFlag("format")
                                    ? chatColor.isFormat()
                                    : !context1.hasFlag("colour") || chatColor.isColor())
                        .map(Enum::name)
                        .collect(Collectors.joining(", ", "[", "]")));
              }
            });

    // TODO lang for invalid args
    plugin
        .getCommandManager()
        .getCommandContexts()
        .registerIssuerAwareContext(
            World.class,
            context -> {
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

}
