package co.sblock.micromodules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.module.Module;

/**
 * Utility to allow players to stop storms and end night.
 * 
 * @author Jikoo
 */
public class SleepVote extends Module {

	private final HashMap<String, HashSet<String>> votes = new HashMap<String, HashSet<String>>();

	public SleepVote(Sblock plugin) {
		super(plugin);
	}

	@Override
	protected void onEnable() { }

	@Override
	protected void onDisable() {
		this.votes.clear();
	}

	public void sleepVote(World world, Player p) {
		if (!isEnabled()) {
			return;
		}
		if (!votes.containsKey(world.getName())) {
			votes.put(world.getName(), new HashSet<String>());
			resetVote(world);
		}
		if (votes.get(world.getName()).add(p.getName())) {
			updateVotes(world, p.getDisplayName());
		}
	}

	/**
	 * Updates the percentage of players who have slept when a player is logging out or changing
	 * worlds.
	 * 
	 * @param world the world to update
	 * @return true if the count has changed
	 */
	public boolean updateVoteCount(final String world, String player) {
		if (!isEnabled()) {
			return false;
		}
		if (!votes.containsKey(world)) {
			return false;
		}
		if (player != null) {
			votes.get(world).remove(player);
		}

		updateVotes(Bukkit.getWorld(world), null);
		return true;
	}

	public void updateVotes(World world, String player) {
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
			// Essentials sets afk players to sleeping ignored
			if (!p.isSleepingIgnored()) {
				worldSize.incrementAndGet();
			}
		});
		if (player != null) {
			sb.append(Color.GOOD_PLAYER).append(player).append(Color.GOOD).append(" has gone to bed. ");
		} else {
			worldSize.decrementAndGet();
		}
		int percent = worldSize.get() == 0 ? 100 : 100 * votes.get(world.getName()).size() / worldSize.get();
		sb.append(Color.GOOD).append(percent).append("% of players have slept.");
		if (percent >= 50) {
			sb.append('\n').append("Time to get up, a new day awaits!");
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

	public void resetVote(final World world) {
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
