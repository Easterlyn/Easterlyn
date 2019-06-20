package com.easterlyn.event;

import com.easterlyn.users.User;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when a new user is created.
 *
 * @author Jikoo
 */
public class UserCreationEvent extends UserEvent {

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public UserCreationEvent(@NotNull User user) {
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
