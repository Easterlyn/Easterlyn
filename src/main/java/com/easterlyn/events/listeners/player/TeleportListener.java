package com.easterlyn.events.listeners.player;

import com.easterlyn.micromodules.Protections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.Spectators;
import com.easterlyn.users.Users;

import java.util.stream.Collectors;
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
		if (event.getCause() != TeleportCause.SPECTATE || event.getTo() == null || event.getTo().getWorld() == null) {
			return;
		}

		if (protections.getHooks().stream().anyMatch(protectionHook -> !protectionHook.canUseButtonsAt(event.getPlayer(), event.getTo()))) {
			event.getPlayer().sendMessage(lang.getValue("spectators.disallowedArea"));
		}

		List<Player> worldPlayers = event.getTo().getWorld().getPlayers();
		for (Player player : worldPlayers) {
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

			checkSpectate(event, player);
			return;
		}

		TreeMap<Double, Player> distanceToPlayers = worldPlayers.stream().collect(Collectors.toMap(player -> player.getLocation().distanceSquared(event.getTo()), player -> player, (o, o2) -> o, TreeMap::new));
		Map.Entry<Double, Player> closestPlayer = distanceToPlayers.firstEntry();
		if (closestPlayer != null) {
			checkSpectate(event, closestPlayer.getValue());
		}
	}

	private void checkSpectate(PlayerTeleportEvent event, Player teleportTarget) {
		if (users.getUser(teleportTarget.getUniqueId()).getSpectatable()) {
			return;
		}

		if (event.getPlayer().hasPermission("easterlyn.spectators.unrestricted")) {
			event.getPlayer().sendMessage(lang.getValue("spectators.ignoreDisallowed")
					.replace("{PLAYER}", teleportTarget.getDisplayName()));
			return;
		}

		event.setCancelled(true);
		event.getPlayer().sendMessage(lang.getValue("spectators.disallowed")
				.replace("{PLAYER}", teleportTarget.getDisplayName()));
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
