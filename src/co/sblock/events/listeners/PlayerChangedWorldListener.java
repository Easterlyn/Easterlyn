package co.sblock.events.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.fx.FXManager;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;
import co.sblock.utilities.spectator.Spectators;
import co.sblock.utilities.vote.SleepVote;

/**
 * Listener for PlayerChangedWorldEvents.
 * 
 * @author Jikoo
 */
public class PlayerChangedWorldListener implements Listener {

	/**
	 * The event handler for PlayerChangedWorldEvents.
	 * 
	 * @param event the PlayerChangedWorldEvent
	 */
	@EventHandler
	public void onPlayerChangedWorlds(PlayerChangedWorldEvent event) {

		SleepVote.getInstance().updateVoteCount(event.getFrom().getName(), event.getPlayer().getName());

		// Keep spectators in spectate when changing worlds
		final UUID uuid = event.getPlayer().getUniqueId();
		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = Bukkit.getPlayer(uuid);
				if (player != null && Spectators.getInstance().isSpectator(uuid)) {
					player.setGameMode(GameMode.SPECTATOR);
				}
			}
		}.runTaskLater(Sblock.getInstance(), 2);

		OnlineUser user = Users.getGuaranteedUser(uuid).getOnlineUser();

		user.removeAllEffects();
		FXManager.getInstance().fullEffectsScan(user);
	}
}
