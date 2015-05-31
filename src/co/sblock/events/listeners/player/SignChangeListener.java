package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import net.md_5.bungee.api.ChatColor;

/**
 * Listener for SignChangeEvents.
 * 
 * @author Jikoo
 */
public class SignChangeListener implements Listener {

	/**
	 * The event handler for SignChangeEvents.
	 * <p>
	 * Allows signs to be colored using &codes.
	 * 
	 * @param event the SignChangeEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		for (int i = 0; i < event.getLines().length; i++) {
			event.setLine(i, ChatColor.translateAlternateColorCodes('&', event.getLine(i)));
		}
	}
}
