/**
 * 
 */
package co.sblock.Sblock.Utilities;

//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
//import org.bukkit.command.CommandSender;

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
//		try {
//			Class<? extends CommandSender> clazz = Class.forName("net.minecraft.server."
//					+ Bukkit.getVersion().replaceAll(".*([0-9]\\.[0-9]\\.[0-9]-R[0-9]\\.[0-9]).*", "$1")
//					+ ".command.ColouredConsoleSender").asSubclass(CommandSender.class);
//			CommandSender suckItBukkit = clazz.getDeclaredConstructor().newInstance();
//			suckItBukkit.sendMessage("[" + logName + "] " + msg);
//			
//		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Bukkit.getConsoleSender().sendMessage("[" + logName + "] " + msg);
//		}
	}

	public static void warningNoLogName(String msg) {
		Bukkit.getLogger().warning(msg);
	}

	public static void warning(String logName, String msg) {
		Bukkit.getLogger().warning("[" + logName + "] " + msg);
	}

	public static void severeNoLogName(String msg) {
		Bukkit.getLogger().severe(msg);
	}

	public static void severe(String logName, String msg) {
		Bukkit.getLogger().severe("[" + logName + "] " + msg);
	}

	public static void err(Exception e) {
		StringBuilder trace = new StringBuilder(e.toString());
		for (StackTraceElement ste : e.getStackTrace()) {
			trace.append("\n\tat " + ste.toString());
		}
		if (e.getCause() != null) {
			trace.append("\nCaused by: " + e.getCause().toString());
			for (StackTraceElement ste : e.getCause().getStackTrace()) {
				trace.append("\n\tat " + ste.toString());
			}
		}
		warningNoLogName("Error report:\n" + trace.toString());
		warningNoLogName("End of error report.");
	}

	public static void criticalErr(Exception e) {
		StringBuilder trace = new StringBuilder(e.toString());
		for (StackTraceElement ste : e.getStackTrace()) {
			trace.append("\n\tat " + ste.toString());
		}
		if (e.getCause() != null) {
			trace.append("\nCaused by: " + e.getCause().toString());
			for (StackTraceElement ste : e.getCause().getStackTrace()) {
				trace.append("\n\tat " + ste.toString());
			}
		}
		severeNoLogName("Error report:\n" + trace);
		severeNoLogName("End of error report.");
	}
}
