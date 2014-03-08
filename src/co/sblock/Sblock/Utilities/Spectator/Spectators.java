package co.sblock.Sblock.Utilities.Spectator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import co.sblock.Sblock.Module;

/**
 * @author Jikoo
 *
 */
public class Spectators extends Module {

	/** The Spectators instance. */
	private static Spectators instance;

	/** The List of Players in spectator mode */
	private Map<String, Location> spectators;

	/**
	 * @see co.sblock.Sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		instance = this;
		spectators = new HashMap<String, Location>();
	}

	/**
	 * @see co.sblock.Sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		instance = null;
	}

	/**
	 * Gets the Spectators instance.
	 * 
	 * @return the Spectators instance.
	 */
	public static Spectators getSpectators() {
		return instance;
	}

	public Set<String> spectators() {
		return spectators.keySet();
	}

	public void addSpectator(Player p) {
		spectators.put(p.getName(), p.getLocation());
		p.setAllowFlight(true);
		p.setFlying(true);
		p.setNoDamageTicks(Integer.MAX_VALUE);
	}

	public boolean isSpectator(String name) {
		return spectators.containsKey(name);
	}

	public void removeSpectator(Player p) {
		// TODO can teleport a player prior to logout?
		p.teleport(spectators.remove(p.getName()));
		p.setFlying(false);
		p.setAllowFlight(false);
		p.setNoDamageTicks(0);
	}
}
