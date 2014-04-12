package co.sblock.Sblock.Events.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import co.sblock.Sblock.SblockEffects.EffectManager;
import co.sblock.Sblock.UserData.Region;
import co.sblock.Sblock.UserData.User;
import co.sblock.Sblock.Utilities.Vote.SleepVote;

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

		// adam verify that this returns the correct number and not 1 lower
		SleepVote.getInstance().updateVoteCount(event.getFrom().getName(), event.getPlayer().getName());

		User user = User.getUser(event.getPlayer().getUniqueId());

		// Update region
		user.updateCurrentRegion(Region.getLocationRegion(event.getPlayer().getLocation()));

		// Scan for and apply passive effects
		user.setAllPassiveEffects(EffectManager.passiveScan(event.getPlayer()));
		EffectManager.applyPassiveEffects(user);
	}
}
