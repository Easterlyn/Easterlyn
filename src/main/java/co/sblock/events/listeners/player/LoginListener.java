package co.sblock.events.listeners.player;

import java.util.regex.Pattern;

import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.events.listeners.SblockListener;

/**
 * Listener for PlayerLoginEvents.
 * 
 * @author Jikoo
 */
public class LoginListener extends SblockListener {

	private final Language lang;
	private final Pattern pattern;

	public LoginListener(Sblock plugin) {
		super(plugin);
		this.lang = plugin.getModule(Language.class);
		this.pattern = Pattern.compile("[^a-zA-Z_0-9]");
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
				&& this.pattern.matcher(event.getPlayer().getName()).find()) {
			/*
			 * One day we had a guy log in who had a space after his name. Played hell with plugins
			 * and couldn't be targeted by commands. Good times. Luckily, he wasn't malicious and
			 * probably didn't even realize how badly he could have screwed us over.
			 * 
			 * If Mojang screws up again, I am not dealing with it.
			 */
			event.setResult(Result.KICK_BANNED);
			event.setKickMessage(this.lang.getValue("events.login.illegalName"));
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
