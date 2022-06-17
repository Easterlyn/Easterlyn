package com.easterlyn.chat.channel;

import com.easterlyn.user.User;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public class ServerChannel extends BaseChannel {

  public ServerChannel(String name) {
    super(name);
  }

  @Override
  public boolean isPrivate() {
    return false;
  }

  @Override
  public void setPrivate(boolean isPrivate) {}

  @Override
  public @Nullable String getPassword() {
    return null;
  }

  @Override
  public void setPassword(@Nullable String password) {}

  @Override
  public @Nullable UUID getOwner() {
    return null;
  }

  @Override
  public boolean isModerator(@NotNull User user) {
    return isOwner(user) || user.hasPermission("easterlyn.chat.channel.moderator");
  }

  @Override
  public void setModerator(@NotNull User user, boolean moderator) {}

  @Override
  public boolean isWhitelisted(@NotNull User user) {
    return isModerator(user);
  }

  @Override
  public void setWhitelisted(@NotNull User user, boolean whitelisted) {}

  @Override
  public boolean isBanned(@NotNull User user) {
    return !isModerator(user);
  }

  @Override
  public void setBanned(@NotNull User user, boolean banned) {}

  @Override
  public boolean isRecentlyAccessed() {
    return false;
  }

  @Override
  public void updateLastAccess() {}

  @Override
  public void load(@NotNull ConfigurationSection data) {}

}
