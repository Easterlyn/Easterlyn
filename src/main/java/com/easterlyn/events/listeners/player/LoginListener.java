package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.events.listeners.EasterlynListener;
import org.bukkit.BanList;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import java.util.regex.Pattern;

/**
 * Listener for PlayerLoginEvents.
 *
 * @author Jikoo
 */
public class LoginListener extends EasterlynListener {

	private final Language lang;
	private final Pattern pattern;

	public LoginListener(Easterlyn plugin) {
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
		if (this.pattern.matcher(event.getPlayer().getName()).find()) {
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
			case KICK_BANNED:
			case KICK_OTHER:
				String reason = event.getKickMessage();
				BanList banlist = Bukkit.getBanList(Type.NAME);
				String id;
				if (banlist.isBanned(id = event.getPlayer().getUniqueId().toString())
						|| banlist.isBanned(id = event.getPlayer().getName())
						|| (banlist = Bukkit.getBanList(Type.IP)).isBanned(id = event.getAddress().getHostAddress())) {
					reason = banlist.getBanEntry(id).getReason();
				}
				event.setKickMessage(reason.replaceAll("<(ip|uuid|name)=.*?>", ""));
				break;
			case ALLOWED:
			case KICK_FULL:
			case KICK_WHITELIST:
			default:
				break;
		}
	}

}
