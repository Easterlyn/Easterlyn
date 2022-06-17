package com.easterlyn.chat.channel;

import org.jetbrains.annotations.NotNull;
import java.util.Set;
import java.util.UUID;

public class AliasedChannel extends ServerChannel {

  private final ServerChannel actual;

  public AliasedChannel(ServerChannel actual, String name) {
    super(name);
    this.actual = actual;
  }

  @Override
  public @NotNull Set<UUID> getMembers() {
    return actual.getMembers();
  }

}
