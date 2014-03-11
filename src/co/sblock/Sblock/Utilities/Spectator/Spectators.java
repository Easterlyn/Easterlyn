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
 * @author Jikoo
 *
 */
public class Spectators extends Module implements CommandListener {

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

	public void addSpectator(Player p) {
		spectators.put(p.getName(), p.getLocation());
		p.setAllowFlight(true);
		p.setFlying(true);
		p.setNoDamageTicks(Integer.MAX_VALUE);
		p.closeInventory();
	}

	public boolean isSpectator(String name) {
		return spectators.containsKey(name);
	}

	public void removeSpectator(Player p) {
		// TODO can teleport a player prior to logout?
		p.teleport(spectators.remove(p.getName()));
		SblockUser.getUser(p.getName()).updateFlight();
		p.setNoDamageTicks(0);
	}

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
