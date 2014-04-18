package co.sblock.Sblock.Events.Packets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.sblock.Sblock.Events.SblockEvents;
import co.sblock.Sblock.UserData.DreamPlanet;
import co.sblock.Sblock.UserData.Region;
import co.sblock.Sblock.UserData.User;

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
		User user = User.getUser(p.getUniqueId());
		if (p != null && user != null) {
			switch (Region.getLocationRegion(p.getLocation())) {
			case EARTH:
			case MEDIUM:
			case LOFAF:
			case LOHAC:
			case LOLAR:
			case LOWAS:
				if (user.getDreamPlanet() == DreamPlanet.NONE) {
					break;
				}
				SblockEvents.getEvents().teleports.add(p.getName());
				if (p.getWorld().equals(user.getPreviousLocation().getWorld())
						|| !user.getDreamPlanet().getWorldName()
								.equals(user.getPreviousLocation().getWorld().getName())) {
					p.teleport(SblockEvents.getEvents().getTowerData()
							.getLocation(user.getTower(), user.getDreamPlanet()));
				} else {
					p.teleport(user.getPreviousLocation());
				}
				break;
			case OUTERCIRCLE:
			case INNERCIRCLE:
				SblockEvents.getEvents().teleports.add(p.getName());
				if (p.getWorld().equals(user.getPreviousLocation().getWorld())) {
					p.teleport(Bukkit.getWorld("Earth").getSpawnLocation());
				} else {
					p.teleport(user.getPreviousLocation());
				}
				break;
			default:
				break;
			}

			SblockEvents.getEvents().fakeWakeUp(p);

		}
		SblockEvents.getEvents().tasks.remove(p.getName());
	}
}
