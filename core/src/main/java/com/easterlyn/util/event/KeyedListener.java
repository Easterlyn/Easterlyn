package com.easterlyn.util.event;

import java.util.function.Consumer;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

public class KeyedListener<T extends Event> extends RegisteredListener {

	private final String key;

	KeyedListener(@NotNull Class<T> eventClass, @NotNull Consumer<T> consumer, @NotNull Plugin plugin,
			@NotNull String key, @NotNull EventPriority priority, boolean ignoreCancelled) {
		super(new Listener() {}, new ConsumerEventExecutor<>(eventClass, consumer), priority, plugin, ignoreCancelled);
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}

}
