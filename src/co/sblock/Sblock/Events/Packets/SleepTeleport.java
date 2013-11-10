package co.sblock.Sblock.Events.Packets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.sblock.Sblock.Events.EventModule;
import co.sblock.Sblock.UserData.DreamPlanet;
import co.sblock.Sblock.UserData.Region;
import co.sblock.Sblock.UserData.SblockUser;

/**
 * Causes a sleep teleport to occur.
 * 
 * @author Jikoo
 */
public class SleepTeleport implements Runnable {

	/** The <code>Player</code> to teleport. */
	private Player p;

	/**
	 * @param p
	 *            the <code>Player</code> to teleport
	 */
	public SleepTeleport(Player p) {
		this.p = p;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		SblockUser user = SblockUser.getUser(p.getName());
		if (p != null && user != null) {
			switch (Region.getLocationRegion(p.getLocation())) {
			case EARTH:
//			case MEDIUM: // Someday, my pretties.
//			case LOFAF:
//			case LOHAC:
//			case LOLAR:
//			case LOWAS:
				if (user.getDPlanet().equals(DreamPlanet.NONE)) {
					break;
				} else {
					EventModule.getEventModule().getListener().teleports.add(p.getName());
					if (p.getWorld().equals(user.getPreviousLocation().getWorld())) {
						p.teleport(EventModule.getEventModule().getTowerData()
								.getLocation(user.getTower(), user.getDPlanet()));
					} else {
						p.teleport(user.getPreviousLocation());
					}
				}
				break;
			case OUTERCIRCLE:
			case INNERCIRCLE:
				EventModule.getEventModule().getListener().teleports.add(p.getName());
				if (p.getWorld().equals(user.getPreviousLocation().getWorld())) {
					p.teleport(Bukkit.getWorld("Earth").getSpawnLocation());
				} else {
					p.teleport(user.getPreviousLocation());
				}
				break;
			default:
				break;
			}

			EventModule.getEventModule().getListener().fakeWakeUp(p);

		}
		EventModule.getEventModule().getListener().tasks.remove(p.getName());
	}
}
