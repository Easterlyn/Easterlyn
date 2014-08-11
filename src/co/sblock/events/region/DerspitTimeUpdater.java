package co.sblock.events.region;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.users.Region;
import co.sblock.users.UserManager;

/**
 * 
 * 
 * @author Jikoo
 */
public class DerspitTimeUpdater extends BukkitRunnable {

	private final World derspit = Bukkit.getWorld("Derspit");

	@Override
	public void run() {
		derspit.setTime(4500L);
		derspit.setStorm(false);
		derspit.setThundering(false);
		derspit.setWeatherDuration(Integer.MAX_VALUE);
		for (Player player : derspit.getPlayers()) {
			if (UserManager.getUser(player.getUniqueId()).getCurrentRegion() == Region.OUTERCIRCLE) {
				player.setPlayerTime(18000L, false);
			} else {
				player.setPlayerTime(6000L, false);
			}
		}
	}
}
