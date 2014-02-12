package co.sblock.Sblock.Events.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import co.sblock.Sblock.Chat.ChatUserManager;
import co.sblock.Sblock.Events.SblockEvents;
import co.sblock.Sblock.SblockEffects.EffectManager;
import co.sblock.Sblock.UserData.Region;
import co.sblock.Sblock.UserData.SblockUser;

/**
 * Listener for PlayerChangedWorldEvents.
 * 
 * @author Jikoo
 */
public class PlayerChangedWorldListener implements Listener {

	/**
	 * The event handler for PlayerChangedWorldEvents.
	 * 
	 * @param event the PlayerChangedWorldEvent
	 */
	@EventHandler
	public void onPlayerChangedWorlds(PlayerChangedWorldEvent event) {
		try {
			ChatUserManager.getUserManager().getUser(event.getPlayer().getName())
					.updateCurrentRegion(Region.getLocationRegion(event.getPlayer().getLocation()));
		} catch (NullPointerException e) {
			SblockEvents.getEvents().getLogger().fine(
					"Error updating region, user is likely entering same overall region.");
		}

		SblockUser u = SblockUser.getUser(event.getPlayer().getName());
		u.setAllPassiveEffects(EffectManager.passiveScan(event.getPlayer()));
		EffectManager.applyPassiveEffects(u);
	}
}
