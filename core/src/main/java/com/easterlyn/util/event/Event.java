package com.easterlyn.util.event;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

/**
 * A utility for converting a Consumer into a Listener.
 *
 * @author Jikoo
 */
public class Event {

	public static <T extends org.bukkit.event.Event> void register(@NotNull Class<T> eventClass, @NotNull Consumer<T> consumer, @NotNull Plugin plugin) {
		register(eventClass, consumer, plugin, EventPriority.NORMAL);
	}

	public static <T extends org.bukkit.event.Event> void register(@NotNull Class<T> eventClass, @NotNull Consumer<T> consumer, @NotNull Plugin plugin,
			@NotNull EventPriority priority) {
		register(eventClass, consumer, plugin, priority, true);
	}

	public static <T extends org.bukkit.event.Event> void register(@NotNull Class<T> eventClass, @NotNull Consumer<T> consumer,
			@NotNull Plugin plugin, @NotNull EventPriority priority, boolean ignoreCancelled) {
		HandlerList handlerList;
		try {
			Method getHandlers = eventClass.getMethod("getHandlerList");
			Object handlerListObj = getHandlers.invoke(null);
			handlerList = (HandlerList) handlerListObj;
		} catch (Exception e) {
			// Re-throw exception, shouldn't occur unless we do something dumb.
			throw new RuntimeException(e);
		}
		handlerList.register(new RegisteredListener(new Listener() {},
				new ConsumerEventExecutor<>(eventClass, consumer), priority, plugin, ignoreCancelled));
	}

	public static <T extends org.bukkit.event.Event> void register(@NotNull Class<T> eventClass, @NotNull Consumer<T> consumer,
			@NotNull Plugin plugin, @NotNull String key) {
		register(eventClass, consumer, plugin, key, EventPriority.NORMAL);
	}

	public static <T extends org.bukkit.event.Event> void register(@NotNull Class<T> eventClass, @NotNull Consumer<T> consumer,
			@NotNull Plugin plugin, @NotNull String key, @NotNull EventPriority priority) {
		register(eventClass, consumer, plugin, key, priority, true);
	}

	public static <T extends org.bukkit.event.Event> void register(@NotNull Class<T> eventClass, @NotNull Consumer<T> consumer,
			@NotNull Plugin plugin, @NotNull String key, @NotNull EventPriority priority, boolean ignoreCancelled) {
		HandlerList handlerList;
		try {
			Method getHandlers = eventClass.getMethod("getHandlers");
			Object handlerListObj = getHandlers.invoke(null);
			handlerList = (HandlerList) handlerListObj;
		} catch (Exception e) {
			// Re-throw exception, shouldn't occur unless we do something dumb.
			throw new RuntimeException(e);
		}

		for (RegisteredListener registeredListener : handlerList.getRegisteredListeners()) {
			if (registeredListener instanceof KeyedListener && ((KeyedListener<?>) registeredListener).getKey().equals(key)) {
				return;
			}
		}

		handlerList.register(new KeyedListener<>(eventClass, consumer, plugin, key, priority, ignoreCancelled));
	}

	private Event() {}

}
