package co.sblock.Sblock.Utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * A tiny class used to ensure that all announcements follow the same format.
 * 
 * @author Jikoo
 *
 */
public class LilHal {

	/**
	 * Broadcast as Lil Hal to all users.
	 */
	public static void tellAll(String msg) {
		Bukkit.broadcastMessage(ChatColor.RED + "[Lil Hal] " + msg);
	}

	/**
	 * Broadcast to mods+
	 */
	public static void tellMods(String msg) {
		Bukkit.broadcast(ChatColor.RED + "[Lil Hal] Mods only: " + msg, "group.denizen");
	}
}
