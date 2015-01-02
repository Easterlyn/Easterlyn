package co.sblock.events.listeners;

import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.chat.ColorDef;
import co.sblock.chat.Chat;
import co.sblock.chat.channel.ChannelType;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.users.Users;

/**
 * Listener for PlayerAsyncChatEvents.
 * 
 * @author Jikoo
 */
public class PlayerAsyncChatListener implements Listener {

	private String[] tests = new String[] {"It is certain.", "It is decidedly so.",
			"Without a doubt.", "Yes, definitely.", "You may rely on it.", "As I see, yes.",
			"Most likely.", "Outlook good.", "Yes.", "Signs point to yes.",
			"Reply hazy, try again.", "Ask again later.", "Better not tell you now.",
			"Cannot predict now.", "Concentrate and ask again.", "Don't count on it.",
			"My reply is no.", "My sources say no.", "Outlook not so good.", "Very doubtful.",
			"Testing complete. Proceeding with operation.", "A critical fault has been discovered while testing.",
			"Error: Test results contaminated.", "tset", "PONG."};

	/**
	 * The event handler for AsyncPlayerChatEvents.
	 * 
	 * We listen at monitor because we cancel the actual chat and send JSON messages instead.
	 * 
	 * @param event the AsyncPlayerChatEvent
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		MessageBuilder mb = new MessageBuilder().setSender(Users.getGuaranteedUser(event.getPlayer().getUniqueId()));
		mb.setMessage(event.getMessage());
		// Ensure message can be sent
		if (!mb.canBuild(true)) {
			event.setCancelled(true);
			return;
		}
		Message message = mb.toMessage();
		// Test
		if (message.getCleanedMessage().equalsIgnoreCase("test")) {
			event.getPlayer().sendMessage(ChatColor.RED + tests[(int) (Math.random() * 25)]);
			event.setCancelled(true);
			return;
		}
		if (message.getCleanedMessage().equalsIgnoreCase("hal") || message.getCleanedMessage().equalsIgnoreCase("dirk")) {
			event.getPlayer().sendMessage(ColorDef.HAL + "What?");
			return;
		}
		// Because we have a collection of UUIDs and not Players, nice compact lambda notation instead of a loop
		event.getRecipients().removeIf(p -> !message.getChannel().getListening().contains(p.getUniqueId()));

		event.setMessage(message.getMessage());
		event.setFormat(message.getConsoleFormat());

		final HashSet<Player> recipients = new HashSet<>(event.getRecipients());

		// Clear and manually send messages to each player so we can wrap links, etc.
		event.getRecipients().clear();
		message.send(recipients);

		// Region channels are the only ones that should be appearing in certain plugins that don't use log filters
		if (message.getChannel().getType() != ChannelType.REGION) {
			event.setCancelled(true);
		}

		// Delay reply to prevent global channels logging reply before original message
		new BukkitRunnable() {
			@Override
			public void run() {
				String msg = event.getMessage().toLowerCase();
				if (msg.startsWith("halc ") || msg.startsWith("halculate ") || msg.startsWith("evhal ") || msg.startsWith("evhaluate ")) {
					msg = msg.substring(msg.indexOf(' ')).trim();
					final Message hal = new MessageBuilder().setSender("Lil Hal")
							.setMessage(ChatColor.RED + Chat.getChat().getHalculator().evhaluate(msg))
							.setChannel(message.getChannel()).toMessage();
					hal.send(recipients);
				} else {
					Chat.getChat().getHal().handleMessage(message, recipients);
				}
			}
		}.runTaskLaterAsynchronously(Sblock.getInstance(), 2L);
	}
}
