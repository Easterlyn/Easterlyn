package com.easterlyn.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when a player logs in after having changed their name.
 *
 * @author Jikoo
 */
public class PlayerNameChangeEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();
  private @NotNull final String from;
  private @NotNull final String to;
  private @NotNull final Player player;

  public PlayerNameChangeEvent(@NotNull Player player, @NotNull String from, @NotNull String to) {
    this.player = player;
    this.from = from;
    this.to = to;
  }

  public @NotNull static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  public @NotNull Player getPlayer() {
    return player;
  }

  public @NotNull String getFrom() {
    return from;
  }

  public @NotNull String getTo() {
    return to;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLER_LIST;
  }
}
