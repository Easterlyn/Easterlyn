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

	@NotNull
	private Player player;
	@NotNull
	private final String from;
	@NotNull
	private final String to;

	public PlayerNameChangeEvent(@NotNull Player player, @NotNull String from, @NotNull String to) {
		this.player = player;
		this.from = from;
		this.to = to;
	}

	@NotNull
	public Player getPlayer() {
		return player;
	}

	@NotNull
	public String getFrom() {
		return from;
	}

	@NotNull
	public String getTo() {
		return to;
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	@NotNull
	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}

}
