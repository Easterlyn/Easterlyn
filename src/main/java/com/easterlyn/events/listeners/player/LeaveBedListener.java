package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.SleepVote;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedLeaveEvent;

/**
 * Listener for updating vote count on bed entry.
 * 
 * @author Jikoo
 */
public class LeaveBedListener extends EasterlynListener {

	private final SleepVote sleep;

	public LeaveBedListener(Easterlyn plugin) {
		super(plugin);
		this.sleep = plugin.getModule(SleepVote.class);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBedLeave(PlayerBedLeaveEvent event) {
		sleep.removeVote(event.getPlayer().getWorld(), event.getPlayer());
	}

}
