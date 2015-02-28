package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import co.sblock.effects.FXManager;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;
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

		OnlineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId()).getOnlineUser();

		user.removeAllEffects();
		FXManager.getInstance().fullEffectsScan(user);
	}
}
