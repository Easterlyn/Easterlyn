package co.sblock.Sblock.Utilities;

import org.bukkit.Bukkit;

import co.sblock.Sblock.Sblock;

/**
 * A small utility to make logging more easy on a per-module basis.
 * 
 * @author Jikoo
 */
public class Sblogger {
	/** The name to prepend the log message with. */
	private String logName;

	/**
	 * Constructor for Sblogger.
	 * 
	 * @param logName
	 *            the name to prepend the log message with
	 */
	public Sblogger(String logName) {
		this.logName = logName;
	}

	/**
	 * Info level logging.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public void info(String msg) {
		Bukkit.getConsoleSender().sendMessage("[" + logName + "] " + msg);
	}

	/**
	 * Warning level logging.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public void warning(String msg) {
		Bukkit.getLogger().warning("[" + logName + "] " + msg);
	}

	/**
	 * Severe level logging.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public void severe(String msg) {
		Bukkit.getLogger().severe("[" + logName + "] " + msg);
	}

	/**
	 * Info level logging with no prepended name.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public static void infoNoLogName(String msg) {
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
	public static void info(String logName, String msg) {
		Bukkit.getConsoleSender().sendMessage("[" + logName + "] " + msg);
	}

	/**
	 * Warning level logging with no prepended name.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public static void warningNoLogName(String msg) {
		Bukkit.getLogger().warning(msg);
	}

	/**
	 * Warning level logging.
	 * 
	 * @param logName
	 *            the name to prepend the log message with
	 * @param msg
	 *            the message to log
	 */
	public static void warning(String logName, String msg) {
		Bukkit.getLogger().warning("[" + logName + "] " + msg);
	}

	/**
	 * Severe level logging with no prepended name.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public static void severeNoLogName(String msg) {
		Bukkit.getLogger().severe(msg);
	}

	/**
	 * Severe level logging.
	 * 
	 * @param logName
	 *            the name to prepend the log message with
	 * @param msg
	 *            the message to log
	 */
	public static void severe(String logName, String msg) {
		Bukkit.getLogger().severe("[" + logName + "] " + msg);
	}

	/**
	 * Hackish debug logging that won't spam Prime server.
	 * 
	 * @param s the <code>String</code> to log
	 */
	public static void debug(String s) {
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
		warningNoLogName("Error report:\n" + trace.toString());
		warningNoLogName("End of error report.");
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
		severeNoLogName("Error report:\n" + trace);
		severeNoLogName("End of error report.");
	}
}
