package co.sblock.Sblock.Utilities.Spectator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.Module;
import co.sblock.Sblock.SblockCommand;
import co.sblock.Sblock.UserData.SblockUser;

/**
 * Module for managing players in a custom gamemode. Designed to allow players
 * to explore without giving any gamebreaking advantages.
 * 
 * @author Jikoo
 */
public class Spectators extends Module implements CommandListener {

	/**
	 * Minimal class for storing a player's location and fall distance prior to
	 * becoming a spectator.
	 * 
	 * @author Jikoo
	 */
	private class Entry {
		private Location l;
		private float fall;
		public Entry(Location l, float fall) {
			this.l = l;
			this.fall = fall;
		}
		public Location getLocation() {
			return l;
		}
		public float getFall() {
			return fall;
		}
	}

	/** The Spectators instance. */
	private static Spectators instance;

	/** The List of Players in spectator mode */
	private Map<String, Entry> spectators;

	/**
	 * @see co.sblock.Sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		instance = this;
		spectators = new HashMap<String, Entry>();
		this.registerCommands(this);
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

	/**
	 * Puts a player into spectator mode.
	 * 
	 * @param p the player to add
	 */
	public void addSpectator(Player p) {
		spectators.put(p.getName(), new Entry(p.getLocation(), p.getFallDistance()));
		p.setAllowFlight(true);
		p.setFlying(true);
		p.setNoDamageTicks(Integer.MAX_VALUE);
		p.closeInventory();
	}

	/**
	 * Check to see if a player is a spectator.
	 * 
	 * @param name the name of the player
	 * 
	 * @return true if the player is a spectator
	 */
	public boolean isSpectator(String name) {
		return spectators.containsKey(name);
	}

	/**
	 * Removes a player's spectator status.
	 * 
	 * @param p
	 */
	public void removeSpectator(Player p) {
		Entry e = spectators.remove(p.getName());
		p.teleport(e.getLocation());
		SblockUser.getUser(p.getName()).updateFlight();
		p.setNoDamageTicks(0);
		p.setFallDistance(e.getFall());
	}

	/**
	 * Command used ingame to toggle spectator status.
	 * 
	 * @param s the CommandSender
	 * @param args any additional command arguments
	 * 
	 * @return true
	 */
	@SblockCommand(description = "Player: Become the ghost (toggles spectator mode)", usage = "/spectate")
	public boolean spectate(CommandSender s, String[] args) {
		if (this.spectators.containsKey(s.getName())) {
			s.sendMessage(ChatColor.GREEN + "Suddenly, you snap back to reality. It was all a dream... wasn't it?");
			this.removeSpectator((Player) s);
		} else {
			s.sendMessage(ChatColor.GREEN + "You feel a tingling sensation about your extremities as you hover up slightly.");
			this.addSpectator((Player) s);
		}
		return true;
	}
}
