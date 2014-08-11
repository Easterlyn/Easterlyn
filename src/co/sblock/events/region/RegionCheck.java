package co.sblock.events.region;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.users.Region;
import co.sblock.users.User;
import co.sblock.users.UserManager;

/**
 * Runnable used to update the Regions of all Players.
 * 
 * @author Jikoo, Dublek
 */
public class RegionCheck extends BukkitRunnable {
	private final World[] medium = new World[] {Bukkit.getWorld("LOWAS"),Bukkit.getWorld("LOFAF"),Bukkit.getWorld("LOHAC"),Bukkit.getWorld("LOLAR")};
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		for (World world : medium) {
			for (Player p : world.getPlayers()) {
				User u = UserManager.getUser(p.getUniqueId());
				Region r = Region.getLocationRegion(p.getLocation());
				if (!u.getCurrentRegion().equals(r)) {
					u.updateCurrentRegion(r);
				}
			}
		}
	}
}
