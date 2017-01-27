package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.Effects;
import com.easterlyn.events.listeners.SblockListener;
import com.easterlyn.micromodules.SleepVote;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 * Listener for PlayerChangedWorldEvents.
 * 
 * @author Jikoo
 */
public class ChangedWorldListener extends SblockListener {

	private final Effects effects;
	private final SleepVote sleep;

	public ChangedWorldListener(Easterlyn plugin) {
		super(plugin);
		this.effects = plugin.getModule(Effects.class);
		this.sleep = plugin.getModule(SleepVote.class);
	}

	/**
	 * The event handler for PlayerChangedWorldEvents.
	 * 
	 * @param event the PlayerChangedWorldEvent
	 */
	@EventHandler
	public void onPlayerChangedWorlds(PlayerChangedWorldEvent event) {

		sleep.updateVoteCount(event.getFrom().getName(), event.getPlayer().getName());

		effects.applyAllEffects(event.getPlayer());
	}

}
