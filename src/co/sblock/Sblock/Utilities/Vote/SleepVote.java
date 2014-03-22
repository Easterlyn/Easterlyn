package co.sblock.Sblock.Utilities.Vote;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import co.sblock.Sblock.Sblock;

/**
 * Utility to allow players to stop storms and end night.
 * 
 * @author Jikoo
 */
public class SleepVote {

	private static SleepVote instance;

	private HashMap<String, HashSet<String>> votes;

	/**
	 * Vote singleton.
	 * 
	 * @return the Vote instance
	 */
	public static SleepVote getInstance() {
		if (instance == null) {
			instance = new SleepVote();
		}
		return instance;
	}

	public SleepVote() {
		votes = new HashMap<String, HashSet<String>>();
	}

	public void sleepVote(World world, Player p) {
		if (!votes.containsKey(world.getName())) {
			votes.put(world.getName(), new HashSet<String>());
			resetVote(world);
		}
		if (votes.get(world.getName()).add(p.getName())) {
			updateVotes(world, p.getName());
		}
	}

	/**
	 * Updates the percentage of players who have slept.
	 * Intended for use when a player is logging in or out.
	 * 
	 * @param world the world to update
	 */
	public void updateVoteCount(final World world) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				updateVotes(world, null);
			}
		}, 0);
	}

	public void updateVotes(World world, String player) {
		if (!votes.containsKey(world.getName())) {
			// No vote running in this world, nothing to update.
			return;
		}
		StringBuilder sb = new StringBuilder();
		if (player != null) {
			sb.append(ChatColor.GREEN).append(player).append(ChatColor.YELLOW).append(" has gone to bed. ");
		}
		int percent = (int) 100 * votes.get(world.getName()).size() / world.getPlayers().size();
		sb.append(ChatColor.YELLOW).append("% of players have slept.");
		if (percent > 50) {
			sb.append('\n').append("Time to get up, a new day awaits!");
			world.setTime(0);
			world.setStorm(false);
			world.setWeatherDuration(world.getWeatherDuration() > 12000 ? world.getWeatherDuration() : 12000);
			Bukkit.getConsoleSender().sendMessage("[SleepVote] " + world.getName() + " set to morning!");
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
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				votes.remove(world.getName());
			}
		}, 24001 - world.getTime());
	}
}
