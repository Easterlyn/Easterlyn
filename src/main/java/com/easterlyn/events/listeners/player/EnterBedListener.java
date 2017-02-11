package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.SleepVote;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedEnterEvent;

/**
 * Listener for updating vote count on bed entry.
 * 
 * @author Jikoo
 */
public class EnterBedListener extends EasterlynListener {

	private final SleepVote sleep;

	public EnterBedListener(Easterlyn plugin) {
		super(plugin);
		this.sleep = plugin.getModule(SleepVote.class);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		sleep.addVote(event.getPlayer().getWorld(), event.getPlayer());
	}

}
