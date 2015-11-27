package co.sblock.progression;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

/**
 * A small BukkitRunnable for managing the Cruxtruder's 4:13 countdown.
 * 
 * @author Jikoo
 */
public class EntryTimer extends BukkitRunnable {

	private final Entry entry;
	private final SimpleDateFormat format;
	private final Hologram hologram;
	private long timeRemaining;
	private final UUID uuid;

	public EntryTimer(Entry entry, Location holoLoc, UUID uuid) {
		this.entry = entry;
		format = new SimpleDateFormat("m:ss");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		timeRemaining = 253000;
		hologram = HologramsAPI.createHologram(entry.getPlugin(), holoLoc);
		hologram.appendTextLine("4:13");
		this.uuid = uuid;
	}

	@Override
	public void run() {
		hologram.clearLines();
		hologram.appendTextLine(format.format(timeRemaining > 0 ? timeRemaining : 0));
		if (timeRemaining == 0) {
			entry.fail(uuid);
		}
		if (timeRemaining < -1) {
			cancel();
		}
		timeRemaining -= 1000;
	}

	@Override
	public synchronized void cancel() throws IllegalStateException {
		hologram.delete();
		super.cancel();
	}
}
