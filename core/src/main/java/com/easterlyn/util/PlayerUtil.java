package com.easterlyn.util;

import com.easterlyn.event.ReportableEvent;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.wrapper.PermissiblePlayer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility for accessing Players.
 *
 * @author Jikoo
 */
public class PlayerUtil {

  private static final Cache<UUID, Player> PLAYER_CACHE =
      CacheBuilder.newBuilder()
          .expireAfterAccess(5, TimeUnit.MINUTES)
          .maximumSize(50)
          .removalListener(
              notification -> {
                if (notification.getValue() == null) {
                  return;
                }
                Player player = (Player) notification.getValue();
                // Save if over 45 days since last login, removes achievements that should not be
                // present.
                if (player.getLastPlayed() < System.currentTimeMillis() - 3888000000L) {
                  player.saveData();
                }
              })
          .build();

  static {
    PermissionUtil.addParent("easterlyn.command.selector", UserRank.STAFF.getPermission());
  }

  private PlayerUtil() {}

  /**
   * Removes a cached Player if present.
   *
   * @param uuid the UUID of the Player.
   */
  public static void removeFromCache(@NotNull UUID uuid) {
    PLAYER_CACHE.invalidate(uuid);
  }

  /**
   * Start fetching a Player for the specified UUID if they have previously logged in,
   * even if offline.
   *
   * @param plugin the Plugin instance used to schedule tasks
   * @param uuid the UUID
   * @return the Future
   */
  public static @NotNull CompletableFuture<Optional<Player>> getPlayer(
      @NotNull Plugin plugin,
      @NotNull UUID uuid) {
    return getPlayer(plugin, uuid, true);
  }

  /**
   * Start fetching a Player for the specified UUID if they have previously logged in,
   * even if offline.
   *
   * @param plugin the Plugin instance used to schedule tasks
   * @param uuid the UUID
   * @param useCached true if the Player cache is preferred over loading a new Player
   * @return the Future
   */
  public static @NotNull CompletableFuture<Optional<Player>> getPlayer(
      @NotNull Plugin plugin,
      @NotNull UUID uuid,
      boolean useCached) {
    Player loadedPlayer = getLoadedPlayer(uuid, useCached);
    if (loadedPlayer != null) {
      return CompletableFuture.completedFuture(Optional.of(loadedPlayer));
    }

    if (plugin.getServer().isPrimaryThread()) {
      return CompletableFuture.completedFuture(Optional.ofNullable(loadOfflinePlayer(uuid)));
    }

    // Return to main thread. Note that Bukkit's method only returns a Future, which is Sad(TM).
    CompletableFuture<Optional<Player>> future = new CompletableFuture<>();
    try {
      new BukkitRunnable() {
        @Override
        public void run() {
            future.complete(Optional.ofNullable(loadOfflinePlayer(uuid)));
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
          super.cancel();
          future.complete(Optional.empty());
        }
      }.runTask(plugin);
    } catch (IllegalStateException ignored) {
      // Server is shutting down or plugin is disabled.
      future.complete(Optional.empty());
    }

    return future;
  }

  public static @Nullable Player getLoadedPlayer(@NotNull UUID uuid, boolean useCached) {
    Player online = Bukkit.getPlayer(uuid);
    if (online != null) {
      // Online, life is easy.
      return online;
    }

    if (useCached) {
      return PLAYER_CACHE.getIfPresent(uuid);
    } else {
      // Invalidate cached player in case new player loaded is modified
      PLAYER_CACHE.invalidate(uuid);
    }

    return null;
  }

  public static @Nullable Player loadOfflinePlayer(@NotNull UUID uuid) {
    if (!Bukkit.isPrimaryThread()) {
      ReportableEvent.call("Loaded player off main thread", 5);
      throw new IllegalStateException("Cannot load players off main thread!");
    }

    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
    if (offlinePlayer.getName() == null) {
      // Player has not logged in.
      return null;
    }

    // Create a profile and entity to load the player data
    // See net.minecraft.server.PlayerList#attemptLogin
    GameProfile profile = new GameProfile(offlinePlayer.getUniqueId(),
        offlinePlayer.getName() != null
            ? offlinePlayer.getName()
            : offlinePlayer.getUniqueId().toString());
    MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
    ServerLevel serverLevel = server.getLevel(Level.OVERWORLD);

    if (serverLevel == null) {
      return null;
    }

    ServerPlayer nmsPlayer =
        new ServerPlayer(
            server,
            serverLevel,
            profile,
            null);
    // TODO: swap to OpenInv to prevent overwriting mounts?

    CraftPlayer player = nmsPlayer.getBukkitEntity();
    if (player == null) {
      return null;
    }
    if (offlinePlayer.hasPlayedBefore()) {
      player.loadData();
    }
    // Wrap player so permissions checks will work
    player = new PermissiblePlayer(player);
    PLAYER_CACHE.put(uuid, player);
    return player;
  }

  public static void cachePlayer(@NotNull Player player) {
    PLAYER_CACHE.put(player.getUniqueId(), player);
  }

  /**
   * Matches an online Player for a given sender Player.
   *
   * @param sender the sender, or null to ignore visibility
   * @param id the identifier used to match a Player
   * @return the Player, or null if no matches were found
   */
  public static @Nullable Player matchOnlinePlayer(
      @Nullable CommandSender sender,
      @NotNull String id) {

    Player senderPlayer = sender instanceof Player ? (Player) sender : null;

    try {
      UUID uuid = UUID.fromString(id);
      Player player = Bukkit.getPlayer(uuid);
      if (player == null || senderPlayer == null || senderPlayer.canSee(player)) {
        return player;
      }
      return null;
    } catch (IllegalArgumentException ignored) {
      // Not a UUID.
    }

    if (sender != null
        && sender.hasPermission("easterlyn.command.selector")
        && id.length() > 1
        && id.charAt(0) == '@') {
      // Theoretically selection accepts a UUID or player name, but why chance it on changes?
      for (Entity entity : Bukkit.selectEntities(sender, id)) {
        if (entity instanceof Player
            && (senderPlayer == null || senderPlayer.canSee((Player) entity))) {
          return (Player) entity;
        }
      }
      return null;
    }

    for (Player player : Bukkit.getOnlinePlayers()) {
      if (senderPlayer != null && !senderPlayer.canSee(player)) {
        continue;
      }

      if (StringUtil.startsWithIgnoreCase(player.getName(), id)
          || StringUtil.startsWithIgnoreCase(player.getDisplayName(), id)) {
        return player;
      }
    }

    return null;
  }

  /**
   * Match a Player, online or off.
   *
   * @param id the UUID or name of the Player
   * @param offline true if offline Players should be matched
   * @param plugin the Plugin instance
   * @return the Player, or null if no matches were found
   */
  public static @NotNull CompletableFuture<Optional<Player>> matchPlayer(
      @NotNull Plugin plugin,
      @Nullable CommandSender sender,
      @NotNull String id,
      boolean offline) {
    // TODO: nick support

    // UUIDs have distinct handling.
    try {
      UUID uuid = UUID.fromString(id);

      if (!offline) {
        return CompletableFuture.completedFuture(Optional.ofNullable(Bukkit.getPlayer(uuid)));
      }

      return getPlayer(plugin, uuid, true);
    } catch (IllegalArgumentException e) {
      // Not a UUID.
    }

    // Target selectors have distinct handling.
    if (sender != null
        && sender.hasPermission("easterlyn.command.selector")
        && id.length() > 1
        && id.charAt(0) == '@') {
        Player senderPlayer = sender instanceof Player ? (Player) sender : null;
      for (Entity entity : Bukkit.selectEntities(sender, id)) {
        if (entity instanceof Player player
            && (senderPlayer == null || senderPlayer.canSee(player))) {
          return CompletableFuture.completedFuture(Optional.of(player));
        }
      }
      return CompletableFuture.completedFuture(Optional.empty());
    }

    // Exact online match.
    Player playerExact = Bukkit.getPlayerExact(id);
    if (playerExact != null) {
      return CompletableFuture.completedFuture(Optional.of(playerExact));
    }

    if (!offline) {
      // Inexact online match.
      return CompletableFuture.completedFuture(Optional.ofNullable(matchOnlinePlayer(sender, id)));
    }

    return matchForwardingOfflineExact(plugin, id);
  }

  private static @NotNull CompletableFuture<Optional<Player>> matchForwardingOfflineExact(
      @NotNull Plugin plugin,
      @NotNull String id) {
    CompletableFuture<Optional<Player>> future = new CompletableFuture<>();
    try {
      new BukkitRunnable() {
        @Override
        public void run() {
          GameProfileCache cache = ((CraftServer) plugin.getServer()).getServer().getProfileCache();
          // Fetch from cache. Must be done async, will request if not present.
          Optional<GameProfile> profileOptional = cache.get(id);

          // Invalid profile name.
          if (profileOptional.isEmpty()) {
            // Pass forward to online inexact.
            matchForwardingInexact(plugin, id).thenAccept(future::complete);
            return;
          }

          UUID uuid = profileOptional.get().getId();
          OfflinePlayer cachedOffline = Bukkit.getOfflinePlayer(uuid);

          // Require user to have actually logged in.
          if (!cachedOffline.hasPlayedBefore()) {
            // Pass forward to online inexact.
            matchForwardingInexact(plugin, id).thenAccept(future::complete);
            return;
          }

          // Fetch player, returning to main thread if needed.
          getPlayer(plugin, uuid).thenAccept(future::complete);
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
          super.cancel();
          future.complete(Optional.empty());
        }
      }.runTaskAsynchronously(plugin);
    } catch (IllegalStateException ignored) {
      // Server is shutting down or plugin is disabled.
      future.complete(Optional.empty());
    }

    return future;
  }

  private static CompletableFuture<Optional<Player>> matchForwardingInexact(
      @NotNull Plugin plugin,
      @NotNull String id) {
    // This is a fallthrough that will also provide offline players, so player visibility
    // should not factor in to player existence. Prefer online inexact for speed.
    Player onlineInexact = matchOnlinePlayer(null, id);
    if (onlineInexact != null) {
      return CompletableFuture.completedFuture(Optional.of(onlineInexact));
    }

    CompletableFuture<Optional<Player>> future = new CompletableFuture<>();

    try {
      new BukkitRunnable() {
        @Override
        public void run() {
          String lowerId = id.toLowerCase();
          OfflinePlayer bestPlayer = null;
          float bestMatch = 0F;
          for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            String offlineName = offlinePlayer.getName();
            if (offlineName == null) {
              // Loaded by UUID only, name has never been looked up.
              continue;
            }

            float currentMatch = StringUtil.compare(lowerId, offlinePlayer.getName().toLowerCase());

            if (currentMatch == 1F) {
              bestPlayer = offlinePlayer;
              break;
            }

            if (currentMatch > bestMatch) {
              bestMatch = currentMatch;
              bestPlayer = offlinePlayer;
            }
          }

          // Best match is null if no players have ever logged in.
          if (bestPlayer == null) {
            future.complete(Optional.empty());
          } else {
            // Fetch player, returning to main thread if needed.
            getPlayer(plugin, bestPlayer.getUniqueId()).thenAccept(future::complete);
          }
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
          super.cancel();
          future.complete(Optional.empty());
        }
      }.runTaskAsynchronously(plugin);
    } catch (IllegalStateException ignored) {
      // Server is shutting down or plugin is disabled.
      future.complete(Optional.empty());
    }

    return future;
  }

}
