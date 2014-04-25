package co.sblock.utilities.progression;

import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import com.dsh105.holoapi.HoloAPI;
import com.dsh105.holoapi.api.Hologram;
import com.google.common.collect.HashBiMap;

import co.sblock.Sblock;
import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Icon;
import co.sblock.machines.type.Machine;
import co.sblock.machines.type.MachineType;
import co.sblock.users.ProgressionState;
import co.sblock.users.User;
import co.sblock.utilities.hologram.EntryTimeTillTag;
import co.sblock.utilities.meteors.Meteorite;

/**
 * Class containing functions controlling the Entry sequence.
 * 
 * @author Jikoo
 */
public class Entry {

	private static Entry instance;

	private HashBiMap<Hologram, UUID> holograms;

	private int task;

	public Entry() {
		holograms = HashBiMap.create();
		task = -1;
		HoloAPI.getTagFormatter().addFormat(Pattern.compile("\\%entry:([0-9]+)\\%"), new EntryTimeTillTag());
	}

	public boolean canStart(User user) {
		if (!holograms.values().contains(user.getUUID()) && user.getPrograms().contains(Icon.SBURBCLIENT.getProgramID())
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

		// Center hologram inside the space above the block
		final Location holoLoc = cruxtruder.clone().add(new Vector(0.5, 0, 0.5));
		// 4:13 = 253 seconds, 2 second display of 0:00
		holograms.put(HoloAPI.getManager().createSimpleHologram(holoLoc, 260,
				"%entry:" + (System.currentTimeMillis() + 254000) + "%"), user.getUUID());

		if (task != -1) {
			return;
		}

		task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new Runnable() {
			@Override
			public void run() {
				for (Hologram hologram : holograms.keySet().toArray(new Hologram[0])) {
					hologram.updateDisplay();
					long time = Long.parseLong(hologram.getLines()[0].replaceAll("\\%entry:([0-9]+)\\%", "$1"));

					if (time <= System.currentTimeMillis()) {
						User user = User.getUser(holograms.get(hologram));
						if (user != null && user.getProgression() == ProgressionState.NONE) {
							fail(User.getUser(holograms.get(hologram)));
						}
					}
				}

				if (holograms.size() == 0) {
					Bukkit.getScheduler().cancelTask(task);
					task = -1;
				}
			}
		}, 20, 20);
	}

	private void finish(User user) {
		Hologram holo = holograms.inverse().remove(user.getUUID());
		if (holo == null) {
			return;
		}
		// Set Hologram invisible (prevents potentially odd logouts)
		holo.clearAllPlayerViews();
		// Create a new Hologram of short duration for effect
		HoloAPI.getManager().createSimpleHologram(holo.getDefaultLocation(), 5, "0:00");

		// Create and drop a Meteorite. TODO display during Entry?
		new Meteorite(holo.getDefaultLocation(), Material.NETHERRACK.name(), 3, true).dropMeteorite();;

		// Reverts the Machine to its original state.
		Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(holo.getDefaultLocation().getBlock());
		if (m != null && m.getType() == MachineType.CRUXTRUDER) {
			m.dodge();
		}

		// Kicks the server out of server mode
		User server = User.getUser(user.getServer());
		if (server != null && server.isServer()) {
			server.stopServerMode();
		}
	}

	public void fail(User user) {
		finish(user);

		// Uninstalls the client program
		user.getPrograms().remove(Icon.SBURBCLIENT.getProgramID());
	}

	public static Entry getEntry() {
		if (instance == null) {
			instance = new Entry();
		}
		return instance;
	}

	public boolean isEntering(User user) {
		return holograms.containsValue(user.getUUID());
	}
}
