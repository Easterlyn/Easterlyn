package co.sblock.Sblock.Utilities;

import java.nio.file.Paths;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

/**
 * A small utility to make logging more easy on a per-module basis.
 * 
 * @author Jikoo
 */
public class Log extends Logger {

	public Log(String name, String localization) {
		super(name, localization);
	}

	/**
	 * Hackish debug logging that won't spam Prime server.
	 * 
	 * @param s the String to log
	 */
	public void debug(Object s) {
		if (Paths.get("").toAbsolutePath().toString().contains("Prime")) {
			fine(s.toString());
		} else {
			info("DEBUG: " + s.toString());
		}
	}

	/**
	 * Info level logging.
	 * 
	 * @param msg the String to log
	 */
	public void info(String msg) {
		Bukkit.getConsoleSender().sendMessage("[" + this.getName() + "] " + msg);
	}

	/**
	 * Fine level logging with no prepended name.
	 * 
	 * @param msg the String to log
	 */
	public static void fineNoName(Object msg) {
		getLogger("Minecraft").fine(msg.toString());
	}

	/**
	 * Info level logging with no prepended name.
	 * 
	 * @param msg the String to log
	 */
	public static void infoNoName(String msg) {
		Bukkit.getConsoleSender().sendMessage(msg);
	}

	/**
	 * Log an Exception in a reader-friendly text block.
	 * <p>
	 * Designed for non-breaking errors, warning level logging.
	 * 
	 * @param e the Exception to log
	 */
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
		Logger.getLogger("Minecraft").warning("Error report:\n" + trace.toString()
				+ "\nEnd of error report.");
	}

	/**
	 * Log an Exception in a reader-friendly text block.
	 * <p>
	 * Designed for game-breaking errors, severe level logging.
	 * 
	 * @param e the Exception to log
	 */
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
		Logger.getLogger("Minecraft").severe("Error report:\n" + trace.toString()
				+ "\nEnd of error report.");
	}
}
