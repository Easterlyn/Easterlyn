package com.easterlyn.event;

import com.easterlyn.users.User;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * A user-based event abstraction.
 *
 * @author Jikoo
 */
public abstract class UserEvent extends Event {

	@NotNull
	private User user;

	public UserEvent(@NotNull User user) {
		super(!Bukkit.isPrimaryThread());
		this.user = user;
	}

	@NotNull
	public User getUser() {
		return user;
	}

}
