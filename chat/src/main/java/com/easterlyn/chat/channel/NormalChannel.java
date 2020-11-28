package com.easterlyn.chat.channel;

import com.easterlyn.user.User;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class representing a standard chat channel.
 *
 * @author Jikoo
 */
public class NormalChannel extends Channel {

  private final Set<UUID> moderators;
  private final Set<UUID> whitelist;
  private final Set<UUID> bans;
  private final AtomicLong lastAccessed;
  private final AtomicBoolean isPrivate;
  private @Nullable String password;

  /**
   * Construct a new NormalChannel.
   *
   * @param name the name of the NormalChannel
   * @param owner the owner of the NormalChannel
   */
  public NormalChannel(@NotNull String name, @NotNull UUID owner) {
    super(name, owner);
    isPrivate = new AtomicBoolean(false);
    moderators = Collections.newSetFromMap(new ConcurrentHashMap<>());
    whitelist = Collections.newSetFromMap(new ConcurrentHashMap<>());
    bans = Collections.newSetFromMap(new ConcurrentHashMap<>());
    lastAccessed = new AtomicLong(System.currentTimeMillis());
    password = null;
  }

  @Override
  public boolean isPrivate() {
    return isPrivate.get();
  }

  @Override
  public void setPrivate(boolean isPrivate) {
    this.isPrivate.set(isPrivate);
    if (!isPrivate) {
      setPassword(null);
    }
  }

  @Override
  public @Nullable String getPassword() {
    return password;
  }

  @Override
  public void setPassword(@Nullable String password) {
    if (isPrivate.get()) {
      this.password = password;
    }
  }

  @Override
  public boolean isModerator(@NotNull User user) {
    return super.isModerator(user) || moderators.contains(user.getUniqueId());
  }

  @Override
  public void setModerator(@NotNull User user, boolean moderator) {
    if (moderator) {
      moderators.add(user.getUniqueId());
      whitelist.add(user.getUniqueId());
    } else {
      moderators.remove(user.getUniqueId());
    }
  }

  @Override
  public boolean isWhitelisted(@NotNull User user) {
    return !isBanned(user)
        && (!isPrivate() || isModerator(user) || whitelist.contains(user.getUniqueId()));
  }

  @Override
  public void setWhitelisted(@NotNull User user, boolean whitelisted) {
    if (whitelisted) {
      bans.remove(user.getUniqueId());
      whitelist.add(user.getUniqueId());
    } else {
      whitelist.remove(user.getUniqueId());
    }
  }

  @Override
  public boolean isBanned(@NotNull User user) {
    return !isModerator(user) && bans.contains(user.getUniqueId());
  }

  @Override
  public void setBanned(@NotNull User user, boolean banned) {
    if (isModerator(user)) {
      return;
    }
    bans.add(user.getUniqueId());
    whitelist.remove(user.getUniqueId());
    getMembers().remove(user.getUniqueId());
  }

  @Override
  public boolean isRecentlyAccessed() {
    // 1000 ms/s * 60 s/min * 60 min/hr * 24 hr/d * 30d
    return lastAccessed.get() + 2592000000L > System.currentTimeMillis();
  }

  @Override
  public void updateLastAccess() {
    lastAccessed.set(System.currentTimeMillis());
  }

  @Override
  public void save(@NotNull Configuration channelStorage) {
    super.save(channelStorage);
    channelStorage.set(getName() + ".private", isPrivate.get());
    channelStorage.set(getName() + ".password", password);
    channelStorage.set(
        getName() + ".moderators",
        moderators.stream().map(UUID::toString).collect(Collectors.toList()));
    channelStorage.set(
        getName() + ".whitelist",
        whitelist.stream().map(UUID::toString).collect(Collectors.toList()));
    channelStorage.set(
        getName() + ".bans", bans.stream().map(UUID::toString).collect(Collectors.toList()));
    channelStorage.set(getName() + ".lastAccess", lastAccessed.get());
  }

  @Override
  public void load(@NotNull ConfigurationSection data) {
    super.load(data);
    isPrivate.set(data.getBoolean("private"));
    password = data.getString("password");
    data.getStringList("moderators")
        .forEach(
            uuidString -> {
              try {
                moderators.add(UUID.fromString(uuidString));
              } catch (IllegalArgumentException expected) {
                // Ignore invalid UUIDs.
              }
            });
    data.getStringList("whitelist")
        .forEach(
            uuidString -> {
              try {
                whitelist.add(UUID.fromString(uuidString));
              } catch (IllegalArgumentException ignored) {
                // Ignore invalid UUIDs.
              }
            });
    data.getStringList("bans")
        .forEach(
            uuidString -> {
              try {
                bans.add(UUID.fromString(uuidString));
              } catch (IllegalArgumentException ignored) {
                // Ignore invalid UUIDs.
              }
            });
    lastAccessed.set(data.getLong("lastAccess", System.currentTimeMillis()));
  }
}
