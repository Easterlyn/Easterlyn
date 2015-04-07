package co.sblock.events.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.sblock.chat.Chat;
import co.sblock.chat.ColorDef;
import co.sblock.chat.channel.ChannelType;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.users.Users;

/**
 * Listener for PlayerAsyncChatEvents.
 * 
 * @author Jikoo
 */
public class PlayerAsyncChatListener implements Listener {

	private final String[] tests = new String[] {"It is certain.", "It is decidedly so.",
			"Without a doubt.", "Yes, definitely.", "You may rely on it.", "As I see, yes.",
			"Most likely.", "Outlook good.", "Yes.", "Signs point to yes.",
			"Reply hazy, try again.", "Ask again later.", "Better not tell you now.",
			"Cannot predict now.", "Concentrate and ask again.", "Don't count on it.",
			"My reply is no.", "My sources say no.", "Outlook not so good.", "Very doubtful.",
			"Testing complete. Proceeding with operation.", "A critical fault has been discovered while testing.",
			"Error: Test results contaminated.", "tset", "PONG."};

	/**
	 * The first event handler for AsyncPlayerChatEvents.
	 * 
	 * LOW priority allows us to modify the event before chat loggers (IRC, etc.) pick it up.
	 * 
	 * @param event the AsyncPlayerChatEvent
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerChatLow(final AsyncPlayerChatEvent event) {
		if (event instanceof SblockAsyncChatEvent) {
			return;
		}
		System.out.println(event.getClass().getName());
		event.setCancelled(true);
		boolean thirdPerson = event.getMessage().startsWith("@#>me");
		MessageBuilder mb = new MessageBuilder().setSender(Users.getGuaranteedUser(event.getPlayer().getUniqueId()));
		if (thirdPerson) {
			event.setMessage(event.getMessage().substring(5));
			mb.setThirdPerson(true);
		}
		mb.setMessage(event.getMessage());
		// Ensure message can be sent
		if (!mb.canBuild(true) || !mb.isSenderInChannel(true)) {
			return;
		}
		Message msg = mb.toMessage();
		// Because we have a collection of UUIDs and not Players, nice compact lambda notation instead of a loop
		event.getRecipients().removeIf(p -> !msg.getChannel().getListening().contains(p.getUniqueId()));

		SblockAsyncChatEvent sblockEvent = new SblockAsyncChatEvent(event.isAsynchronous(), event.getPlayer(), event.getRecipients(), msg);
		Bukkit.getPluginManager().callEvent(sblockEvent);
	}

	/**
	 * The first event handler for SblockAsyncChatEvents.
	 * 
	 * Mostly used to remove messages that should not be sent.
	 * 
	 * @param event
	 */
	@EventHandler(ignoreCancelled = true)
	public void onSblockChat(final SblockAsyncChatEvent event) {
		String cleaned = event.getSblockMessage().getCleanedMessage();
		// Test
		if (cleaned.equalsIgnoreCase("test")) {
			event.getPlayer().sendMessage(ChatColor.RED + tests[(int) (Math.random() * 25)]);
			event.setCancelled(true);
			return;
		}
		if (cleaned.equalsIgnoreCase("hal") || cleaned.equalsIgnoreCase("dirk")) {
			event.getPlayer().sendMessage(ColorDef.HAL + "What?");
			event.setCancelled(true);
			return;
		}
		if (event.getSblockMessage().getChannel().getType() == ChannelType.REGION && rpMatch(cleaned)) {
			event.getPlayer().sendMessage(ColorDef.HAL + "RP is not allowed in the main chat. Join #rp or #fanrp using /focus!");
			event.setCancelled(true);
			return;
		}
	}

	/**
	 * The second event handler for SblockAsyncChatEvents.
	 * 
	 * Because we send JSON messages, we actually have to remove all recipients from the event and
	 * manually send each one the message.
	 * 
	 * To combat GriefPrevention modifying the event at HIGHEST for soft mutes, we softdepend on it
	 * in our plugin.yml - this should ideally cause GP to handle it before us.
	 * 
	 * To prevent IRC and other chat loggers from picking it up, it must be cancelled before they
	 * recieve it at MONITOR.
	 * 
	 * @param event the AsyncPlayerChatEvent
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSblockChatComplete(final SblockAsyncChatEvent event) {
		// Region channels are the only ones that should be appearing in certain plugins
		if (event.getSblockMessage().getChannel().getType() != ChannelType.REGION) {
			event.setCancelled(true);
		}

		// Prevent IRC picking up soft muted messages
		if (event.getRecipients().size() < event.getSblockMessage().getChannel().getListening().size()) {
			event.setFormat("[SoftMute] " + event.getFormat());
		}

		// Manually send messages to each player so we can wrap links, etc.
		event.getSblockMessage().send(event.getRecipients());

		// Handle Hal functions
		String msg = ChatColor.stripColor(event.getMessage().toLowerCase());
		if (msg.startsWith("halc ") || msg.startsWith("halculate ") || msg.startsWith("evhal ") || msg.startsWith("evhaluate ")) {
			msg = msg.substring(msg.indexOf(' ')).trim();
			final Message hal = new MessageBuilder().setSender(ChatColor.DARK_RED + "Lil Hal")
					.setChannel(event.getSblockMessage().getChannel())
					.setMessage(ChatColor.RED + Chat.getChat().getHalculator().evhaluate(msg))
					.toMessage();
			if (msg.length() > 30) {
				event.getPlayer().sendMessage(ColorDef.HAL + "Your equation is a bit long for public chat. Please use /halc to reduce spam.");
			}
			hal.send(event.getRecipients());
		} else {
			Chat.getChat().getHal().handleMessage(event.getSblockMessage(), event.getRecipients());
		}
	}

	public boolean rpMatch(String message) {
		if (message.matches("([hH][oO][nN][kK] ?)+")) {
			return true;
		}
		return false;
	}
}
