/**
 * 
 */
package co.sblock.Sblock.Utilities;

import org.bukkit.Bukkit;

/**
 * A small utility to make logging more easy on a per-module basis.
 * 
 * @author Jikoo
 * 
 */
public class Sblogger {
	private String logName;

	public Sblogger(String logName) {
		this.logName = logName;
	}

	public void info(String msg) {
		Bukkit.getLogger().info("[" + logName + "] " + msg);
	}

	public void warning(String msg) {
		Bukkit.getLogger().warning("[" + logName + "] " + msg);
	}

	public void severe(String msg) {
		Bukkit.getLogger().severe("[" + logName + "] " + msg);
	}
}
