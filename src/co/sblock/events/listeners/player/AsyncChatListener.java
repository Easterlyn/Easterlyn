package co.sblock.events.listeners.player;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.sblock.chat.Chat;
import co.sblock.chat.ColorDef;
import co.sblock.chat.channel.ChannelType;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.users.Users;
import co.sblock.utilities.player.DummyPlayer;

/**
 * Listener for PlayerAsyncChatEvents.
 * 
 * @author Jikoo
 */
public class AsyncChatListener implements Listener {

	private final String[] tests = new String[] {"It is certain.", "It is decidedly so.",
			"Without a doubt.", "Yes, definitely.", "You may rely on it.", "As I see, yes.",
			"Most likely.", "Outlook good.", "Yes.", "Signs point to yes.",
			"Reply hazy, try again.", "Ask again later.", "Better not tell you now.",
			"Cannot predict now.", "Concentrate and ask again.", "Don't count on it.",
			"My reply is no.", "My sources say no.", "Outlook not so good.", "Very doubtful.",
			"Testing complete. Proceeding with operation.", "A critical fault has been discovered while testing.",
			"Error: Test results contaminated.", "tset", "PONG."};

	/**
	 * Because we send JSON messages, we actually have to remove all recipients from the event and
	 * manually send each one the message.
	 * 
	 * To prevent IRC and other chat loggers from picking up chat sent to non-regional channels,
	 * non-regional chat must be cancelled.
	 * 
	 * @param event the SblockAsyncChatEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
		Message message;
		if (event instanceof SblockAsyncChatEvent) {
			message = ((SblockAsyncChatEvent) event).getSblockMessage();
		} else {
			MessageBuilder mb = new MessageBuilder().setSender(Users.getGuaranteedUser(event.getPlayer().getUniqueId()))
					.setMessage(event.getMessage());
			// Ensure message can be sent
			if (!mb.canBuild(true) || !mb.isSenderInChannel(true)) {
				return;
			}
			message = mb.toMessage();

			event.getRecipients().removeIf(p -> !message.getChannel().getListening().contains(p.getUniqueId()));
		}

		String cleaned = message.getCleanedMessage();

		if (cleaned.equalsIgnoreCase("test")) {
			event.getPlayer().sendMessage(ChatColor.RED + tests[(int) (Math.random() * 25)]);
		}
		if (Chat.getChat().getHal().isOnlyTrigger(cleaned)) {
			event.getPlayer().sendMessage(ColorDef.HAL + "What?");
			event.setCancelled(true);
			return;
		}
		if (message.getChannel().getType() == ChannelType.REGION) {
			if (rpMatch(cleaned)) {
				event.getPlayer().sendMessage(ColorDef.HAL + "RP is not allowed in the main chat. Join #rp or #fanrp using /focus!");
				event.setCancelled(true);
				return;
			}

			// Prevent IRC picking up soft muted messages
			if (event.getRecipients().size() < message.getChannel().getListening().size()) {
				event.setFormat("[SoftMute] " + event.getFormat());
			}
		}

		// Region channels are the only ones that should be appearing in certain plugins
		if (message.getChannel().getType() != ChannelType.REGION) {
			event.setCancelled(true);
		}

		// Manually send messages to each player so we can wrap links, etc.
		message.send(event.getRecipients(), !(event instanceof SblockAsyncChatEvent));

		// Dummy player should not trigger Hal; he may become one.
		if (event.getPlayer() instanceof DummyPlayer) {
			return;
		}

		// Handle Hal functions
		String msg = ChatColor.stripColor(event.getMessage().toLowerCase());
		if (msg.startsWith("halc ") || msg.startsWith("halculate ") || msg.startsWith("evhal ") || msg.startsWith("evhaluate ")) {
			msg = msg.substring(msg.indexOf(' ')).trim();
			final Message hal = new MessageBuilder().setSender(ChatColor.DARK_RED + "Lil Hal")
					.setMessage(ChatColor.RED + Chat.getChat().getHalculator().evhaluate(msg))
					.setChannel(message.getChannel()).toMessage();
			if (msg.length() > 30) {
				event.getPlayer().sendMessage(ColorDef.HAL + "Your equation is a bit long for public chat. Please use /halc to reduce spam.");
			}
			hal.send(event.getRecipients());
		} else {
			Chat.getChat().getHal().handleMessage(message, event.getRecipients());
		}
	}

	public boolean rpMatch(String message) {
		if (message.matches("([hH][oO][nN][kK] ?)+")) {
			return true;
		}
		return false;
	}
}
