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
		if (votes.get(world.getName()) == null) {
			votes.put(world.getName(), new HashSet<String>());
			resetVote(world);
		}
		if (votes.get(world.getName()).add(p.getName())) {
			updateVotes(world, p.getName());
		}
	}

	public void updateVotes(World world, String player) {
		if (!votes.containsKey(world.getName())) {
			// Vote has succeeded, task is completing.
			return;
		}
		if (player == null) {
			// Timeout reached, should be day by now
			votes.remove(world.getName());
			return;
		}
		int percent = (int) 100 * votes.get(world.getName()).size() / world.getPlayers().size();
		StringBuilder sb = new StringBuilder(ChatColor.GREEN.toString()).append(player).append(ChatColor.YELLOW);
		sb.append(" has gone to bed. ").append(percent).append("% of players are now sleeping.");
		if (percent > 50) {
			sb.append('\n').append("Time to get up, a new day awaits!");
			world.setTime(0);
			world.setStorm(false);
			world.setWeatherDuration(world.getWeatherDuration() > 12000 ? world.getWeatherDuration() : 12000);
			votes.remove(world.getName());
		}
		String msg = sb.toString();
		for (Player p : world.getPlayers()) {
			p.sendMessage(msg);
		}
		Bukkit.getConsoleSender().sendMessage("[SleepVote] " + world.getName() + " set to morning!");
	}

	public void resetVote(final World world) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				updateVotes(world, null);
			}
		}, 24001 - world.getTime());
	}
}
