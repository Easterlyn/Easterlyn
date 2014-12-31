package co.sblock.utilities;

import java.nio.file.Paths;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

/**
 * A small utility to make logging more easy on a per-module basis.
 * 
 * @author Jikoo
 */
public class Log extends Logger {

	private final String BRACKETED_NAME;

	private Log(String name, String localization) {
		super(name, localization);
		BRACKETED_NAME = "[" + name + "] ";
		LogManager.getLogManager().addLogger(this);
	}

	/**
	 * Fancy magic for getting a logger named as the class that called it.
	 * If you're having trouble with the compiler causing an error instead of a deprecation warning, try
	 * http://stackoverflow.com/questions/860187/access-restriction-on-class-due-to-restriction-on-required-library-rt-jar
	 * @see http://stackoverflow.com/questions/421280/how-do-i-find-the-caller-of-a-method-using-stacktrace-or-reflection
	 */
	@SuppressWarnings("deprecation")
	public static Log getLog() {
		String name = sun.reflect.Reflection.getCallerClass(2).getName();
		return new Log(name, null);
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
		Bukkit.getConsoleSender().sendMessage(BRACKETED_NAME + msg);
	}

	/**
	 * Warning level logging.
	 * 
	 * @param msg the String to log
	 */
	public void warning(String msg) {
		super.warning(BRACKETED_NAME + msg);
	}

	/**
	 * Severe level logging.
	 * 
	 * @param msg the String to log
	 */
	public void severe(String msg) {
		super.warning(BRACKETED_NAME + msg);
	}

	/**
	 * Log an Exception in a reader-friendly text block.
	 * <p>
	 * Designed for non-breaking errors, warning level logging.
	 * 
	 * @param e the Exception to log
	 */
	public void err(Exception e) {
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
		warning("Error report:\n" + trace.toString() + "\nEnd of error report.");
	}

	/**
	 * Log an Exception in a reader-friendly text block.
	 * <p>
	 * Designed for game-breaking errors, severe level logging.
	 * 
	 * @param e the Exception to log
	 */
	public void criticalErr(Throwable e) {
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
		severe("Error report:\n" + trace.toString() + "\nEnd of error report.");
	}

	/**
	 * Info level logging with no prepended name.
	 * 
	 * @param msg the String to log
	 */
	public static void anonymousInfo(String msg) {
		Bukkit.getConsoleSender().sendMessage(msg);
	}

	public static Log getLog(String name) {
		Log log = null;
		try {
			log = (Log) LogManager.getLogManager().getLogger(name);
		} catch (ClassCastException e) {
			// We probably just reloaded.
		}
		if (log == null) {
			log = new Log(name, null);
			LogManager.getLogManager().addLogger(log);
		}
		return log;
	}
}
