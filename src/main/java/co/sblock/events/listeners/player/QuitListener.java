package co.sblock.events.listeners.player;

import java.util.Iterator;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.discord.Discord;
import co.sblock.effects.Effects;
import co.sblock.events.listeners.SblockListener;
import co.sblock.micromodules.AwayFromKeyboard;
import co.sblock.micromodules.DreamTeleport;
import co.sblock.micromodules.FreeCart;
import co.sblock.micromodules.Godule;
import co.sblock.micromodules.SleepVote;
import co.sblock.micromodules.Spectators;
import co.sblock.users.User;
import co.sblock.users.Users;
import co.sblock.utilities.InventoryManager;

/**
 * Listener for PlayerQuitEvents.
 * 
 * @author Jikoo
 */
public class QuitListener extends SblockListener {

	private final AwayFromKeyboard afk;
	private final Discord discord;
	private final DreamTeleport dream;
	private final Effects effects;
	private final FreeCart carts;
	private final Godule godule;
	private final SleepVote sleep;
	private final Spectators spectators;
	private final Users users;

	public QuitListener(Sblock plugin) {
		super(plugin);
		this.afk = plugin.getModule(AwayFromKeyboard.class);
		this.discord = plugin.getModule(Discord.class);
		this.dream = plugin.getModule(DreamTeleport.class);
		this.effects = plugin.getModule(Effects.class);
		this.carts = plugin.getModule(FreeCart.class);
		this.godule = plugin.getModule(Godule.class);
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
			event.setQuitMessage(Color.BAD_PLAYER + event.getPlayer().getDisplayName() + Color.BAD + " ollies outie");
		}

		// Clear AFK status of player
		afk.clearActivity(event.getPlayer());

		// Handle reactive Effects that use quits
		effects.handleEvent(event, event.getPlayer(), true);

		// Discord integration
		discord.postMessage(discord.getBotName(), event.getPlayer().getDisplayName() + " logs out.", true);

		// Update vote
		sleep.updateVoteCount(event.getPlayer().getWorld().getName(), event.getPlayer().getName());

		// Remove free minecart if riding one
		carts.remove(event.getPlayer());

		// Remove Spectator status
		if (spectators.isSpectator(event.getPlayer().getUniqueId())) {
			spectators.removeSpectator(event.getPlayer(), true);
		}

		// Stop scheduled sleep teleport
		if (dream.getSleepTasks().containsKey(event.getPlayer().getName())) {
			dream.getSleepTasks().remove(event.getPlayer().getName()).cancel();
		}

		// Restore inventory if still preserved
		InventoryManager.restoreInventory(event.getPlayer());

		// Delete team for exiting player to avoid clutter
		Users.unteam(event.getPlayer());

		final UUID uuid = event.getPlayer().getUniqueId();
		User user = users.getUser(uuid);
		user.save();

		// Disable "god" effect, if any
		if (event.getPlayer().hasPermission("sblock.god")) {
			godule.disable(user.getUserAspect());
		}

		// Inform channels that the player is no longer listening to them
		for (Iterator<String> iterator = user.getListening().iterator(); iterator.hasNext();) {
			if (!user.removeListeningQuit(iterator.next())) {
				iterator.remove();
			}
		}
	}

}
