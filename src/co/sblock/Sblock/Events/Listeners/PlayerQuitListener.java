package co.sblock.Sblock.Events.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.ChatUserManager;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Database.SblockData;
import co.sblock.Sblock.Events.SblockEvents;
import co.sblock.Sblock.SblockEffects.Cooldowns;
import co.sblock.Sblock.UserData.SblockUser;
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

		// Update vote
		SleepVote.getInstance().updateVoteCount(event.getPlayer().getWorld());

		// Remove Spectator status
		if (Spectators.getSpectators().isSpectator(event.getPlayer().getName())) {
			Spectators.getSpectators().removeSpectator(event.getPlayer());
		}

		// Stop scheduled sleep teleport
		if (SblockEvents.getEvents().tasks.containsKey(event.getPlayer().getName())) {
			Bukkit.getScheduler().cancelTask(SblockEvents.getEvents().tasks.remove(event.getPlayer().getName()));
		}

		// Clean up any expired cooldown entries for the player
		Cooldowns.cleanup(event.getPlayer().getName());

		// Remove Server status
		SblockUser sUser = SblockUser.getUser(event.getPlayer().getName());
		if (sUser != null && sUser.isServer()) {
			sUser.stopServerMode();
		}

		// Restore inventory if still preserved
		InventoryManager.restoreInventory(event.getPlayer());

		// Save data and inform channels that the player is no longer listening to them
		ChatUser cUser = ChatUserManager.getUserManager().getUser(event.getPlayer().getName());
		SblockData.getDB().saveUserData(event.getPlayer().getName());
		if (cUser == null) {
			// Sending a message to a channel will remove invalid players.
			return;
		}
		for (String s : cUser.getListening()) {
			cUser.removeListeningQuit(s);
		}
		try {
			Channel regionC = ChannelManager.getChannelManager().getChannel("#" + cUser.getCurrentRegion().toString());
			cUser.removeListeningQuit(regionC.getName());
		} catch (NullPointerException e) {
			SblockEvents.getEvents().getLogger().warning("User's region channel was invalid!");
		}
	}
}
