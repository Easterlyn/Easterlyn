package co.sblock.utilities.rxbukkit;

import java.util.concurrent.TimeUnit;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.internal.schedulers.ScheduledAction;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

/**
 * Custom Scheduler for use in rxjava-bukkit.
 * 
 * @author rmichela
 * @author Twister915
 * @author Jikoo
 */
@Data
@EqualsAndHashCode(callSuper = false)
public final class RxBukkitScheduler extends Scheduler {

	public enum ConcurrencyMode {
		SYNC,
		ASYNC
	}

	private final JavaPlugin plugin;
	private final ConcurrencyMode concurrencyMode;

	public RxBukkitScheduler(JavaPlugin plugin, ConcurrencyMode concurrencyMode) {
		this.plugin = plugin;
		this.concurrencyMode = concurrencyMode;
	}

	private BukkitTask actualSchedule(final Action0 action, int ticksDelay) {
		BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				action.call();
			}
		};
		if (concurrencyMode == ConcurrencyMode.ASYNC) {
			return runnable.runTaskLaterAsynchronously(plugin, ticksDelay);
		}
		return runnable.runTaskLater(plugin, ticksDelay);
	}

	@Override
	public Worker createWorker() {
		return new BukkitWorker();
	}

	private final class BukkitWorker extends Worker {
		private final CompositeSubscription allSubscriptions = new CompositeSubscription();

		@Override
		public Subscription schedule(Action0 action) {
			return schedule(action, 0, TimeUnit.MILLISECONDS);
		}

		@Override
		public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
			final BukkitTask bukkitTask = actualSchedule(action,
					(int) Math.round(unit.toMillis(delayTime) / 50D));
			ScheduledAction scheduledAction = new ScheduledAction(action, allSubscriptions);
			scheduledAction.add(Subscriptions.create(new Action0() {
				@Override
				public void call() {
					bukkitTask.cancel();
				}
			}));
			return scheduledAction;
		}

		@Override
		public void unsubscribe() {
			this.allSubscriptions.unsubscribe();
		}

		@Override
		public boolean isUnsubscribed() {
			return this.allSubscriptions.isUnsubscribed();
		}
	}

}
