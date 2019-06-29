package com.easterlyn.util.event;

import java.util.function.Consumer;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class KeyedListener<T extends Event> extends SimpleListener<T> {

	private final String key;

	public KeyedListener(@NotNull Class<T> eventClass, @NotNull Consumer<T> consumer, @NotNull Plugin plugin,
			@NotNull String key) {
		super(eventClass, consumer, plugin);
		this.key = key;
	}

	public KeyedListener(@NotNull Class<T> eventClass, @NotNull Consumer<T> consumer, @NotNull Plugin plugin,
			@NotNull String key, @NotNull EventPriority priority) {
		super(eventClass, consumer, plugin, priority);
		this.key = key;
	}

	public KeyedListener(@NotNull Class<T> eventClass, @NotNull Consumer<T> consumer, @NotNull Plugin plugin,
			@NotNull String key, @NotNull EventPriority priority, boolean ignoreCancelled) {
		super(eventClass, consumer, plugin, priority, ignoreCancelled);
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}

}
