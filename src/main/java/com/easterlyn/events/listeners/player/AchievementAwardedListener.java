package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.FreeCart;

import org.bukkit.Achievement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;

/**
 * Listener for PlayerAchievementAwardedEvents.
 * 
 * @author Jikoo
 */
public class AchievementAwardedListener extends EasterlynListener {

	private final FreeCart carts;

	public AchievementAwardedListener(Easterlyn plugin) {
		super(plugin);
		this.carts = plugin.getModule(FreeCart.class);
	}

	@EventHandler
	public void onPlayerAchievementAwarded(PlayerAchievementAwardedEvent event) {
		// No receiving On A Rail from server-provided rails
		if (event.getAchievement() == Achievement.ON_A_RAIL
				&& carts.isOnFreeCart(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

}
