package com.easterlyn.event;

import com.easterlyn.user.User;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * A user-based event abstraction.
 *
 * @author Jikoo
 */
public abstract class UserEvent extends Event {

  private @NotNull final User user;

  public UserEvent(@NotNull User user) {
    super(!Bukkit.isPrimaryThread());
    this.user = user;
  }

  public @NotNull User getUser() {
    return user;
  }
}
