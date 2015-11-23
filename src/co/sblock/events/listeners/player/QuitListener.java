package co.sblock.events.listeners.player;

import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.discord.Discord;
import co.sblock.effects.Effects;
import co.sblock.events.Events;
import co.sblock.events.listeners.SblockListener;
import co.sblock.micromodules.FreeCart;
import co.sblock.micromodules.Godule;
import co.sblock.micromodules.SleepVote;
import co.sblock.micromodules.Spectators;
import co.sblock.progression.Entry;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.ProgressionState;
import co.sblock.users.Users;
import co.sblock.utilities.InventoryManager;

/**
 * Listener for PlayerQuitEvents.
 * 
 * @author Jikoo
 */
public class QuitListener extends SblockListener {

	private final Discord discord;
	private final Effects effects;
	private final Entry entry;
	private final Events events;
	private final FreeCart carts;
	private final SleepVote sleep;
	private final Spectators spectators;

	public QuitListener(Sblock plugin) {
		super(plugin);
		discord = plugin.getModule(Discord.class);
		effects = plugin.getModule(Effects.class);
		entry = plugin.getModule(Entry.class);
		events = plugin.getModule(Events.class);
		carts = plugin.getModule(FreeCart.class);
		sleep = plugin.getModule(SleepVote.class);
		spectators = plugin.getModule(Spectators.class);
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
			event.setQuitMessage(Color.BAD_PLAYER + event.getPlayer().getDisplayName() + Color.BAD + " ollies outie");
		}

		// Handle reactive Effects that use quits
		effects.handleEvent(event, event.getPlayer(), true);

		// Discord integration
		discord.postMessage(event.getPlayer().getName(),
				event.getPlayer().getName() + " logs out.", true);

		// Update vote
		sleep.updateVoteCount(event.getPlayer().getWorld().getName(), event.getPlayer().getName());

		// Remove free minecart if riding one
		carts.remove(event.getPlayer());

		// Remove Spectator status
		if (spectators.isSpectator(event.getPlayer().getUniqueId())) {
			spectators.removeSpectator(event.getPlayer());
		}

		// Stop scheduled sleep teleport
		if (events.getSleepTasks().containsKey(event.getPlayer().getName())) {
			events.getSleepTasks().remove(event.getPlayer().getName()).cancel();
		}

		// Restore inventory if still preserved
		InventoryManager.restoreInventory(event.getPlayer());

		// Delete team for exiting player to avoid clutter
		Users.unteam(event.getPlayer());

		final UUID uuid = event.getPlayer().getUniqueId();
		OfflineUser user = Users.getGuaranteedUser(getPlugin(), uuid);
		user.save();

		// Disable "god" effect, if any
		if (event.getPlayer().hasPermission("sblock.god")) {
			Godule.getInstance().disable(user.getUserAspect());
		}

		// Remove Server status
		if (user instanceof OnlineUser && ((OnlineUser) user).isServer()) {
			((OnlineUser) user).stopServerMode();
		}

		// Complete success sans animation if player logs out
		if (user.getProgression() == ProgressionState.ENTRY_COMPLETING) {
			entry.finalizeSuccess(event.getPlayer(), user);
		}

		// Fail Entry if in progress
		if (user.getProgression() == ProgressionState.ENTRY_UNDERWAY) {
			entry.fail(user);
		}

		// Inform channels that the player is no longer listening to them
		for (Iterator<String> iterator = user.getListening().iterator(); iterator.hasNext();) {
			if (!user.removeListeningQuit(iterator.next())) {
				iterator.remove();
			}
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				// A player chatting during an unload will cause the recipient's OnlineUser to be re-created.
				// To combat this, we attempt to unload the user after they should no longer be online.
				if (!Bukkit.getOfflinePlayer(uuid).isOnline()) {
					Users.unloadUser(uuid);
				}
			}
		}.runTask(getPlugin());
	}
}
