package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import co.sblock.effects.Effects;
import co.sblock.micromodules.SleepVote;

/**
 * Listener for PlayerChangedWorldEvents.
 * 
 * @author Jikoo
 */
public class ChangedWorldListener implements Listener {

	/**
	 * The event handler for PlayerChangedWorldEvents.
	 * 
	 * @param event the PlayerChangedWorldEvent
	 */
	@EventHandler
	public void onPlayerChangedWorlds(PlayerChangedWorldEvent event) {

		SleepVote.getInstance().updateVoteCount(event.getFrom().getName(), event.getPlayer().getName());

		Effects.getInstance().applyAllEffects(event.getPlayer());
	}
}
