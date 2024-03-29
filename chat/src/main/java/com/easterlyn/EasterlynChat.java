package com.easterlyn;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import co.aikar.locales.MessageKey;
import com.easterlyn.chat.channel.AliasedChannel;
import com.easterlyn.chat.channel.BaseChannel;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.InternalChannel;
import com.easterlyn.chat.channel.MainChannel;
import com.easterlyn.chat.channel.MessageChannel;
import com.easterlyn.chat.channel.ServerChannel;
import com.easterlyn.chat.channel.UserChannel;
import com.easterlyn.chat.command.ChannelFlag;
import com.easterlyn.chat.listener.ChannelManagementListener;
import com.easterlyn.chat.listener.MuteListener;
import com.easterlyn.command.CoreLang;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.plugin.EasterlynPlugin;
import com.easterlyn.user.PlayerUser;
import com.easterlyn.user.User;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.Colors;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.text.ParsedText;
import com.easterlyn.util.text.StaticQuoteConsumer;
import com.easterlyn.util.text.TextParsing;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Plugin for managing Easterlyn's chat system.
 *
 * @author Jikoo
 */
public class EasterlynChat extends EasterlynPlugin {

  public static final ServerChannel DEFAULT = new MainChannel();
  public static final String USER_CHANNELS = "chat.channels";
  public static final String USER_CURRENT = "chat.current";
  public static final String USER_HIGHLIGHTS = "chat.highlights";
  public static final String USER_MUTE = "chat.mute";
  private static final Pattern CHANNEL_PATTERN =
      Pattern.compile("^(#(?:[A-Za-z0-9]{0,15}|#))([\\W&&[^" + ChatColor.COLOR_CHAR + "}#]])?$");
  private final Map<String, Channel> channels = new ConcurrentHashMap<>();

  @Override
  protected void enable() {
    // Permission to use >greentext.
    PermissionUtil.getOrCreate("easterlyn.chat.greentext", PermissionDefault.TRUE);
    // Permission to bypass all chat filtering.
    PermissionUtil.addParent("easterlyn.chat.spam", UserRank.MODERATOR.getPermission());
    PermissionUtil.addParent("easterlyn.chat.spam", "easterlyn.spam");
    // Permission to use all caps.
    PermissionUtil.getOrCreate("easterlyn.chat.spam.caps", PermissionDefault.TRUE);
    PermissionUtil.addParent("easterlyn.chat.spam.caps", "easterlyn.chat.spam");
    // Permission to use non-ascii characters and mixed case words.
    PermissionUtil.getOrCreate("easterlyn.chat.spam.normalize", PermissionDefault.TRUE);
    PermissionUtil.addParent("easterlyn.chat.spam.normalize", "easterlyn.chat.spam");
    // Permission to not be affected by speed limitations.
    PermissionUtil.getOrCreate("easterlyn.chat.spam.fast", PermissionDefault.TRUE);
    PermissionUtil.addParent("easterlyn.chat.spam.fast", "easterlyn.chat.spam");
    // Permission for gibberish filtering, average characters per word.
    PermissionUtil.addParent("easterlyn.chat.spam.gibberish", "easterlyn.chat.spam");
    // Permission to send duplicate messages in a row within 30 seconds.
    // Default false - quite handy to prevent accidental uparrow enter.
    PermissionUtil.getOrCreate("easterlyn.chat.spam.repeat", PermissionDefault.FALSE);
    // Permission for messages to automatically color using name color.
    PermissionUtil.getOrCreate("easterlyn.chat.color", PermissionDefault.FALSE);
    // Permission to be recognized as a moderator in every channel.
    PermissionUtil.addParent("easterlyn.chat.channel.moderator", UserRank.STAFF.getPermission());
    // Permission to be recognized as an owner in every channel.
    PermissionUtil.addParent("easterlyn.chat.channel.owner", UserRank.ADMIN.getPermission());
    // Permission to make funky channel names
    PermissionUtil.addParent(
        "easterlyn.command.channel.create.anyname", UserRank.HEAD_ADMIN.getPermission());

    FileConfiguration config = getConfig();
    Set<String> remove = new HashSet<>();
    config
        .getKeys(false)
        .forEach(
            key -> {
              if (!loadChannel(key, config.getConfigurationSection(key))) {
                remove.add(key);
              }
            });

    remove.forEach(key -> config.set(key, null));

    channels.put("", DEFAULT);
    channels.put("main", DEFAULT);
    channels.put("aether", new AliasedChannel(DEFAULT, "aether"));
    channels.put("dm", new MessageChannel());
    channels.put("sign", new InternalChannel("sign"));
    channels.put("#", new InternalChannel("#"));

    getServer().getPluginManager().registerEvents(new ChannelManagementListener(this), this);
    getServer().getPluginManager().registerEvents(new MuteListener(this), this);

    // TODO
    //  - anti-spam listener
    //  - log signs to #sign
    //  - Add permission for translating & codes in chat
    //  - Allow overrides for channel hover

  }

  public Map<String, Channel> getChannels() {
    return channels;
  }

  @Override
  public void onDisable() {
    FileConfiguration config = getConfig();
    channels
        .values()
        .forEach(
            channel -> {
              if (channel.isRecentlyAccessed()) {
                channel.save(config);
              } else {
                config.set(channel.getName(), null);
              }
            });
    saveConfig();
  }

  private boolean loadChannel(@NotNull String name, @Nullable ConfigurationSection data) {
    if (data == null) {
      return false;
    }

    String className = data.getString("class");
    if (className == null) {
      return false;
    }

    Class<?> channelClass;
    try {
      channelClass = Class.forName(className);
    } catch (ClassNotFoundException e) {
      return false;
    }
    if (!BaseChannel.class.isAssignableFrom(channelClass)) {
      return false;
    }

    Constructor<?> channelConstructor;
    try {
      channelConstructor = channelClass.getConstructor(String.class, UUID.class);
    } catch (NoSuchMethodException e) {
      return false;
    }

    String uuidString = data.getString("owner");
    if (uuidString == null) {
      return false;
    }
    UUID owner;
    try {
      owner = UUID.fromString(uuidString);
    } catch (Exception e) {
      return false;
    }

    Channel channel;
    try {
      channel = (Channel) channelConstructor.newInstance(name, owner);
    } catch (ReflectiveOperationException e) {
      return false;
    }

    if (!channel.isRecentlyAccessed()) {
      return false;
    }

    channel.load(data);

    channels.put(name, channel);
    return true;
  }

  @Override
  protected void register(@NotNull EasterlynCore plugin) {
    TextParsing.addQuoteConsumer(
        new StaticQuoteConsumer(CHANNEL_PATTERN) {
          @Override
          public void addComponents(
              @NotNull ParsedText components, @NotNull Supplier<Matcher> matcherSupplier) {
            Matcher matcher = matcherSupplier.get();
            String channelName = matcher.group(1);
            Channel channel = getChannels().get(channelName.toLowerCase().substring(1));
            TextComponent component;
            if (channel != null) {
              component = channel.getMention();
            } else {
              int end = matcher.end(1);
              component = new TextComponent(matcher.group().substring(0, end));
              component.setColor(Colors.CHANNEL);
              component.setUnderlined(true);
              component.setHoverEvent(
                  new HoverEvent(
                      HoverEvent.Action.SHOW_TEXT,
                      new Text(
                          TextComponent.fromLegacyText(
                              Colors.COMMAND + "/join " + Colors.CHANNEL + channelName))));
              component.setClickEvent(
                  new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + channelName));
            }
            components.addComponent(component);

            String trailingPunctuation = matcher.group(2);
            if (trailingPunctuation != null && !trailingPunctuation.isEmpty()) {
              components.addText(
                  trailingPunctuation, component.getHoverEvent(), component.getClickEvent());
            }
          }
        });

    // TODO probably need ChannelFuture
    IssuerAwareContextResolver<Channel, BukkitCommandExecutionContext> channelContext =
        context -> {
          String firstArg = context.getFirstArg();

          // Current channel or unspecified, defaulting to current
          if (context.hasFlag(ChannelFlag.CURRENT)
              || (context.hasFlag(ChannelFlag.LISTENING_OR_CURRENT)
                      || context.hasFlag(ChannelFlag.VISIBLE_OR_CURRENT))
                  && (firstArg == null || firstArg.indexOf('#') != 0)) {
            if (!context.getIssuer().isPlayer()) {
              // Console never has a current channel
              throw new InvalidCommandArgument(CoreLang.NO_CONSOLE);
            }
            UUID uniqueId = context.getPlayer().getUniqueId();
            // Load player for next time if they aren't loaded.
            plugin.getUserManager().getPlayer(uniqueId);
            PlayerUser playerUser = plugin
                .getUserManager()
                .getLoadedPlayer(uniqueId);
            if (playerUser == null) {
              throw new InvalidCommandArgument(MessageKey.of("chat.common.no_current_channel"));
            }
            String channelName = playerUser.getStorage().getString(USER_CURRENT);
            Channel channel = channels.get(channelName);
            if (channel == null) {
              throw new InvalidCommandArgument(MessageKey.of("chat.common.no_current_channel"));
            }
            return channel;
          }

          // Channel name must be specified for anything beyond this point, pop argument
          context.popFirstArg();

          if (firstArg == null) {
            throw new InvalidCommandArgument(MessageKey.of("chat.common.no_specified_channel"));
          }

          if (firstArg.length() == 0 || firstArg.charAt(0) != '#') {
            throw new InvalidCommandArgument(
                MessageKey.of("chat.commands.channel.create.error.naming_conventions"));
          }

          Channel channel = channels.get(firstArg.substring(1).toLowerCase());

          if (channel == null) {
            throw new InvalidCommandArgument(
                MessageKey.of("chat.common.no_matching_channel"), "{value}", firstArg);
          }

          User user;
          if (context.getIssuer().isPlayer()) {
            user = getCore().getUserManager().getLoadedPlayer(context.getIssuer().getUniqueId());
            if (user == null) {
              throw new InvalidCommandArgument(MessageKey.of("chat.common.no_current_channel"));
            }
          } else {
            user = null;
          }

          if (context.hasFlag(ChannelFlag.VISIBLE)
              || context.hasFlag(ChannelFlag.VISIBLE_OR_CURRENT)) {
            if (user == null || !channel.isPrivate() || channel.isWhitelisted(user)) {
              return channel;
            }
            throw new InvalidCommandArgument(
                MessageKey.of("chat.common.no_channel_access"),
                "{value}",
                channel.getDisplayName());
          }

          if (context.hasFlag(ChannelFlag.LISTENING)
              || context.hasFlag(ChannelFlag.LISTENING_OR_CURRENT)) {
            if (user == null) {
              return channel;
            }
            List<String> channels = user.getStorage().getStringList(EasterlynChat.USER_CHANNELS);
            if (!channels.contains(channel.getName())) {
              throw new InvalidCommandArgument(
                  MessageKey.of("chat.common.not_listening_to_channel"),
                  "{value}",
                  channel.getDisplayName());
            }
            return channel;
          }

          if (user == null) {
            throw new InvalidCommandArgument(CoreLang.NO_CONSOLE);
          }

          if (context.hasFlag(ChannelFlag.NOT_LISTENING)) {
            List<String> channels = user.getStorage().getStringList(EasterlynChat.USER_CHANNELS);
            if (channels.contains(channel.getName())) {
              throw new InvalidCommandArgument(
                  MessageKey.of("chat.common.listening_to_channel"),
                  "{value}",
                  channel.getDisplayName());
            }
            return channel;
          }

          ReportableEvent.call("Missing Channel context flag!", 10);
          throw new InvalidCommandArgument(CoreLang.ERROR_LOGGED);
        };

    plugin
        .getCommandManager()
        .getCommandContexts()
        .registerIssuerAwareContext(Channel.class, channelContext);
    plugin
        .getCommandManager()
        .getCommandContexts()
        .registerIssuerAwareContext(
            UserChannel.class,
            context -> {
              Channel channel = channelContext.getContext(context);
              if (!(channel instanceof UserChannel)) {
                throw new InvalidCommandArgument(
                    MessageKey.of("chat.common.channel_not_modifiable"));
              }
              return (UserChannel) channel;
            });

    plugin
        .getCommandManager()
        .getCommandCompletions()
        .registerAsyncCompletion(
            "channels",
            getUserHandler(
                user ->
                    channels.values().stream()
                        .distinct()
                        .filter(channel -> channel.isWhitelisted(user))
                        .map(Channel::getDisplayName)
                        .collect(Collectors.toSet())));
    plugin
        .getCommandManager()
        .getCommandCompletions()
        .setDefaultCompletion("channels", Channel.class, UserChannel.class);

    plugin
        .getCommandManager()
        .getCommandCompletions()
        .registerAsyncCompletion(
            "channelsJoinable",
            getUserHandler(
                user -> {
                  List<String> channelsJoined =
                      user.getStorage().getStringList(EasterlynChat.USER_CHANNELS);
                  return Stream.concat(
                      channels.values().stream()
                          .filter(channel -> channel.isWhitelisted(user))
                          .map(Channel::getName),
                      channelsJoined.stream())
                      .map(name -> '#' + name)
                      .collect(Collectors.toSet());
                }));

    plugin
        .getCommandManager()
        .getCommandCompletions()
        .registerAsyncCompletion(
            "channelsListening",
            getUserHandler(
                user ->
                    user.getStorage().getStringList(EasterlynChat.USER_CHANNELS)
                        .stream()
                        .map(name -> '#' + name)
                        .collect(Collectors.toSet())));

    plugin
        .getCommandManager()
        .getCommandCompletions()
        .registerAsyncCompletion(
            "channelsModerated",
            getUserHandler(
                user ->
                    channels.values().stream()
                        .distinct()
                        .filter(channel -> channel.isModerator(user))
                        .map(Channel::getDisplayName)
                        .collect(Collectors.toSet())));

    plugin
        .getCommandManager()
        .getCommandCompletions()
        .registerAsyncCompletion(
            "channelsOwned",
            getUserHandler(
                user ->
                    channels.values().stream()
                        .distinct()
                        .filter(channel -> channel.isOwner(user))
                        .map(Channel::getDisplayName)
                        .collect(Collectors.toSet())));

    plugin.getLocaleManager().addLocaleSupplier(this);
    plugin.registerCommands(this, getClassLoader(), "com.easterlyn.chat.command");
  }
  @Contract(pure = true)
  private CommandCompletions.@NotNull AsyncCommandCompletionHandler<BukkitCommandCompletionContext>
      getUserHandler(Function<User, Collection<String>> userHandler) {
    return context -> {
      if (!context.getIssuer().isPlayer()) {
        return channels.keySet();
      }

      User user = null;
      try {
        user = getCore().getUserManager().getPlayer(context.getPlayer().getUniqueId())
            .get(1, TimeUnit.SECONDS).orElse(null);
      } catch (TimeoutException | InterruptedException | ExecutionException e) {
        ReportableEvent.call("Issue fetching user async", e, 5);
      }

      if (user == null) {
        return List.of();
      }

      return userHandler.apply(user);
    };
  }

}
