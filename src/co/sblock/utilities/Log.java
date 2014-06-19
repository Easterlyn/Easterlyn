package co.sblock.utilities;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

/**
 * A small utility to make logging easier on a per-module basis.
 *
 * @author Jikoo
 */
public class Log extends Logger {

    /*
     * The LogManager class is verbose beyond belief, we can model it more simply with a simple map
     */
    private static final Map<String, Log> loggerMap = new HashMap<String, Log>();

    /*
     * keep a static reference to the anonymous logger
     */
    private static final Log minecraftAnonymous = new Log("Minecraft", null);
    static {
        loggerMap.put("Minecraft", minecraftAnonymous);
    }

    /*
     * store the bracked name so that we can avoid string creation / concatination on ever log
     */
    public final StringBuilder BRACKETED_NAME;

    /**
     * Static factory method for generating named loggers
     *
     * @param name the name of the logger
     * @return a named logger
     */
    public static Log getLog(String name) {
        Log log = loggerMap.get(name);
        if (log == null) {
            log = new Log(name, null);
            loggerMap.put(name, log);
        }
        return log;
    }

    /**
     * Remove a logger from memoisation
     * Does not guarantee GC, but we can attempt to make that better
     *
     * @param name the name of the logger to remove
     * @return whether the logger was removed successfully
     */
    public static boolean remove(String name) {
        return loggerMap.remove(name) != null;
    }

    /**
     * Remove all entries from the logging map.
     * using this does not guarantee that the loggers will be GC'd
     * but hopefully we can work towards that.
     *
     * @return whether the map was purged successfully
     */
    public static boolean purge() {
        loggerMap.clear();
        return loggerMap.size() == 0;
    }

    /**
     * private constructor to inhibit needless instantiation, as well as subclassing
     *
     * @param name the name of the logger
     * @param localization the language (generally not used)
     */
    private Log(String name, String localization) {
        super(name, localization);
        BRACKETED_NAME = new StringBuilder("[").append(name).append("]");
    }

    /**
     * Hackish debug logging that won't spam Prime server.
     * TODO: make less hackish
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
        Bukkit.getConsoleSender().sendMessage(BRACKETED_NAME.append(msg).toString());
    }

    /**
     * Warning level logging.
     *
     * @param msg the String to log
     */
    public void warning(String msg) {
        super.warning(BRACKETED_NAME.append(msg).toString());
    }

    /**
     * Severe level logging.
     *
     * @param msg the String to log
     */
    public void severe(String msg) {
        super.warning(BRACKETED_NAME.append(msg).toString());
    }

    /**
     * Log an Exception in a reader-friendly text block.
     * <p>
     * Designed for non-breaking errors, warning level logging.
     *
     * @param e the Exception to log
     */
    public void err(Exception e) {
        StringBuilder trace = new StringBuilder("Error report:\n");
        trace.append(e.toString());
        for (StackTraceElement ste : e.getStackTrace()) {
            trace.append("\n\tat ").append(ste.toString());
        }
        if (e.getCause() != null) {
            trace.append("\nCaused by: ").append(e.getCause().toString());
            for (StackTraceElement ste : e.getCause().getStackTrace()) {
                trace.append("\n\tat ").append(ste.toString());
            }
        }
        warning(trace.append("\nEnd of error report.").toString());
    }

    /**
     * Log an Exception in a reader-friendly text block.
     * <p>
     * Designed for game-breaking errors, severe level logging.
     *
     * @param e the Exception to log
     */
    public void criticalErr(Exception e) {
        StringBuilder trace = new StringBuilder("Error report:\n");
        trace.append(e.toString());
        for (StackTraceElement ste : e.getStackTrace()) {
            trace.append("\n\tat ").append(ste.toString());
        }
        if (e.getCause() != null) {
            trace.append("\nCaused by: ").append(e.getCause().toString());
            for (StackTraceElement ste : e.getCause().getStackTrace()) {
                trace.append("\n\tat ").append(ste.toString());
            }
        }
        severe(trace.append("\nEnd of error report.").toString());
    }

    /**
     * Fine level logging labeled as 'Minecraft'
     *
     * @param msg the String to log
     */
    public static void anonymousFine(Object msg) {
        minecraftAnonymous.fine(msg.toString());
    }

    /**
     * Info level logging with no prepended name.
     *
     * @param msg the String to log
     */
    public static void anonymousInfo(String msg) {
        Bukkit.getConsoleSender().sendMessage(msg);
    }

}
