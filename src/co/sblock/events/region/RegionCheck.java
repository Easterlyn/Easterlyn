package co.sblock.events.region;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Runnable used to update the Regions of all Players.
 * 
 * @author Jikoo, Dublek
 */
public class RegionCheck extends BukkitRunnable {
	@SuppressWarnings("unused")
	private final World[] medium = new World[] {Bukkit.getWorld("LOWAS"),Bukkit.getWorld("LOFAF"),Bukkit.getWorld("LOHAC"),Bukkit.getWorld("LOLAR")};
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
//		for (World world : medium) {
//			for (Player p : world.getPlayers()) {
//				// TODO calculate relative planets
//				// may just want separate loops because destination will be per-location per-planet anyway
//				// I.E. southeast planet, triangle northwest would have destination Derspit(InnerCircle)
//				//     east and south would have destination Derspit (OuterCircle) and other directions obvious
//			}
//		}
	}
}
