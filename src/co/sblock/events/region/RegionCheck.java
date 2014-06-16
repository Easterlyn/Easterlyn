package co.sblock.events.region;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.sblock.events.SblockEvents;
import co.sblock.users.Region;
import co.sblock.users.User;

/**
 * Runnable used to update the Regions of all Players.
 * 
 * @author Jikoo, Dublek
 */
public class RegionCheck implements Runnable {
    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            for (Player p : Bukkit.getWorld("Medium").getPlayers()) {
                User u = User.getUser(p.getUniqueId());
                Region r = Region.getLocationRegion(p.getLocation());
                if (!u.getCurrentRegion().equals(r)) {
                    u.updateCurrentRegion(r);
                }
            }
        } catch (NullPointerException e) {
            SblockEvents.getEvents().getLogger().debug("Region update error:\n" + e.getCause());
        }
    }
}
