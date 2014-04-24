package co.sblock.utilities.progression;

import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.dsh105.holoapi.HoloAPI;
import com.dsh105.holoapi.api.Hologram;

import co.sblock.Sblock;
import co.sblock.machines.type.Icon;
import co.sblock.users.ProgressionState;
import co.sblock.users.User;
import co.sblock.utilities.hologram.EntryTimeTillTag;

/**
 * Class containing functions controlling the Entry sequence.
 * 
 * @author Jikoo
 */
public class Entry {

	private static Entry instance;

	private HashSet<UUID> users;
	private HashSet<Hologram> holograms;

	private int task;

	public Entry() {
		users = new HashSet<>();
		holograms = new HashSet<>();
		task = -1;
		HoloAPI.getTagFormatter().addFormat(Pattern.compile("%entry:\\w{3,16}:\\d+%"), new EntryTimeTillTag());
	}

	public boolean canStart(User user) {
		if (!users.contains(user.getUUID()) && user.getPrograms().contains(Icon.SBURBCLIENT.getProgramID())
				&& user.getProgression() == ProgressionState.NONE) {
			return true;
		}

		// User has started or finished Entry already or not installed the SburbClient.
		return false;
	}

	public void startEntry(User user, Location cruxtruder) {
		if (!canStart(user)) {
			return;
		}

		users.add(user.getUUID());
		Location holoLoc = cruxtruder.clone().add(new Vector(0, 1, 0));
		// 4:13 = 253 seconds, 2 second display of 0:00
		holograms.add(HoloAPI.getManager().createSimpleHologram(holoLoc, 255,
				"%entry:" + user.getPlayerName() + ":" + (System.currentTimeMillis() + 253000) + "%"));

		if (task == -1) {
			task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new Runnable() {
				@Override
				public void run() {
					for (Hologram hologram : holograms.toArray(new Hologram[0])) {
						long time = Long.getLong(hologram.getLines()[0].replaceAll("%entry:\\w{3,16}:(\\d+)%", ""));
						if (time <= System.currentTimeMillis()) {
							holograms.remove(hologram);
							// dropMeteor hologram.getLocation
							//if User.getUser(Bukkit.getPlayer(hologram.getLines()[0]).getUniqueId()).getProgression() != ProgressionState.ENTRY
							// user.removeProgram(Icon.SBURBCLIENT.getProgramID())
						}
						hologram.updateDisplay();
					}

					if (holograms.size() == 0) {
						Bukkit.getScheduler().cancelTask(task);
						task = -1;
					}
				}
			}, 20, 20);
		}
	}

	public static Entry getEntry() {
		if (instance == null) {
			instance = new Entry();
		}
		return instance;
	}
}
