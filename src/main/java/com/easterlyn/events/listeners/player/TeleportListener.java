package com.easterlyn.events.listeners.player;

import java.util.UUID;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.users.Users;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener for PlayerTeleportEvents.
 * 
 * @author Jikoo
 */
public class TeleportListener extends EasterlynListener {

	private final Language lang;
	private final Users users;

	public TeleportListener(Easterlyn plugin) {
		super(plugin);
		this.lang = plugin.getModule(Language.class);
		this.users = plugin.getModule(Users.class);
	}

	/**
	 * The event handler for PlayerTeleportEvents.
	 * 
	 * @param event the PlayerTeleportEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (event.getCause() != TeleportCause.SPECTATE) {
			return;
		}
		for (Player player : event.getTo().getWorld().getPlayers()) {
			if (!player.getLocation().equals(event.getTo())) {
				continue;
			}
			if (player.getGameMode() == GameMode.SPECTATOR) {
				if (player.getSpectatorTarget() != null
						&& player.getSpectatorTarget() instanceof Player) {
					player = (Player) player.getSpectatorTarget();
				} else {
					continue;
				}
			}
			if (users.getUser(player.getUniqueId()).getSpectatable()) {
				return;
			}
			if (event.getPlayer().hasPermission("easterlyn.spectators.unrestricted")) {
				event.getPlayer().sendMessage(lang.getValue("spectators.ignoreDisallowed")
						.replace("{PLAYER}", player.getDisplayName()));
				return;
			}
			event.setCancelled(true);
			event.getPlayer().sendMessage(lang.getValue("spectators.disallowed")
					.replace("{PLAYER}", player.getDisplayName()));
			return;
		}
	}

	/**
	 * The event handler for PlayerTeleportEvents.
	 * <p>
	 * This method is for events that are guaranteed to be completed.
	 * 
	 * @param event the PlayerTeleportEvent
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleportHasOccurred(final PlayerTeleportEvent event) {
		// People keep doing stupid stuff like /home while falling from spawn
		event.getPlayer().setFallDistance(0);

		switch (event.getCause()) {
		case COMMAND:
			// The back command is only for commands.
			users.getUser(event.getPlayer().getUniqueId()).setBackLocation(event.getFrom());
			break;
		default:
			break;
		}

		if (event.getTo().getWorld().equals(event.getFrom().getWorld())) {
			return;
		}

		final UUID uuid = event.getPlayer().getUniqueId();

		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					// Player has logged out.
					return;
				}
				users.getUser(uuid).updateFlight();
			}
		}.runTask(getPlugin());
	}

}
