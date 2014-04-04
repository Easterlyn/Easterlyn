package co.sblock.Sblock.Events.Listeners;

import java.util.regex.Pattern;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import co.sblock.Sblock.Database.SblockData;

/**
 * Listener for PlayerLoginEvents.
 * 
 * @author Jikoo
 */
public class PlayerLoginListener implements Listener {

	private Pattern pattern;

	public PlayerLoginListener() {
		pattern = Pattern.compile("\\W");
	}

	/**
	 * The event handler for PlayerLoginEvents.
	 * 
	 * @param event the PlayerLoginEvent
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (pattern.matcher(event.getPlayer().getName()).find()) {
			event.setResult(Result.KICK_BANNED);
			event.setKickMessage("Your name contains invalid characters. Valid characters are [a-zA-Z_0-9]."
					+ "\nPlease contact Mojang about this issue.");
		}
		switch (event.getResult()) {
		case ALLOWED:
		case KICK_FULL:
		case KICK_WHITELIST:
			return;
		case KICK_BANNED:
		case KICK_OTHER:
			String reason = SblockData.getDB().getBanReason(event.getPlayer().getName(),
					event.getAddress().getHostAddress());
			if (reason != null) {
				event.setKickMessage(reason);
			}
			return;
		default:
			return;
		}
	}
}
