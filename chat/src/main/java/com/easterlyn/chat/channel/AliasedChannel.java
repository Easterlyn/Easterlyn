package com.easterlyn.chat.channel;

import com.easterlyn.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Set;
import java.util.UUID;

public class AliasedChannel extends ServerChannel {

  private final ServerChannel actual;

  public AliasedChannel(@NotNull ServerChannel actual, @NotNull String name) {
    super(name);
    this.actual = actual;
  }

  @Override
  public boolean isFocusedChannel(@Nullable String channelName) {
    return super.isFocusedChannel(channelName) || actual.isFocusedChannel(channelName);
  }

  @Override
  public boolean isWhitelisted(@NotNull User user) {
    return false;
  }

  @Override
  public @NotNull Set<UUID> getMembers() {
    return actual.getMembers();
  }

  public @NotNull ServerChannel getActual() {
    return this.actual;
  }

}
