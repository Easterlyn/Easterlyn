package co.sblock.utilities.progression;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.users.UserManager;

/**
 * 
 * 
 * @author Jikoo
 */
public class EntryTimer extends BukkitRunnable {

	private SimpleDateFormat format;
//	private Hologram hologram;
	private long timeRemaining;
	private UUID uuid;

	public EntryTimer(Location holoLoc, UUID uuid) {
		format = new SimpleDateFormat("m:ss");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		timeRemaining = 253000;
//		hologram = HolographicDisplaysAPI.createHologram(Sblock.getInstance(), holoLoc, "4:13");
		this.uuid = uuid;
	}

	@Override
	public void run() {
//		hologram.setLine(0, format.format(timeRemaining > 0 ? timeRemaining : 0));
//		hologram.update();
		if (timeRemaining == 0) {
			Entry.getEntry().fail(UserManager.getUser(uuid));
		}
		if (timeRemaining < -1) {
			cancel();
		}
		timeRemaining -= 1000;
	}

	@Override
	public synchronized void cancel() throws IllegalStateException {
//		hologram.delete();
		super.cancel();
	}
}
