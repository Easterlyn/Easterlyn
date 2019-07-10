package com.easterlyn.event;

import com.easterlyn.user.User;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when a user is unloaded.
 *
 * @author Jikoo
 */
public class UserUnloadEvent extends UserEvent {

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public UserUnloadEvent(@NotNull User user) {
		super(user);
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
