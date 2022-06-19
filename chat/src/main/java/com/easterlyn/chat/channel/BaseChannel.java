package com.easterlyn.chat.channel;

import com.easterlyn.util.command.Group;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class representing a chat {@link Group}.
 *
 * @author Jikoo
 */
public abstract class BaseChannel implements Channel {

  private final String name;
  private final Set<UUID> members;
  private TextComponent mention;

  /**
   * Construct a new Channel with the given name and owner.
   *
   * @param name the name of the Channel
   */
  public BaseChannel(@NotNull String name) {
    this.name = name;
    this.members = Collections.newSetFromMap(new ConcurrentHashMap<>());
  }

  @Override
  public @NotNull final String getName() {
    return name;
  }

  @Override
  public boolean isFocusedChannel(@Nullable String channelName) {
    return this.name.equals(channelName);
  }

  @Override
  public @NotNull final TextComponent getMention() {
    if (mention == null) {
      mention = Channel.super.getMention();
    }
    return mention;
  }

  /**
   * Gets a set of all listening users' UUIDs.
   *
   * @return all relevant UUIDs
   */
  public @NotNull Set<UUID> getMembers() {
    return this.members;
  }

}
