package com.easterlyn;

import com.easterlyn.discord.ChannelType;
import com.easterlyn.discord.DiscordUser;
import com.easterlyn.discord.MinecraftBridge;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.util.wrapper.ConcurrentConfiguration;
import com.github.jikoo.planarwrappers.event.Event;
import com.github.jikoo.planarwrappers.tuple.Pair;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

public class EasterlynDiscord extends JavaPlugin {

  private final Map<ChannelType, Pair<StringBuffer, Long>> messageQueue = new ConcurrentHashMap<>();
  private ConcurrentConfiguration datastore;
  private LoadingCache<Object, Object> pendingAuthentications;
  private GatewayDiscordClient client;

  private static String generateCode() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 6; i++) {
      String chars = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
      sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
    }
    return sb.toString();
  }

  @Override
  public void onEnable() {
    saveDefaultConfig();
    datastore = ConcurrentConfiguration.load(this, new File(getDataFolder(), "datastore.yml"));
    pendingAuthentications =
        CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(
                new CacheLoader<>() {
                  @Override
                  public Object load(@NotNull Object key) {
                    if (!(key instanceof UUID)) {
                      throw new IllegalArgumentException("Key must be a UUID");
                    }
                    String code;
                    do {
                      code = generateCode();
                    } while (pendingAuthentications.getIfPresent(code) != null);
                    pendingAuthentications.put(code, key);
                    return code;
                  }
                });

    connect();

    Event.register(
        ReportableEvent.class,
        event -> {
          String message = event.getMessage();
          if (event.hasTrace()) {
            message += '\n' + event.getTrace();
          }
          postMessage(ChannelType.REPORT, message);
        },
        this);

    RegisteredServiceProvider<EasterlynCore> registration =
        getServer().getServicesManager().getRegistration(EasterlynCore.class);
    if (registration != null) {
      register(registration.getProvider());
    }

    Event.register(
        PluginEnableEvent.class,
        event -> {
          if (event.getPlugin() instanceof EasterlynCore) {
            register((EasterlynCore) event.getPlugin());
          }
        },
        this);
  }

  private void connect() {
    String token = getConfig().getString("token");
    if (token == null || token.isEmpty()) {
      getLogger().warning("No token provided! Nothing to do.");
      return;
    }

    getServer()
        .getScheduler()
        .runTaskAsynchronously(
            this,
            () -> {
              DiscordClient client = DiscordClientBuilder.create(token).build();
              client
                  .login()
                  .doOnSuccess(
                      gatewayClient -> {
                        this.client = gatewayClient;
                        new MinecraftBridge(this, gatewayClient).setup();
                        gatewayClient
                            .updatePresence(Presence.online(Activity.playing("play.easterlyn.com")))
                            .subscribe();
                      })
                  .subscribe();
            });
  }

  private void register(EasterlynCore plugin) {
    plugin.registerCommands(this, getClassLoader(), "com.easterlyn.discord.command");
    plugin.getLocaleManager().addLocaleSupplier(this);
  }

  public String getCommandPrefix() {
    return "/";
  }

  public boolean isChannelType(Snowflake channelID, ChannelType type) {
    ConfigurationSection guildSection = this.getConfig().getConfigurationSection("guilds");
    if (guildSection == null) {
      return false;
    }

    String channelIdString = channelID.asString();
    for (String guildIDString : guildSection.getKeys(false)) {
      if (channelIdString.equals(
          guildSection.getString(guildIDString + ".channels." + type.getPath()))) {
        return true;
      }
    }

    return false;
  }

  public Collection<GuildMessageChannel> getChannelIDs(ChannelType type) {
    if (Bukkit.isPrimaryThread()) {
      throw new IllegalStateException("Don't demand information from Discord on the main thread.");
    }
    Collection<GuildMessageChannel> collection =
        Collections.newSetFromMap(new ConcurrentHashMap<>());
    if (!this.isEnabled() || client == null) {
      return collection;
    }
    ConfigurationSection guildSection = this.getConfig().getConfigurationSection("guilds");
    if (guildSection == null) {
      return collection;
    }

    Phaser phaser = new Phaser(1);
    for (String guildIDString : guildSection.getKeys(false)) {
      // Parse guild ID
      Snowflake guildID;
      try {
        guildID = Snowflake.of(guildIDString);
      } catch (NumberFormatException e) {
        continue;
      }

      String channelIdString =
          guildSection.getString(guildIDString + ".channels." + type.getPath());
      if (channelIdString == null) {
        continue;
      }
      Snowflake channelID;
      try {
        channelID = Snowflake.of(channelIdString);
      } catch (NumberFormatException e) {
        continue;
      }

      phaser.register();
      client
          .getGuildById(guildID)
          .flatMap(
              guild ->
                  guild
                      .getChannelById(channelID)
                      .cast(GuildMessageChannel.class)
                      .doOnSuccess(collection::add))
          .doOnError(Throwable::printStackTrace)
          .doOnTerminate(phaser::arriveAndDeregister)
          .subscribe();
    }

    phaser.arriveAndAwaitAdvance();
    return collection;
  }

  public @Nullable DiscordUser getUser(@NotNull Snowflake id) throws IllegalStateException {
    String uuidString = datastore.getString("link." + id.asString());
    if (uuidString == null) {
      return null;
    }
    return getUser(UUID.fromString(uuidString));
  }

  public @NotNull DiscordUser getUser(@NotNull UUID uuid) throws IllegalStateException {
    RegisteredServiceProvider<EasterlynCore> registration =
        getServer().getServicesManager().getRegistration(EasterlynCore.class);
    if (registration == null) {
      throw new IllegalStateException("EasterlynCore not enabled!");
    }
    return new DiscordUser(registration.getProvider().getUserManager().getUser(uuid));
  }

  public @NotNull String getPendingLink(@NotNull UUID uuid) {
    return (String) pendingAuthentications.getUnchecked(uuid);
  }

  public @Nullable UUID getPendingLink(@NotNull String code) {
    return (UUID) pendingAuthentications.getIfPresent(code);
  }

  public @NotNull Mono<Snowflake> getFirstMemberSnowflake(@NotNull String string) {
    try {
      return Mono.justOrEmpty(Snowflake.of(string));
    } catch (NumberFormatException ignored) {
      // Not a snowflake, fall through to name matching.
    }

    String id = string.toLowerCase();

    return client
        .getGuilds()
        .flatMap(Guild::getMembers)
        .skipUntil(
            member ->
                member.getDisplayName().toLowerCase().startsWith(id)
                    || member.getTag().toLowerCase().startsWith(id))
        .next()
        .map(Member::getId);
  }

  public void addLink(@NotNull UUID uuid, @NotNull Snowflake snowflake) {
    Object linkCode = pendingAuthentications.getIfPresent(uuid);
    if (linkCode != null) {
      pendingAuthentications.invalidate(uuid);
      pendingAuthentications.invalidate(linkCode);
    }

    datastore.set("link." + snowflake.asString(), uuid.toString());
  }

  public void postMessage(ChannelType channelType, String message) {
    if (client == null) {
      // TODO handle client not connected
      return;
    }

    if (getServer().isPrimaryThread()) {
      getServer()
          .getScheduler()
          .runTaskAsynchronously(this, () -> postMessage(channelType, message));
      return;
    }

    if (channelType == ChannelType.MAIN) {
      postMessage(ChannelType.LOG, message);
    }

    if (channelType.getAggregateTime() > 0) {
      Pair<StringBuffer, Long> aggregateData = messageQueue.get(channelType);
      if (aggregateData == null) {
        aggregateData = new Pair<>(new StringBuffer(), 0L);
        messageQueue.put(channelType, aggregateData);
      }

      // Max message length is 2000. Cap aggregation to 1900 to be safe.
      if (aggregateData.getLeft().length() + message.length() + 1 > 1900) {
        directPostMessage(channelType, aggregateData.getLeft().toString());
        aggregateData.getLeft().delete(0, aggregateData.getLeft().length());
      }

      if (aggregateData.getLeft().length() > 0) {
        aggregateData.getLeft().append('\n');
      }
      aggregateData.getLeft().append(message);

      if (aggregateData.getRight() <= System.currentTimeMillis()) {
        // Typing status while aggregating
        getChannelIDs(channelType)
            .forEach(
                channel ->
                    channel
                        .typeUntil(Mono.delay(Duration.ofMillis(channelType.getAggregateTime())))
                        .subscribe());

        aggregateData.setRight(System.currentTimeMillis() + channelType.getAggregateTime());
        Pair<StringBuffer, Long> data = aggregateData;
        getServer()
            .getScheduler()
            .runTaskLaterAsynchronously(
                this,
                () -> {
                  directPostMessage(channelType, data.getLeft().toString());
                  data.getLeft().delete(0, data.getLeft().length());
                },
                channelType.getAggregateTime() / 20);
      }

      return;
    }

    directPostMessage(channelType, message);
  }

  private void directPostMessage(ChannelType channelType, String message) {
    if (client == null) {
      // TODO handle client not connected
      return;
    }

    while (message.length() > 1900) {
      String search = message.substring(0, 1900);
      int index = search.lastIndexOf('\n');
      if (index > -1) {
        directPostMessage(channelType, message.substring(0, index));
        // Ignore newline.
        message = message.substring(index + 1);
        continue;
      }
      directPostMessage(channelType, message.substring(0, 1900));
      message = message.substring(1900);
    }

    String finalMessage = message.trim();
    if (finalMessage.isEmpty()) {
      return;
    }

    getChannelIDs(channelType).forEach(channel -> channel.createMessage(finalMessage).subscribe());
  }
}
