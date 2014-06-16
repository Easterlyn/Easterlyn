package co.sblock.utilities.spectator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import co.sblock.module.CommandDescription;
import co.sblock.module.CommandListener;
import co.sblock.module.CommandUsage;
import co.sblock.module.Module;
import co.sblock.module.SblockCommand;
import co.sblock.users.User;
import co.sblock.utilities.inventory.InventoryManager;

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
		private PotionEffect[] effects;
		public Entry(Location l, float fall, PotionEffect[] effects) {
			this.l = l;
			this.fall = fall;
			this.effects = effects;
		}
		public Location getLocation() {
			return l;
		}
		public float getFall() {
			return fall;
		}
		public PotionEffect[] getPotionEffects() {
			return effects;
		}
	}

	/** The Spectators instance. */
	private static Spectators instance;

	/** The List of Players in spectator mode */
	private Map<UUID, Entry> spectators;

	/**
	 * @see co.sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		instance = this;
		spectators = new HashMap<>();
		this.registerCommands(this);
	}

	/**
	 * @see co.sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		instance = null;
		for (UUID u : spectators.keySet().toArray(new UUID[0])) {
			Player p = Bukkit.getPlayer(u);
			if (p != null) {
				this.removeSpectator(p);
			}
		}
	}

	/**
	 * Gets the Spectators instance.
	 * 
	 * @return the Spectators instance.
	 */
	public static Spectators getSpectators() {
		return instance;
	}

	public Set<UUID> spectators() {
		return spectators.keySet();
	}

	/**
	 * Puts a player into spectator mode.
	 * 
	 * @param p the player to add
	 */
	public void addSpectator(Player p) {
		spectators.put(p.getUniqueId(), new Entry(p.getLocation(), p.getFallDistance(),
				p.getActivePotionEffects().toArray(new PotionEffect[0]).clone()) );
		p.getActivePotionEffects().clear();
		p.setAllowFlight(true);
		p.setFlying(true);
		p.setNoDamageTicks(Integer.MAX_VALUE);
		p.closeInventory();
		InventoryManager.storeAndClearInventory(p);
	}

	/**
	 * Check to see if a player is a spectator.
	 * 
	 * @param name the name of the player
	 * 
	 * @return true if the player is a spectator
	 */
	public boolean isSpectator(UUID userID) {
		return spectators.containsKey(userID);
	}

	/**
	 * Removes a player's spectator status.
	 * 
	 * @param p
	 */
	public void removeSpectator(Player p) {
		Entry e = spectators.remove(p.getUniqueId());
		p.teleport(e.getLocation());
		User.getUser(p.getUniqueId()).updateFlight();
		p.setNoDamageTicks(0);
		p.setFallDistance(e.getFall());
		InventoryManager.restoreInventory(p);
		for (PotionEffect potion : e.getPotionEffects()) {
			p.addPotionEffect(potion);
		}
	}

	/**
	 * Command used ingame to toggle spectator status.
	 * 
	 * @param s the CommandSender
	 * @param args any additional command arguments
	 * 
	 * @return true
	 */
	@CommandDescription("Player: Become the ghost (toggles spectator mode)")
	@CommandUsage("/spectate")
	@SblockCommand
	public boolean spectate(CommandSender s, String[] args) {
		if (User.getUser(((Player) s).getUniqueId()).isServer()) {
			s.sendMessage(ChatColor.RED + "Perhaps you should focus on helping your client!");
			return true;
		}
		if (this.spectators.containsKey(((Player) s).getUniqueId())) {
			s.sendMessage(ChatColor.GREEN + "Suddenly, you snap back to reality. It was all a dream... wasn't it?");
			this.removeSpectator((Player) s);
		} else {
			s.sendMessage(ChatColor.GREEN + "You feel a tingling sensation about your extremities as you hover up slightly.");
			this.addSpectator((Player) s);
		}
		return true;
	}
}
