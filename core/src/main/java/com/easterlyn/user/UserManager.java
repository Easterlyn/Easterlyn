package com.easterlyn.user;

import com.easterlyn.EasterlynCore;
import com.easterlyn.event.PlayerNameChangeEvent;
import com.easterlyn.event.UserCreationEvent;
import com.easterlyn.event.UserLoadEvent;
import com.easterlyn.event.UserUnloadEvent;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.text.TextParsing;
import com.easterlyn.util.wrapper.ConcurrentConfiguration;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.jikoo.planarwrappers.event.Event;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manager for loading users.
 *
 * @author Jikoo
 */
public class UserManager {

  private final EasterlynCore plugin;
  private final Cache<UUID, PlayerUser> userCache;

  public UserManager(@NotNull EasterlynCore plugin) {
    this.plugin = plugin;
    this.userCache =
        Caffeine.newBuilder()
            .expireAfterAccess(30L, TimeUnit.MINUTES)
            .maximumSize(plugin.getServer().getMaxPlayers() * 2L)
            .removalListener(
                (UUID key, PlayerUser value, RemovalCause cause) -> {
                  if (key == null || value == null) {
                    return;
                  }
                  if (cause == RemovalCause.EXPIRED && plugin.getServer().getPlayer(key) != null) {
                    // Player is online. Schedule immediate re-addition and don't release perm data.
                    plugin.getServer().getScheduler()
                        .runTaskAsynchronously(plugin, () -> setPlayerUser(key, value));
                    return;
                  }
                  plugin.getServer().getPluginManager().callEvent(new UserUnloadEvent(value));
                  PermissionUtil.releasePermissionData(key);
                })
            .build();

    Event.register(
        AsyncPlayerPreLoginEvent.class,
        event -> {
          if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            getOrLoadNow(event.getUniqueId());
          }
        },
        plugin,
        EventPriority.MONITOR);

    Event.register(
        PlayerQuitEvent.class,
        event ->
            plugin
                .getServer()
                .getScheduler()
                .runTaskAsynchronously(
                    plugin,
                    () -> {
                      User user = userCache.getIfPresent(event.getPlayer().getUniqueId());
                      if (user != null) {
                        // Keep permissions loaded if userdata is still loaded
                        PermissionUtil.loadPermissionData(event.getPlayer().getUniqueId());
                      }
                    }),
        plugin);

    TextParsing.addQuoteConsumer(new PlayerUserQuoteConsumer(userCache.asMap()::values));
  }

  private void setPlayerUser(@NotNull UUID uuid, @NotNull PlayerUser user) {
    this.userCache.put(uuid, user);
  }

  // TODO getUser(CommandSender)?

  public @Nullable PlayerUser getLoadedPlayer(@NotNull UUID uuid) {
    return userCache.getIfPresent(uuid);
  }

  public @NotNull CompletableFuture<Optional<PlayerUser>> getPlayer(@NotNull UUID uuid) {
    PlayerUser present = userCache.getIfPresent(uuid);
    if (present != null) {
      return CompletableFuture.completedFuture(Optional.of(present));
    }
    CompletableFuture<Optional<PlayerUser>> future = new CompletableFuture<>();
    try {
      new BukkitRunnable() {
        @Override
        public void run() {
          future.complete(Optional.of(getOrLoadNow(uuid)));
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
          super.cancel();
          future.complete(Optional.empty());
        }
      }.runTaskAsynchronously(plugin);
    } catch (Exception ignored) {
      // Plugin disabled or server shutting down.
      future.complete(Optional.empty());
    }
    return future;
  }

  public @NotNull PlayerUser getOrLoadNow(@NotNull UUID uuid) {
    PlayerUser present = userCache.getIfPresent(uuid);
    if (present != null) {
      return present;
    }

    PlayerUser user = loadPlayerUser(plugin, uuid);
    userCache.put(uuid, user);
    plugin.getServer().getPluginManager().callEvent(new UserLoadEvent(user));
    return user;
  }

  private @NotNull PlayerUser loadPlayerUser(@NotNull EasterlynCore plugin, @NotNull final UUID uuid) {
    PluginManager pluginManager = plugin.getServer().getPluginManager();
    File file = Path.of(plugin.getDataFolder().getPath(), "users", uuid + ".yml").toFile();
    ConcurrentConfiguration storage = ConcurrentConfiguration.load(plugin, file);
    if (file.exists()) {
      PlayerUser user = new PlayerUser(plugin, uuid, storage);
      Player player = user.getPlayer();

      if (player != null && player.getAddress() != null) {
        storage.set("ip", player.getAddress().getHostString());
        String previousName = storage.getString("name");
        if (previousName != null && !previousName.equals(player.getName())) {
          storage.set("previousName", previousName);
          storage.set("name", player.getName());
          pluginManager.callEvent(
              new PlayerNameChangeEvent(player, previousName, player.getName()));
        }
      }

      return user;
    }

    Player player = Bukkit.getPlayer(uuid);

    PlayerUser user = new PlayerUser(plugin, uuid, ConcurrentConfiguration.load(plugin, file));
    if (player != null) {
      user.getStorage().set("name", player.getName());
      if (player.getAddress() != null) {
        user.getStorage().set("ip", player.getAddress().getHostString());
      }

      pluginManager.callEvent(new UserCreationEvent(user));
    }

    return user;
  }

  public void clearCache() {
    userCache.invalidateAll();
    userCache.cleanUp();
  }

}
