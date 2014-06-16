package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import co.sblock.effects.EffectManager;
import co.sblock.users.Region;
import co.sblock.users.User;
import co.sblock.utilities.vote.SleepVote;

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

		SleepVote.getInstance().updateVoteCount(event.getFrom().getName(), event.getPlayer().getName());

		User user = User.getUser(event.getPlayer().getUniqueId());

		// Update region
		user.updateCurrentRegion(Region.getLocationRegion(event.getPlayer().getLocation()));

		// Scan for and apply passive effects
		user.setAllPassiveEffects(EffectManager.passiveScan(event.getPlayer()));
		EffectManager.applyPassiveEffects(user);
	}
}
