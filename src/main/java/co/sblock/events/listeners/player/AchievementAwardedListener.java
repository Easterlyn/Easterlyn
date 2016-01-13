package co.sblock.events.listeners.player;

import org.bukkit.Achievement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.micromodules.FreeCart;

/**
 * Listener for PlayerAchievementAwardedEvents.
 * 
 * @author Jikoo
 */
public class AchievementAwardedListener extends SblockListener {

	private final FreeCart carts;

	public AchievementAwardedListener(Sblock plugin) {
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
