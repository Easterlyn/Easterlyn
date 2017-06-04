package com.easterlyn.micromodules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.module.Module;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Utility to allow players to stop storms and end night.
 * 
 * @author Jikoo
 */
public class SleepVote extends Module {

	private final HashMap<String, HashSet<String>> votes = new HashMap<>();

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
			votes.put(world.getName(), new HashSet<>());
			resetVote(world);
		}
		if (votes.get(world.getName()).add(p.getName())) {
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
			voted = votes.get(world.getName()).remove(player.getName());
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

		if (player != null) {
			sb.append(lang.getValue("sleep.player").replace("{PLAYER}", player)).append(' ');
		}

		int percent = worldSize.get() == 0 ? 100 : 100 * votes.get(world.getName()).size() / worldSize.get();
		sb.append(lang.getValue("sleep.percent").replace("{PERCENT}", String.valueOf(percent)));
		if (percent >= 50) {
			sb.append('\n').append(lang.getValue("sleep.success"));
			world.setTime(0);
			world.setStorm(false);
			world.setWeatherDuration(world.getWeatherDuration() > 12000 ? world.getWeatherDuration() : 12000);
			getLogger().info(world.getName() + " set to morning!");
			votes.remove(world.getName());
		} else if (player == null) {
			// No spam on log in/out
			return;
		}

		String msg = sb.toString();
		for (Player p : world.getPlayers()) {
			p.sendMessage(msg);
		}
	}

	private void resetVote(final World world) {
		if (!isEnabled()) {
			return;
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				votes.remove(world.getName());
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
