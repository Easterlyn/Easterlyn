package co.sblock.events.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import co.sblock.data.SblockData;
import co.sblock.effects.Cooldowns;
import co.sblock.events.SblockEvents;
import co.sblock.users.ProgressionState;
import co.sblock.users.User;
import co.sblock.utilities.inventory.InventoryManager;
import co.sblock.utilities.progression.Entry;
import co.sblock.utilities.spectator.Spectators;
import co.sblock.utilities.vote.SleepVote;

/**
 * Listener for PlayerQuitEvents.
 * 
 * @author Jikoo
 */
public class PlayerQuitListener implements Listener {

	/**
	 * The event handler for PlayerQuitEvents.
	 * 
	 * @param event the PlayerQuitEvent
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		// Our very own custom quits!
		event.setQuitMessage(ChatColor.AQUA + event.getPlayer().getDisplayName() + ChatColor.RED + " ollies outie");

		// Update vote
		SleepVote.getInstance().updateVoteCount(event.getPlayer().getWorld().getName(), event.getPlayer().getName());

		// Remove Spectator status
		if (Spectators.getSpectators().isSpectator(event.getPlayer().getUniqueId())) {
			Spectators.getSpectators().removeSpectator(event.getPlayer());
		}

		// Stop scheduled sleep teleport
		if (SblockEvents.getEvents().tasks.containsKey(event.getPlayer().getName())) {
			Bukkit.getScheduler().cancelTask(SblockEvents.getEvents().tasks.remove(event.getPlayer().getName()));
		}

		// Remove from list awaiting Captchadex inventory opening
		SblockEvents.getEvents().openingCaptchadex.remove(event.getPlayer().getName());

		// Clean up any expired cooldown entries for the player
		Cooldowns.cleanup(event.getPlayer().getName());

		// Remove Server status
		User user = User.getUser(event.getPlayer().getUniqueId());
		if (user != null && user.isServer()) {
			user.stopServerMode();
		}

		// Fail Entry if in progress
		if (user != null && user.getProgression() == ProgressionState.NONE) {
			Entry.getEntry().fail(user);
		}

		// Restore inventory if still preserved
		InventoryManager.restoreInventory(event.getPlayer());

		// Save user data
		SblockData.getDB().saveUserData(event.getPlayer().getUniqueId());

		// Inform channels that the player is no longer listening to them
		for (String s : user.getListening()) {
			user.removeListeningQuit(s);
		}
	}
}
