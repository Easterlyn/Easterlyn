package co.sblock.utilities;

import org.bukkit.Bukkit;

import co.sblock.chat.ColorDef;

/**
 * A tiny class used to ensure that all announcements follow the same format.
 * 
 * @author Jikoo
 *
 */
public class Broadcast {

    /**
     * Broadcast as Lil Hal to all users.
     */
    public static void lilHal(String msg) {
        Bukkit.broadcastMessage(ColorDef.HAL + msg);
    }

    /**
     * General broadcast to all users.
     */
    public static void general(String msg) {
        Bukkit.broadcastMessage(msg);
    }
}
