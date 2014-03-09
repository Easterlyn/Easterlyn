package co.sblock.Sblock.Events.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.ChatUserManager;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Database.SblockData;
import co.sblock.Sblock.Events.SblockEvents;
import co.sblock.Sblock.SblockEffects.Cooldowns;
import co.sblock.Sblock.Utilities.Inventory.InventoryManager;
import co.sblock.Sblock.Utilities.Spectator.Spectators;

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
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (Spectators.getSpectators().isSpectator(event.getPlayer().getName())) {
			Spectators.getSpectators().removeSpectator(event.getPlayer());
		}
		InventoryManager.restoreInventory(event.getPlayer());
		ChatUser u = ChatUserManager.getUserManager().getUser(event.getPlayer().getName());
		if (u == null) {
			return; // We don't want to make another db call just to announce quit.
		}
		if (SblockEvents.getEvents().tasks.containsKey(u.getPlayerName())) {
			Bukkit.getScheduler().cancelTask(SblockEvents.getEvents().tasks.remove(u.getPlayerName()));
		}
		for (String s : u.getListening()) {
			u.removeListeningQuit(s);
		}
		try {
			Channel regionC = ChannelManager.getChannelManager().getChannel("#" + u.getCurrentRegion().toString());
			u.removeListening(regionC.getName());
		} catch (NullPointerException e) {
			SblockEvents.getEvents().getLogger().warning("User's region channel was invalid!");
		}
		SblockData.getDB().saveUserData(event.getPlayer().getName());
		Cooldowns.cleanup(event.getPlayer().getName());
	}
}
