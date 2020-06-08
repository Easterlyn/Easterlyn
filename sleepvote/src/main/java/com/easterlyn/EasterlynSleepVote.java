package com.easterlyn;

import com.easterlyn.util.event.SimpleListener;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class EasterlynSleepVote extends JavaPlugin {

	private static final int NIGHT_START = 12541;
	private static final int NIGHT_END = 23458;
	private static final int NIGHT_DURATION = NIGHT_END - NIGHT_START + 2;

	private final Map<String, BukkitTask> worldTasks = new HashMap<>();

	@Override
	public void onEnable() {
		PlayerJoinEvent.getHandlerList().register(new SimpleListener<>(PlayerJoinEvent.class,
				event -> getServer().getScheduler().runTask(this,
						() -> updateBar(event.getPlayer().getWorld())), this));

		PlayerQuitEvent.getHandlerList().register(new SimpleListener<>(PlayerQuitEvent.class,
				event -> getServer().getScheduler().runTask(this,
						() -> updateBar(event.getPlayer().getWorld())), this));

		PlayerBedLeaveEvent.getHandlerList().register(new SimpleListener<>(PlayerBedLeaveEvent.class,
				event -> getServer().getScheduler().runTask(this,
						() -> updateBar(event.getPlayer().getWorld())), this));

		PlayerBedEnterEvent.getHandlerList().register(new SimpleListener<>(PlayerBedEnterEvent.class,
				event -> getServer().getScheduler().runTaskLater(this,
						() -> updateBar(event.getPlayer().getWorld()), 510L), this));

		PlayerChangedWorldEvent.getHandlerList().register(new SimpleListener<>(PlayerChangedWorldEvent.class,
				event -> getServer().getScheduler().runTask(this, () -> {
					updateBar(event.getFrom());
					updateBar(event.getPlayer().getWorld());
				}), this));
	}

	private void updateBar(World world) {
		if (world.getEnvironment() != World.Environment.NORMAL) {
			return;
		}

		BossBar bossBar = Bukkit.getBossBar(new NamespacedKey(this, world.getName()));
		if (bossBar == null) {
			bossBar = Bukkit.createBossBar(new NamespacedKey(this, world.getName()),
					"Sleeping Percentage", BarColor.BLUE, BarStyle.SOLID);
		}

		long worldTime = world.getTime();
		boolean day = worldTime > NIGHT_END || worldTime < NIGHT_START;

		double sleeping = 0;
		double total = 0;
		for (Player player : world.getPlayers()) {
			if (player.isSleepingIgnored()) {
				continue;
			}
			++total;
			if (player.isSleeping()) {
				++sleeping;
			}
		}

		// Technically 0/0 is 100%, but for the purpose of clearing when no players are sleeping, it is not.
		double percentage =  sleeping == 0 ? 0 : total == 0 ? 1 : sleeping / total;

		if (percentage <= 0) {
			BukkitTask bukkitTask = worldTasks.remove(world.getName());
			if (bukkitTask != null && !bukkitTask.isCancelled()) {
				bukkitTask.cancel();
			}
			bossBar.removeAll();
			bossBar.setProgress(0);
			return;
		}

		// Day sleep to reset rain: 50% required, no time change
		percentage = day ? Math.min(1, percentage * 2) : percentage;

		if (bossBar.getProgress() == percentage) {
			// No state change.
			return;
		}

		bossBar.setProgress(percentage);
		world.getPlayers().forEach(bossBar::addPlayer);

		if (day) {
			if (percentage >= 1) {
				world.setWeatherDuration(0);

				BukkitTask bukkitTask = worldTasks.remove(world.getName());
				if (bukkitTask != null && !bukkitTask.isCancelled()) {
					bukkitTask.cancel();
				}

				bossBar.removeAll();
				bossBar.setProgress(0);
			} else {
				// Fire update at nightfall to change bar state.
				worldTasks.computeIfAbsent(world.getName(), worldName -> new BukkitRunnable() {
					@Override
					public void run() {
						worldTasks.remove(worldName);
						updateBar(world);
					}
				}.runTaskLater(this, (worldTime < NIGHT_START ? NIGHT_START - worldTime : worldTime - NIGHT_DURATION) + 2));
			}
			return;
		}

		BukkitTask bukkitTask = worldTasks.remove(world.getName());
		if (bukkitTask != null && !bukkitTask.isCancelled()) {
			bukkitTask.cancel();
		}

		double additionalTicksPerTick = NIGHT_DURATION / (NIGHT_DURATION * percentage) - 1;

		worldTasks.put(world.getName(), new BukkitRunnable() {
			private double fractionalTicks = 0;

			@Override
			public void run() {
				fractionalTicks += additionalTicksPerTick;
				if (fractionalTicks < 1) {
					return;
				}

				int modTicks = (int) fractionalTicks;
				fractionalTicks -= modTicks;

				world.setTime(Math.min(world.getTime() + modTicks, NIGHT_END));
			}
		}.runTaskTimer(this, 0, 1));
	}

}
