package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import co.sblock.Sblock;
import co.sblock.effects.Effects;
import co.sblock.events.listeners.SblockListener;
import co.sblock.micromodules.SleepVote;

/**
 * Listener for PlayerChangedWorldEvents.
 * 
 * @author Jikoo
 */
public class ChangedWorldListener extends SblockListener {

	private final Effects effects;
	private final SleepVote sleep;

	public ChangedWorldListener(Sblock plugin) {
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
