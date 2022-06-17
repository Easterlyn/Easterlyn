package com.easterlyn.discord;

import com.easterlyn.EasterlynChat;
import com.easterlyn.EasterlynCore;
import com.easterlyn.EasterlynDiscord;
import com.easterlyn.chat.channel.AliasedChannel;
import com.easterlyn.chat.event.UserChatEvent;
import com.github.jikoo.planarwrappers.event.Event;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public class MinecraftBridge {

  private final EasterlynDiscord plugin;
  private final GatewayDiscordClient client;
  private final Cache<Snowflake, Boolean> warnings;
  private final Pattern mention = Pattern.compile("<([@#]|:\\w+:)(\\d+)>");

  public MinecraftBridge(EasterlynDiscord plugin, GatewayDiscordClient client) {
    this.plugin = plugin;
    this.client = client;

    this.warnings =
        CacheBuilder.newBuilder()
            .weakKeys()
            .weakValues()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build();
  }

  public void setup() {

    client
        .getEventDispatcher()
        .on(MessageCreateEvent.class)
        .flatMap(
            event ->
                Mono.fromRunnable(
                        () -> {
                          if (event.getMessage().getAuthor().isEmpty()
                              || event.getMessage().getAuthor().get().isBot()) {
                            return;
                          }

                          User author = event.getMessage().getAuthor().get();
                          String msg = event.getMessage().getContent();
                          MessageChannel channel = event.getMessage().getChannel().block();

                          String command = null;
                          String commandData = null;
                          if (msg.startsWith(plugin.getCommandPrefix())) {
                            int prefixLength = plugin.getCommandPrefix().length();
                            int space = msg.indexOf(' ');
                            if (space < prefixLength) {
                              space = msg.length();
                            }
                            command = msg.substring(prefixLength, space).toLowerCase();
                            commandData = msg.substring(space < msg.length() ? space + 1 : space);
                          }

                          if (command != null && command.equals("link")) {
                            UUID pendingLink = plugin.getPendingLink(commandData);
                            if (pendingLink == null) {
                              channel.createMessage(
                                  "Invalid link code! If your code expired, rerun `/link` in"
                                      + " Minecraft.\n"
                                      + "If you're having trouble, try clicking the message to"
                                      + " copy the code!");
                            } else {
                              plugin.addLink(pendingLink, author.getId());
                            }
                          }

                          boolean main =
                              channel instanceof GuildMessageChannel
                                  && plugin.isChannelType(channel.getId(), ChannelType.MAIN);

                          if (!main && command == null) {
                            return;
                          }

                          DiscordUser sender = plugin.getUser(author.getId());
                          if (sender == null) {
                            Snowflake id = author.getId();
                            if (warnings.getIfPresent(id) != null) {
                              return;
                            }
                            warnings.put(id, true);
                            channel
                                .createMessage(
                                    author.getMention()
                                        + ", you must run `/link` in Minecraft to use this"
                                        + " feature!")
                                .subscribe();
                            return;
                          }

                          if (command != null) {
                            handleCommand(sender, channel, command, commandData);
                            return;
                          }

                          handleDiscordChat(sender, event.getMessage());
                        })
                    .onErrorResume(
                        error -> {
                          System.out.println("Caught exception handling Discord event:");
                          error.printStackTrace();
                          return Mono.empty();
                        })
                    .thenReturn(event))
        .subscribe();

    EasterlynChat chat = JavaPlugin.getPlugin(EasterlynChat.class);
    chat.getChannels().put("discord", new AliasedChannel(EasterlynChat.DEFAULT, "discord"));
    Event.register(
        UserChatEvent.class,
        event -> {
          // TODO may want to try to softdepend on chat instead of hard
          if (event.isAsynchronous()) {
            handleMinecraftChat(event);
          } else {
            // Ensure handling is done asynchronously even if chat is somehow sent on main thread
            plugin
                .getServer()
                .getScheduler()
                .runTaskAsynchronously(plugin, () -> handleMinecraftChat(event));
          }
        },
        plugin);

    Event.register(
        PlayerCommandPreprocessEvent.class,
        event -> {
          if (event.getPlayer().hasPermission("easterlyn.discord.exempt.commandlog")) {
            return;
          }
          RegisteredServiceProvider<EasterlynCore> registration =
              plugin.getServer().getServicesManager().getRegistration(EasterlynCore.class);
          if (registration == null) {
            return;
          }

          int space = event.getMessage().indexOf(' ');
          String commandName =
              event
                  .getMessage()
                  .substring(1, space > 0 ? space : event.getMessage().length())
                  .toLowerCase();
          SimpleCommandMap commandMap = registration.getProvider().getSimpleCommandMap();
          if (commandMap == null) {
            return;
          }
          Command command = commandMap.getCommand(commandName);
          if (command != null
              && !plugin
                  .getConfig()
                  .getStringList("command-log-blacklist")
                  .contains(command.getName())) {
            plugin.postMessage(
                ChannelType.LOG,
                event.getPlayer().getName() + " issued command: " + event.getMessage());
          }
        },
        plugin,
        EventPriority.MONITOR);

    Event.register(
        PlayerJoinEvent.class,
        event ->
            plugin.postMessage(
                ChannelType.MAIN,
                ChatColor.stripColor(event.getPlayer().getDisplayName()) + " logs in."),
        plugin,
        EventPriority.MONITOR);

    Event.register(
        PlayerQuitEvent.class,
        event ->
            plugin.postMessage(
                ChannelType.MAIN,
                ChatColor.stripColor(event.getPlayer().getDisplayName()) + " logs out."),
        plugin);
  }

  private void handleCommand(
      @NotNull DiscordUser user,
      @NotNull MessageChannel channel,
      @NotNull String command,
      @NotNull String commandData) {
    // TODO
  }

  private void handleDiscordChat(DiscordUser user, Message message) {
    String content = message.getContent();
    if (content.isBlank() && message.getAttachments().isEmpty() || message.getAuthor().isEmpty()) {
      return;
    }
    for (Attachment attachment : message.getAttachments()) {
      if (!content.isEmpty()) {
        content = content.concat(" ");
      }
      content = content.concat(attachment.getProxyUrl());
    }
    String finalContent = content;
    if (!user.hasPermission("easterlyn.discord.unfiltered")) {
      message
          .getChannel()
          .doOnSuccess(
              messageChannel -> {
                Mono<Message> replyMono;
                if (finalContent.indexOf('\n') > 0) {
                  replyMono =
                      messageChannel.createMessage(
                          "Newlines are not allowed in messages to Minecraft, "
                              + message.getAuthor().get().getMention());
                } else if (finalContent.length() > 255) {
                  replyMono =
                      messageChannel.createMessage(
                          "Messages from Discord may not be over 255 characters, "
                              + message.getAuthor().get().getMention());
                } else {
                  return;
                }
                replyMono
                    .doOnSuccess(
                        replyMessage ->
                            replyMessage.addReaction(ReactionEmoji.unicode(":no_entry_sign:")))
                    .subscribe();
              })
          .subscribe();
    }

    new UserChatEvent(user, EasterlynChat.DEFAULT, sanitizeForMinecraft(content)).send();
  }

  private String sanitizeForMinecraft(String message) {
    Matcher matcher = mention.matcher(message);
    StringBuilder sb = new StringBuilder();
    int lastMatch = 0;
    while (matcher.find()) {
      String type = matcher.group(1);
      sb.append(message, lastMatch, matcher.start()).append(type);
      Snowflake id = Snowflake.of(matcher.group(2));
      if ("@".equals(type)) {
        DiscordUser user = plugin.getUser(id);
        if (user != null) {
          sb.append('@').append(user.getDisplayName());
        } else {
          sb.append(id);
        }
      } else if ("#".equals(type)) {
        Channel channel = client.getChannelById(id).block();
        if (channel instanceof GuildChannel) {
          sb.append(((GuildChannel) channel).getName());
        } else {
          sb.append(id);
        }
      } else if (type.length() < 1 || type.charAt(0) != ':') {
        // Skip custom emoji, matcher group 1 is the full emoji name.
        sb.append(id);
      }
      lastMatch = matcher.end();
    }
    sb.append(message.substring(lastMatch));
    return sb.toString();
  }

  private void handleMinecraftChat(UserChatEvent event) {
    if (event.getUser() instanceof DiscordUser) {
      return;
    }

    StringBuilder builder = new StringBuilder();
    if (event.isThirdPerson()) {
      builder.append("* ");
    }
    builder.append('`').append(event.getUser().getDisplayName()).append("`");
    if (!event.isThirdPerson()) {
      builder.append(':');
    }
    builder.append(' ');
    builder.append(
        ChatColor.stripColor(event.getMessage())
            .replace("@everyone", "@Everyone")
            .replace("@here", "@Here"));

    String message = builder.toString();

    if (event.getChannel().getName().equals(EasterlynChat.DEFAULT.getName())) {
      plugin.postMessage(ChannelType.MAIN, message);
    } else {
      plugin.postMessage(ChannelType.LOG, message);
    }
  }
}
