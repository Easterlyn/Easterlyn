package co.sblock.utilities;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import co.sblock.Sblock;

/**
 * Container for Holograms while Machines undergo a rework.
 * 
 * @author Jikoo
 */
public class Holograms {

	private static Map<Location, Hologram> holograms;

	private static void instantiate() {
		if (holograms == null) {
			holograms = new HashMap<>();
		}
	}

	public static Hologram getHologram(Location location) {
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			return null;
		}
		instantiate();
		if (holograms.containsKey(location)) {
			return holograms.get(location);
		}
		Hologram hologram = HologramsAPI.createHologram(Sblock.getInstance(), location);
		holograms.put(location, hologram);
		return hologram;
	}

	public static void removeHologram(Location location) {
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			return;
		}
		instantiate();
		if (holograms.containsKey(location)) {
			holograms.remove(location).delete();
		}
	}

	public static void disable() {
		if (holograms == null) {
			return;
		}
		for (Hologram hologram : holograms.values()) {
			hologram.delete();
		}
		holograms.clear();
		holograms = null;
	}
}
