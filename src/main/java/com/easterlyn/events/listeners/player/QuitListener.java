package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.discord.Discord;
import com.easterlyn.effects.Effects;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.AwayFromKeyboard;
import com.easterlyn.micromodules.FreeCart;
import com.easterlyn.micromodules.SleepVote;
import com.easterlyn.micromodules.Spectators;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Listener for PlayerQuitEvents.
 *
 * @author Jikoo
 */
public class QuitListener extends EasterlynListener {

	private final AwayFromKeyboard afk;
	private final Discord discord;
	private final Effects effects;
	private final FreeCart carts;
	private final Language lang;
	private final SleepVote sleep;
	private final Spectators spectators;
	private final Users users;

	public QuitListener(Easterlyn plugin) {
		super(plugin);
		this.afk = plugin.getModule(AwayFromKeyboard.class);
		this.discord = plugin.getModule(Discord.class);
		this.effects = plugin.getModule(Effects.class);
		this.carts = plugin.getModule(FreeCart.class);
		this.lang = plugin.getModule(Language.class);
		this.sleep = plugin.getModule(SleepVote.class);
		this.spectators = plugin.getModule(Spectators.class);
		this.users = plugin.getModule(Users.class);
	}

	/**
	 * The event handler for PlayerQuitEvents.
	 *
	 * @param event the PlayerQuitEvent
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		// Our very own custom quits!
		if (event.getQuitMessage() != null) {
			event.setQuitMessage(lang.getValue("chat.server.quit").replace("{PLAYER}", event.getPlayer().getDisplayName()));
		}

		// Clear AFK status of player
		afk.clearActivity(event.getPlayer());

		// Handle reactive Effects that use quits
		effects.handleEvent(event, event.getPlayer(), true);

		// Discord integration
		discord.postMessage(null, event.getPlayer().getDisplayName() + " logs out.", true);

		// Update vote
		sleep.removeVote(event.getPlayer().getWorld(), event.getPlayer());

		// Remove free minecart if riding one
		carts.remove(event.getPlayer());

		// Remove Spectator status
		if (spectators.isSpectator(event.getPlayer().getUniqueId())) {
			spectators.removeSpectator(event.getPlayer(), true);
		}

		// Delete team for exiting player to avoid clutter
		Users.unteam(event.getPlayer());

		final UUID uuid = event.getPlayer().getUniqueId();
		User user = users.getUser(uuid);
		user.save();

		// Inform channels that the player is no longer listening to them
		user.getListening().removeIf(channelName -> !user.removeListeningQuit(channelName));
	}

}
