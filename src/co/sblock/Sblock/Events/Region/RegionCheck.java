package co.sblock.Sblock.Events.Region;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.sblock.Sblock.Chat2.ChatUser;
import co.sblock.Sblock.Chat2.ChatUserManager;
import co.sblock.Sblock.UserData.Region;

/**
 * Runnable used to update the <code>Region</code>s of all <code>Player</code>s.
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
				ChatUser u = ChatUserManager.getUserManager().getUser(p.getName());
				Region r = Region.getLocationRegion(p.getLocation());
				if (u.getPlayer().isOnline() && !u.getCurrentRegion().equals(r)) {
					u.updateCurrentRegion(r);
				}
			}
		} catch (NullPointerException e) {
		}
	}
}
