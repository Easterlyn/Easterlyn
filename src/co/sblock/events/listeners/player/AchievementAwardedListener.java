package co.sblock.events.listeners.player;

import org.bukkit.Achievement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;

import co.sblock.utilities.minecarts.FreeCart;

/**
 * Listener for PlayerAchievementAwardedEvents.
 * 
 * @author Jikoo
 */
public class AchievementAwardedListener implements Listener {

	@EventHandler
	public void onPlayerAchievementAwarded(PlayerAchievementAwardedEvent event) {
		// No receiving On A Rail from server-provided rails
		if (event.getAchievement() == Achievement.ON_A_RAIL
				&& FreeCart.getInstance().isOnFreeCart(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
}
