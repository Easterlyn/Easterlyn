package co.sblock.utilities.rxbukkit;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * EventStreamer for rxjava-bukkit.
 * 
 * @author rmichela
 * @author Twister915
 * @author Jikoo
 */
public final class EventStreamer extends BaseStreamer {

	public EventStreamer(Plugin plugin, RxBukkitScheduler syncScheduler,
			RxBukkitScheduler asyncScheduler) {
		super(plugin, syncScheduler, asyncScheduler);
	}

	@SafeVarargs
	public final <T extends Event> Observable<T> observeEvent(Class<? extends T>... events) {
		return observeEvent(EventPriority.NORMAL, events);
	}

	@SafeVarargs
	public final <T extends Event> Observable<T> observeEvent(EventPriority priority,
			Class<? extends T>... events) {
		return observeEvent(priority, false, events);
	}

	@SafeVarargs
	public final <T extends Event> Observable<T> observeEvent(final EventPriority priority,
			final boolean ignoreCancelled, final Class<? extends T>... events) {
		// Creates an observer which...
		return Observable.create(new Observable.OnSubscribe<T>() {
			@Override
			public void call(final Subscriber<? super T> subscriber) {
				// Creates an empty listener
				final Listener listener = new Listener() {};
				// Creates an event executor
				@SuppressWarnings("unchecked")
				EventExecutor executor = new EventExecutor() {
					@Override
					public void execute(Listener listener1, Event event) throws EventException {
						// Check to make sure the event bukkit sent us can apply to what the
						// observer is expecting (type T). Strangely, they'll send things like the
						// EntityDamageEvent when we want to cast to EntityDamageByEntity so it's
						// best if we manually check here to make sure it can apply to any class.
						boolean canAssign = false;

						// get the class of the event
						Class<? extends Event> eventClass = event.getClass();
						// go through all classes we're concerned with emitting (T is their mutual supertype).
						for (Class<? extends T> aClass : events) {
							if (aClass.isAssignableFrom(eventClass)) {
								// If one of the classes (from the events classes arg) is assignable to the
								// event class (is a superclass or equal to) then we're good, and can break.
								canAssign = true;
								break;
							}
						}

						// if we never discovered a class which is assignable, this is one of those
						// weird and rare cases where Bukkit is stupid
						if (!canAssign) {
							return;
						}

						try {
							// noinspection unchecked
							subscriber.onNext((T) event);
						} catch (Throwable t) {
							Exceptions.throwOrReport(t, subscriber);
						}
					}
				};

				// Registers all the event types to that listener
				PluginManager pluginManager = Bukkit.getPluginManager();
				for (Class<? extends T> event : events)
					pluginManager.registerEvent(event, listener, priority, executor, plugin,
							ignoreCancelled);

				// Registers a HandlerList.unregisterAll call as the unsubscribe action
				subscriber.add(Subscriptions.create(new Action0() {
					@Override
					public void call() {
						HandlerList.unregisterAll(listener);
					}
				}));

				// Needs to unsubscribe when the plugin disables
				pluginManager.registerEvent(PluginDisableEvent.class, listener,
						EventPriority.MONITOR, new EventExecutor() {
							@Override
							public void execute(Listener l, Event event) throws EventException {
								PluginDisableEvent disableEvent = (PluginDisableEvent) event;
								if (disableEvent.getPlugin().equals(plugin))
									subscriber.onCompleted();

							}
						}, plugin, false);
			}
		}).compose(this.<T> getSyncTransformer());
	}
}
