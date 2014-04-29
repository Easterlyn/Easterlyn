package co.sblock.utilities.progression;

import java.util.HashMap;
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
import co.sblock.machines.MachineManager;
import co.sblock.machines.type.Icon;
import co.sblock.machines.type.Machine;
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

//	private final Material[] materials;
	private HashBiMap<Hologram, UUID> holograms;
	private HashMap<UUID, Meteorite> meteors;

	private int task;

	public Entry() {
//		materials = createMaterialList();
		holograms = HashBiMap.create();
		meteors = new HashMap<>();
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

	public boolean isEntering(User user) {
		return holograms.containsValue(user.getUUID());
	}


	public void startEntry(User user, Location cruxtruder) {
		if (!canStart(user)) {
			return;
		}

		// Center hologram inside the space above the block
		final Location holoLoc = cruxtruder.clone().add(new Vector(0.5, 0, 0.5));
		// 4:13 = 253 seconds, 2 second display of 0:00
		// Set to 254 seconds because 1ms delay and rounding causes the display to start at 4:13
		holograms.put(HoloAPI.getManager().createSimpleHologram(holoLoc, 260,
				"%entry:" + (System.currentTimeMillis() + 254000) + "%"), user.getUUID());
		Meteorite meteorite = new Meteorite(holoLoc, Material.NETHERRACK.name(), 3, true, -1);
		// 254 seconds * 20 ticks per second = 5080
		meteorite.hoverMeteorite(5080);
		meteors.put(user.getUUID(), meteorite);

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
		// Set Hologram invisible (necessary since logout = failure)
		holo.clearAllPlayerViews();
		// Create a new Hologram of short duration for effect
		HoloAPI.getManager().createSimpleHologram(holo.getDefaultLocation(), 5, "0:00");

		// Drop the Meteor created.
		Meteorite meteorite = meteors.remove(user.getUUID());
		if (!meteorite.hasDropped()) {
			meteorite.dropMeteorite();
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

		// Removes all free machines placed by the User or their server
		for (Machine m : MachineManager.getManager().getMachines(user.getUUID())) {
			if (m.getType().isFree()) {
				m.remove();
			}
		}
	}

	public void succeed(User user) {
		finish(user);

		// TODO
	}

	public static Entry getEntry() {
		if (instance == null) {
			instance = new Entry();
		}
		return instance;
	}

	private Material[] createMaterialList() {
		return null; // TODO
	}
}
