package co.sblock.Sblock.Events.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import co.sblock.Sblock.Database.SblockData;
import co.sblock.Sblock.SblockEffects.EffectManager;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;
import co.sblock.Sblock.Utilities.Vote.SleepVote;

/**
 * Listener for PlayerJoinEvents.
 * 
 * @author Jikoo
 */
public class PlayerJoinListener implements Listener {

	/**
	 * The event handler for PlayerJoinEvents.
	 * 
	 * @param event the PlayerJoinEvent
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		SblockData.getDB().loadUserData(event.getPlayer().getName());

		SblockUser u = UserManager.getUserManager().addUser(event.getPlayer().getName());
		u.setAllPassiveEffects(EffectManager.passiveScan(event.getPlayer()));
		EffectManager.applyPassiveEffects(u);

		SleepVote.getInstance().updateVoteCount(event.getPlayer().getWorld());
	}
}
