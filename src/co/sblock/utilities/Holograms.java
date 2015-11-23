package co.sblock.utilities;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import co.sblock.Sblock;
import co.sblock.module.Module;

/**
 * Container for Holograms while Machines undergo a rework.
 * 
 * @author Jikoo
 */
public class Holograms extends Module {


	private Map<Location, Hologram> holograms;

	public Holograms(Sblock plugin) {
		super(plugin);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onEnable() { }

	@Override
	protected void onDisable() {
		if (holograms == null) {
			return;
		}
		for (Hologram hologram : holograms.values()) {
			hologram.delete();
		}
		holograms.clear();
		holograms = null;
	}

	public Hologram getHologram(Location location) {
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			return null;
		}
		if (holograms.containsKey(location)) {
			return holograms.get(location);
		}
		Hologram hologram = HologramsAPI.createHologram(getPlugin(), location);
		holograms.put(location, hologram);
		return hologram;
	}

	public void removeHologram(Location location) {
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			return;
		}
		if (holograms.containsKey(location)) {
			holograms.remove(location).delete();
		}
	}

	@Override
	public String getName() {
		return "Holograms";
	}
}
