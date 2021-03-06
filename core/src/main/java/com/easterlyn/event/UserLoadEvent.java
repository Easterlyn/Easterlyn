package com.easterlyn.event;

import com.easterlyn.user.User;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when a user is loaded.
 *
 * @author Jikoo
 */
public class UserLoadEvent extends UserEvent {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  public UserLoadEvent(@NotNull User user) {
    super(user);
  }

  public @NotNull static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLER_LIST;
  }
}
