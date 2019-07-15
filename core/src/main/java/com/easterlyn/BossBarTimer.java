package com.easterlyn;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BossBarTimer {

	// Minimum noticeable boss bar increment on vanilla client - bar is 180px.
	private static final double MINIMUM_BOSS_BAR_INCREMENT = 1D / 180;

	@NotNull
	private final String title;
	@NotNull
	private final Runnable onComplete;
	@NotNull
	private final Collection<Player> players;
	@Nullable
	private Supplier<Boolean> shouldContinue = null;
	@Nullable
	private Runnable onFailure = null;
	@NotNull
	private BarColor barColor = BarColor.GREEN;
	@NotNull
	private BarStyle barStyle = BarStyle.SOLID;
	private BarFlag[] barFlags = new BarFlag[0];
	@NotNull
	private DisplayType displayType = DisplayType.FILL;

	public BossBarTimer(@NotNull String title, @NotNull Runnable onComplete, @NotNull Player player, @NotNull Player... players) {
		this.title = title;
		this.onComplete = onComplete;
		this.players = new HashSet<>();
		this.players.add(player);
		this.players.addAll(Arrays.asList(players));
	}

	public BossBarTimer withFailureFunction(@NotNull Supplier<Boolean> continueCheck, @NotNull Runnable onFailure) {
		this.shouldContinue = continueCheck;
		this.onFailure = onFailure;
		return this;
	}

	public BossBarTimer withBarColor(BarColor barColor) {
		this.barColor = barColor;
		return this;
	}

	public BossBarTimer withBarStyle(BarStyle barStyle) {
		this.barStyle = barStyle;
		return this;
	}

	public BossBarTimer withDisplayType(DisplayType displayType) {
		this.displayType = displayType;
		return this;
	}

	public BossBarTimer withBarFlags(@NotNull BarFlag flag, @NotNull BarFlag... flags) {
		barFlags = EnumSet.of(flag, flags).toArray(new BarFlag[0]);
		return this;
	}

	public void schedule(Plugin plugin, String barKey, long duration, TimeUnit durationUnit) {
		long tickDuration = TimeUnit.SECONDS.convert(duration, durationUnit) * 20;
		if (tickDuration < 1) {
			onComplete.run();
			return;
		}

		NamespacedKey key = new NamespacedKey(plugin, barKey);
		KeyedBossBar existingBossBar = plugin.getServer().getBossBar(key);
		if (existingBossBar != null) {
			// Run already in progress, reset.
			existingBossBar.setProgress(displayType.getStart());
			return;
		}
		KeyedBossBar bossBar = plugin.getServer().createBossBar(key, title, barColor, barStyle, barFlags);
		players.forEach(bossBar::addPlayer);

		int period = (int) Math.ceil(tickDuration * MINIMUM_BOSS_BAR_INCREMENT);
		if (period < 2) {
			period = 2;
		} else if (period > 100 && shouldContinue != null) {
			period = 100;
		}

		final double percentPerPeriod = period / (double) tickDuration;

		new BukkitRunnable() {
			@Override
			public void run() {
				bossBar.setProgress(displayType.increment(bossBar.getProgress(), percentPerPeriod));

				if (displayType.isFinished(bossBar.getProgress())) {
					cancel();
				} else if (shouldContinue != null && !shouldContinue.get()) {
					cancel();
				}
			}

			@Override
			public synchronized void cancel() throws IllegalStateException {
				if (displayType.isFinished(bossBar.getProgress())) {
					if (onFailure != null) {
						onFailure.run();
					}
				} else {
					onComplete.run();
				}

				plugin.getServer().removeBossBar(bossBar.getKey());

				super.cancel();
			}
		}.runTaskTimer(plugin, period, period);
	}

	public static Supplier<Boolean> supplierPlayerImmobile(Player player) {
		World world = player.getWorld();
		BoundingBox boundingBox = BoundingBox.of(player.getLocation(), 0.5, 0.5, 0.5);
		return () -> player.getWorld().equals(world) && boundingBox.contains(player.getLocation().toVector());
	}

	public enum DisplayType {

		FILL(0, (value1, value2) -> Math.min(1, value1 + value2), value -> value >= 1),
		DRAIN(1, (value1, value2) -> Math.max(0, value1 - value2), value -> value <= 0);

		private final double start;
		private final DoubleBinaryOperator incrementOperation;
		private final Function<Double, Boolean> finishedFunction;

		DisplayType(double start, DoubleBinaryOperator incrementOperation, Function<Double, Boolean> finishedFunction) {
			this.start = start;
			this.incrementOperation = incrementOperation;
			this.finishedFunction = finishedFunction;
		}

		double getStart() {
			return start;
		}

		double increment(double currentProgress, double increment) {
			return incrementOperation.applyAsDouble(currentProgress, increment);
		}

		boolean isFinished(double value) {
			return finishedFunction.apply(value);
		}

	}

}
