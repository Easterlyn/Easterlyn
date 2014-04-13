package co.sblock.Sblock.Events.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import co.sblock.Sblock.Database.SblockData;
import co.sblock.Sblock.Events.SblockEvents;
import co.sblock.Sblock.SblockEffects.Cooldowns;
import co.sblock.Sblock.UserData.ChatData;
import co.sblock.Sblock.UserData.User;
import co.sblock.Sblock.Utilities.Inventory.InventoryManager;
import co.sblock.Sblock.Utilities.Spectator.Spectators;
import co.sblock.Sblock.Utilities.Vote.SleepVote;

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

		// Restore inventory if still preserved
		InventoryManager.restoreInventory(event.getPlayer());

		// Save user data
		SblockData.getDB().saveUserData(event.getPlayer().getUniqueId());

		// Inform channels that the player is no longer listening to them
		for (String s : ChatData.getListening(user)) {
			ChatData.removeListeningQuit(user, s);
		}
	}
}
