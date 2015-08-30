package co.sblock.events.listeners.player;

import java.util.regex.Pattern;

import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

/**
 * Listener for PlayerLoginEvents.
 * 
 * @author Jikoo
 */
public class LoginListener implements Listener {

	private final Pattern pattern;

	public LoginListener() {
		pattern = Pattern.compile("\\W");
	}

	/**
	 * The event handler for PlayerLoginEvents.
	 * 
	 * @param event the PlayerLoginEvent
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (event.getPlayer() == null) {
			return;
		}
		if (event.getPlayer().getName() != null
				&& pattern.matcher(event.getPlayer().getName()).find()) {
			event.setResult(Result.KICK_BANNED);
			event.setKickMessage("Your name contains invalid characters. Valid characters are [a-zA-Z_0-9]."
					+ "\nPlease contact Mojang about this issue.");
			return;
		}
		switch (event.getResult()) {
		case ALLOWED:
		case KICK_FULL:
		case KICK_WHITELIST:
			return;
		case KICK_BANNED:
		case KICK_OTHER:
			String reason = null;
			if (event.getPlayer().getName() != null
					&& Bukkit.getBanList(Type.NAME).isBanned(event.getPlayer().getName())) {
				reason = Bukkit.getBanList(Type.NAME).getBanEntry(event.getPlayer().getName()).getReason()
						.replaceAll("<ip=.*?>", "");
			} else {
				reason = Bukkit.getBanList(Type.IP).getBanEntry(event.getAddress().getHostAddress()).getReason()
						.replaceAll("<(uuid|name)=[\\w-]+?>", "");
			}
			if (reason != null) {
				event.setKickMessage(reason);
			}
			return;
		default:
			return;
		}
	}
}
