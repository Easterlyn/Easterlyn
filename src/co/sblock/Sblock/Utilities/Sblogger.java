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
		Bukkit.getConsoleSender().sendMessage("[" + logName + "] " + msg);
	}

	public void warning(String msg) {
		Bukkit.getLogger().warning("[" + logName + "] " + msg);
	}

	public void severe(String msg) {
		Bukkit.getLogger().severe("[" + logName + "] " + msg);
	}

	public static void infoNoLogName(String msg) {
		Bukkit.getConsoleSender().sendMessage(msg);
	}

	public static void info(String logName, String msg) {
		Bukkit.getConsoleSender().sendMessage("[" + logName + "] " + msg);
	}

	public static void warning(String logName, String msg) {
		Bukkit.getLogger().warning("[" + logName + "] " + msg);
	}

	public static void severe(String logName, String msg) {
		Bukkit.getLogger().severe("[" + logName + "] " + msg);
	}
}
