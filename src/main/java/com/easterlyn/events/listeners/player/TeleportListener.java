package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.Protections;
import com.easterlyn.micromodules.Spectators;
import com.easterlyn.users.Users;
import java.util.UUID;
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
	private final Protections protections;
	private final Spectators spectators;
	private final Users users;

	public TeleportListener(Easterlyn plugin) {
		super(plugin);
		this.lang = plugin.getModule(Language.class);
		this.protections = plugin.getModule(Protections.class);
		this.spectators = plugin.getModule(Spectators.class);
		this.users = plugin.getModule(Users.class);
	}

	/**
	 * The event handler for PlayerTeleportEvents.
	 * 
	 * @param event the PlayerTeleportEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (event.getCause() != TeleportCause.SPECTATE || event.getTo() == null || event.getTo().getWorld() == null
				|| !spectators.isSpectator(event.getPlayer().getUniqueId())) {
			return;
		}

		if (protections.getHooks().stream().anyMatch(protectionHook -> !protectionHook.canUseButtonsAt(event.getPlayer(), event.getTo()))) {
			event.getPlayer().sendMessage(lang.getValue("spectators.disallowedArea"));
		}

		double closestDistance = Double.MAX_VALUE;
		Player closestPlayer = null;
		for (Player player : event.getTo().getWorld().getPlayers()) {
			double distance = player.getLocation().distanceSquared(event.getTo());
			if (distance < closestDistance) {
				closestDistance = distance;
				if (player.getGameMode() == GameMode.SPECTATOR) {
					if (player.getSpectatorTarget() != null
							&& player.getSpectatorTarget() instanceof Player) {
						closestPlayer = (Player) player.getSpectatorTarget();
					} else {
						continue;
					}
				} else {
					closestPlayer = player;
				}
			}
			if (distance == 0) {
				break;
			}
		}

		if (closestPlayer == null) {
			if (!event.getPlayer().hasPermission("easterlyn.spectators.unrestricted")) {
				// Target is in spectate, no spectating.
				event.setCancelled(true);
			}
			return;
		}

		if (users.getUser(closestPlayer.getUniqueId()).getSpectatable()) {
			// Force location to player's actual location in case we ignored a spectator.
			event.setTo(closestPlayer.getLocation());
			return;
		}

		if (event.getPlayer().hasPermission("easterlyn.spectators.unrestricted")) {
			event.getPlayer().sendMessage(lang.getValue("spectators.ignoreDisallowed")
					.replace("{PLAYER}", closestPlayer.getDisplayName()));
			return;
		}

		event.setCancelled(true);
		event.getPlayer().sendMessage(lang.getValue("spectators.disallowed")
				.replace("{PLAYER}", closestPlayer.getDisplayName()));
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

		if (!spectators.isSpectator(event.getPlayer().getUniqueId())) {
			switch (event.getCause()) {
			case PLUGIN: // Temporarily allow /back for any plugin-induced TP.
			case COMMAND:
				// The back command is only for commands.
				users.getUser(event.getPlayer().getUniqueId()).setBackLocation(event.getFrom());
				break;
			default:
				break;
			}
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
