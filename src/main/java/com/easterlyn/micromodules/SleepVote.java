package com.easterlyn.micromodules;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.module.Module;
import com.easterlyn.utilities.JSONUtil;
import net.md_5.bungee.api.ChatMessageType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility to allow players to stop storms and end night.
 *
 * @author Jikoo
 */
public class SleepVote extends Module {

	private final HashMap<String, Pair<BossBar, HashSet<String>>> votes = new HashMap<>();

	private Language lang;

	public SleepVote(Easterlyn plugin) {
		super(plugin);
	}

	@Override
	protected void onEnable() {
		this.lang = this.getPlugin().getModule(Language.class);
	}

	@Override
	protected void onDisable() {
		this.votes.clear();
	}

	public void addVote(World world, Player p) {
		if (!isEnabled()) {
			return;
		}
		if (!votes.containsKey(world.getName())) {
			votes.put(world.getName(), new ImmutablePair<>(
					Bukkit.createBossBar(lang.getValue("sleep.title"), BarColor.BLUE, BarStyle.SEGMENTED_20),
					new HashSet<>()));
			resetVote(world);
		}
		if (votes.get(world.getName()).getRight().add(p.getName())) {
			updateVotesLater(world, p.getDisplayName());
		}
	}

	/**
	 * Updates the percentage of players who have slept when a player is logging out or changing
	 * worlds.
	 *
	 * @param world the world to update
	 * @return true if the count has changed
	 */
	public boolean removeVote(final World world, Player player) {
		if (!isEnabled()) {
			return false;
		}

		if (!votes.containsKey(world.getName())) {
			return false;
		}

		boolean voted = false;
		if (player != null) {
			voted = votes.get(world.getName()).getRight().remove(player.getName());
		}

		updateVotesLater(world, null);
		return voted;
	}

	private void updateVotesLater(final World world, final String player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				updateVotes(world, player);
			}
		}.runTaskLater(this.getPlugin(), 30L);
	}

	private void updateVotes(World world, String player) {
		if (!isEnabled()) {
			return;
		}

		if (!votes.containsKey(world.getName())) {
			// No vote running in this world, nothing to update.
			return;
		}

		StringBuilder sb = new StringBuilder();
		AtomicInteger worldSize = new AtomicInteger(0);
		world.getPlayers().forEach(p -> {
			// Ignore players who are AFK due to idling
			if (!p.isSleepingIgnored()) {
				worldSize.incrementAndGet();
			}
		});

		BossBar bar = votes.get(world.getName()).getLeft();

		Bukkit.getOnlinePlayers().forEach(p ->  {
			if (p.getWorld().equals(world)) {
				bar.addPlayer(p);
			} else {
				bar.removePlayer(p);
			}
		});

		if (player != null) {
			sb.append(lang.getValue("sleep.player").replace("{PLAYER}", player)).append(' ');
		}

		double percent = worldSize.get() == 0 ? 1 : votes.get(world.getName()).getRight().size() / (double) worldSize.get();
		bar.setProgress(percent > .5 ? 1.0 : percent * 2);

		sb.append(lang.getValue("sleep.percent").replace("{PERCENT}", String.valueOf(percent * 100)));
		String title = null;

		if (percent >= 0.5) {
			title = lang.getValue("sleep.success");
			world.setTime(0);
			world.setStorm(false);
			world.setWeatherDuration(world.getWeatherDuration() > 12000 ? world.getWeatherDuration() : 12000);
			getLogger().info(world.getName() + " set to morning!");
			votes.remove(world.getName()).getLeft().removeAll();
		} else if (percent <= 0) {
			votes.remove(world.getName()).getLeft().removeAll();
		} else if (player == null) {
			// No spam on log in/out
			return;
		}

		String msg = sb.toString();
		for (Player p : world.getPlayers()) {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, JSONUtil.fromLegacyText(msg));
			if (title != null) {
				p.sendTitle("", title, 10, 40, 10);
			}
		}
	}

	private void resetVote(final World world) {
		if (!isEnabled()) {
			return;
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				Pair<BossBar, HashSet<String>> pair = votes.remove(world.getName());
				if (pair != null) {
					pair.getLeft().removeAll();
				}
			}
		}.runTaskLater(getPlugin(), 24001 - world.getTime());
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getName() {
		return "SleepVote";
	}

}
