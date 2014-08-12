package co.sblock.events.packets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.sblock.events.SblockEvents;
import co.sblock.users.User;
import co.sblock.users.UserManager;

/**
 * Causes a sleep teleport to occur.
 * 
 * @author Jikoo
 */
public class SleepTeleport implements Runnable {

	/** The Player to teleport. */
	private Player p;

	/**
	 * @param p the Player to teleport
	 */
	public SleepTeleport(Player p) {
		this.p = p;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		User user = UserManager.getUser(p.getUniqueId());
		if (p != null && user != null) {
			SblockEvents.getEvents().teleports.add(p.getName());
			if (!user.getDreamPlanet().getWorldName().equals(user.getPreviousLocation().getWorld().getName())) {
				p.teleport(Bukkit.getWorld(user.getDreamPlanet().getWorldName()).getSpawnLocation());
			} else {
				if (p.getWorld().equals(user.getPreviousLocation().getWorld())) {
					p.teleport(Bukkit.getWorld("Earth").getSpawnLocation());
				} else {
					p.teleport(user.getPreviousLocation());
				}
			}
			SblockEvents.getEvents().fakeWakeUp(p);
		}
		SblockEvents.getEvents().tasks.remove(p.getName());
	}
}
