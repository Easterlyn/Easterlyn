package co.sblock.Sblock.Utilities;

import java.util.logging.Logger;

import org.bukkit.Bukkit;

import co.sblock.Sblock.Sblock;

/**
 * A small utility to make logging more easy on a per-module basis.
 * <p>
 * Extends Logger because my compiler warns when errors aren't logged.
 * 
 * @author Jikoo
 */
public class Log extends Logger {
	/**
	 * @deprecated DO NOT USE.
	 */
	private Log(String arg0, String arg1) {
		super(arg0, arg1);
	}

	/**
	 * Info level logging.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public void info(String msg) {
		Bukkit.getConsoleSender().sendMessage("[Sblock] " + msg);
	}

	/**
	 * Warning level logging.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public void warning(String msg) {
		getLogger("Minecraft").warning("[Sblock] " + msg);
	}

	/**
	 * Severe level logging.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public void severe(String msg) {
		getLogger("Minecraft").severe("[Sblock] " + msg);
	}

	/**
	 * Fine level logging with no prepended name.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public static void fineNoName(Object msg) {
		getLogger("Minecraft").fine(msg.toString());
	}

	/**
	 * Fine level logging.
	 * 
	 * @param logName
	 *            the name to prepend the log message with
	 * @param msg
	 *            the message to log
	 */
	public static void fine(String logName, Object msg) {
		getLogger("Minecraft").fine("[" + logName + "] " + msg.toString());
	}

	/**
	 * Info level logging with no prepended name.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public static void infoNoName(String msg) {
		Bukkit.getConsoleSender().sendMessage(msg);
	}

	/**
	 * Info level logging.
	 * 
	 * @param logName
	 *            the name to prepend the log message with
	 * @param msg
	 *            the message to log
	 */
	public static void info(String logName, Object msg) {
		Bukkit.getConsoleSender().sendMessage("[" + logName + "] " + msg.toString());
	}

	/**
	 * Warning level logging with no prepended name.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public static void warningNoName(Object msg) {
		getLogger("Minecraft").warning(msg.toString());
	}

	/**
	 * Warning level logging.
	 * 
	 * @param logName
	 *            the name to prepend the log message with
	 * @param msg
	 *            the message to log
	 */
	public static void warning(String logName, Object msg) {
		getLogger("Minecraft").warning("[" + logName + "] " + msg.toString());
	}

	/**
	 * Severe level logging with no prepended name.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public static void severeNoName(Object msg) {
		Bukkit.getLogger().severe(msg.toString());
	}

	/**
	 * Severe level logging.
	 * 
	 * @param logName
	 *            the name to prepend the log message with
	 * @param msg
	 *            the message to log
	 */
	public static void severe(String logName, Object msg) {
		Bukkit.getLogger().severe("[" + logName + "] " + msg.toString());
	}

	/**
	 * Hackish debug logging that won't spam Prime server.
	 * 
	 * @param s the <code>String</code> to log
	 */
	public static void fineDebug(Object s) {
		if (!Sblock.getInstance().getDataFolder().getAbsolutePath().contains("Prime")) {
			fine("DEBUG", s);
		}
	}

	/**
	 * Hackish debug logging that won't spam Prime server.
	 * 
	 * @param s the <code>String</code> to log
	 */
	public static void debug(Object s) {
		if (!Sblock.getInstance().getDataFolder().getAbsolutePath().contains("Prime")) {
			info("DEBUG", s);
		}
	}

	/**
	 * Log an <code>Exception</code> in a reader-friendly text block.
	 * <p>
	 * Designed for non-breaking errors, warning level logging.
	 * 
	 * @param e
	 *            the <code>Exception</code> to log
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
		warningNoName("Error report:\n" + trace.toString());
		warningNoName("End of error report.");
	}

	/**
	 * Log an <code>Exception</code> in a reader-friendly text block.
	 * <p>
	 * Designed for game-breaking errors, severe level logging.
	 * 
	 * @param e
	 *            the <code>Exception</code> to log
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
		severeNoName("Error report:\n" + trace);
		severeNoName("End of error report.");
	}
}
