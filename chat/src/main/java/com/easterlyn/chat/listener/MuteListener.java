package com.easterlyn.chat.listener;

import com.easterlyn.EasterlynChat;
import com.easterlyn.EasterlynCore;
import com.easterlyn.chat.event.UserChatEvent;
import com.easterlyn.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public class MuteListener implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onUserChat(UserChatEvent event) {
		if (event.getUser().getStorage().getLong(EasterlynChat.USER_MUTE, 0L) > System.currentTimeMillis()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		cancelIfMute(event.getPlayer(), event);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onBookEdit(PlayerEditBookEvent event) {
		cancelIfMute(event.getPlayer(), event);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onSignChange(SignChangeEvent event) {
		cancelIfMute(event.getPlayer(), event);
	}

	private void cancelIfMute(Player player, Cancellable cancellable) {
		RegisteredServiceProvider<EasterlynCore> easterlynProvider = Bukkit.getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (easterlynProvider == null) {
			return;
		}

		User user = easterlynProvider.getProvider().getUserManager().getUser(player.getUniqueId());
		if (user.getStorage().getLong(EasterlynChat.USER_MUTE, 0L) > System.currentTimeMillis()) {
			cancellable.setCancelled(true);
		}
	}

}
