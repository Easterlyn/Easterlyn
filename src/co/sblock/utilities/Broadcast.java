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

	protected static Broadcast INSTANCE;

	protected Broadcast() {
		INSTANCE = this;
	}
	
	/**
	 * Broadcast as Lil Hal to all users.
	 */
	public void lilHalImpl(String msg) {
		Bukkit.broadcastMessage(ColorDef.HAL + msg);
	}

	/**
	 * General broadcast to all users.
	 */
	public void generalImpl(String msg) {
		Bukkit.broadcastMessage(msg);
	}
	
	/**
	 * Broadcast as Lil Hal to all users.
	 */
	public static void lilHal(String msg) {
		if (INSTANCE == null) {
			new Broadcast();
		}
		INSTANCE.lilHalImpl(msg);
	}

	/**
	 * General broadcast to all users.
	 */
	public static void general(String msg) {
		if (INSTANCE == null) {
			new Broadcast();
		}
		INSTANCE.generalImpl(msg);
	}
}
