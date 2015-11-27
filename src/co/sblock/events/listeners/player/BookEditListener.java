package co.sblock.events.listeners.player;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.chat.Chat;
import co.sblock.chat.Color;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.type.computer.EmailWriter;
import co.sblock.machines.type.computer.Programs;
import co.sblock.users.User;
import co.sblock.users.Users;
import co.sblock.utilities.RegexUtils;

/**
 * Listener for PlayerEditBookEvents.
 * 
 * @author Jikoo
 */
public class BookEditListener extends SblockListener {

	private final Chat chat;
	private final Users users;
	private final EmailWriter email;

	public BookEditListener(Sblock plugin) {
		super(plugin);
		this.chat = plugin.getModule(Chat.class);
		this.users = plugin.getModule(Users.class);
		this.email = (EmailWriter) Programs.getProgramByName("EmailWriter");
	}

	/**
	 * The EventHandler for PlayerEditBookEvents.
	 * 
	 * @param event the PlayerEditBookEvent.
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onBookedit(PlayerEditBookEvent event) {
		// Muted players can't write books.
		if (chat.testForMute(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}

		if (!event.isSigning()) {
			return;
		}

		final BookMeta sendable = event.getNewBookMeta();
		if (!email.isLetterMeta(sendable)) {
			return;
		}

		if (sendable.getPageCount() == 0) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(Color.BAD + "You can't send empty mail!");
		}

		String title = sendable.getTitle();
		if (title == null || title.length() < 3 || title.length() > 16
				|| RegexUtils.stripNonAlphanumerics(title).length() < title.length()) {
			sendable.setTitle(null);
			event.setSigning(false);
			event.getPlayer().sendMessage(Color.BAD + "Invalid name! The title of the letter must be the full name of the recipient.");
		}

		event.setCancelled(true);
		event.getPlayer().getInventory().setItem(event.getSlot(), null);
		final UUID sender = event.getPlayer().getUniqueId();

		// future make this less horribly messy and indented
		new BukkitRunnable() {
			@Override
			public void run() {
				@SuppressWarnings("deprecation")
				final OfflinePlayer player = Bukkit.getOfflinePlayer(title);
				new BukkitRunnable() {
					@Override
					public void run() {
						if (!player.hasPlayedBefore()) {
							Player senderPlayer = Bukkit.getPlayer(sender);
							if (senderPlayer != null) {
								senderPlayer.sendMessage(Color.BAD
										+ "Invalid name! The title of the letter must be the full name of the recipient.");
							}
							ItemStack letter = new ItemStack(Material.BOOK_AND_QUILL);
							sendable.setTitle(null);
							letter.setItemMeta(sendable);
							if (senderPlayer.getInventory().getItem(event.getSlot()) != null) {
								senderPlayer.getWorld().dropItem(senderPlayer.getLocation(),
										senderPlayer.getInventory().getItem(event.getSlot())).setPickupDelay(0);
							}
							senderPlayer.getInventory().setItem(event.getSlot(), letter);
							return;
						}
						User user = users.getUser(player.getUniqueId());
						List<ItemStack> items = user.getMailItems();
						if (items.size() > 44) {
							Player senderPlayer = Bukkit.getPlayer(sender);
							if (senderPlayer != null) {
								senderPlayer.sendMessage(Color.BAD_PLAYER + user.getPlayerName() + Color.BAD
										+ "'s inbox is full! They'll need to collect their mail.");
							}
							return;
						}
						String title = sendable.getPage(0).split("\n")[0];
						if (title.length() > 20) {
							title = title.substring(0,  20);
						}
						sendable.setTitle(title);
						ItemStack mail = new ItemStack(Material.WRITTEN_BOOK);
						mail.setItemMeta(sendable);
						items.add(mail);
					}
				}.runTask(getPlugin());
			}
		}.runTaskAsynchronously(getPlugin());
	}

}
