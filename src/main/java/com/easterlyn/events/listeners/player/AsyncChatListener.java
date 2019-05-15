package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.Language;
import com.easterlyn.chat.ai.HalMessageHandler;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.NickChannel;
import com.easterlyn.chat.channel.RegionChannel;
import com.easterlyn.chat.message.Message;
import com.easterlyn.chat.message.MessageBuilder;
import com.easterlyn.discord.Discord;
import com.easterlyn.events.event.EasterlynAsyncChatEvent;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.AwayFromKeyboard;
import com.easterlyn.micromodules.Cooldowns;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.StringMetric;
import com.easterlyn.utilities.TextUtils;
import com.easterlyn.utilities.player.WrappedSenderPlayer;
import com.google.common.collect.ImmutableList;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * Listener for PlayerAsyncChatEvents.
 *
 * @author Jikoo
 */
public class AsyncChatListener extends EasterlynListener {

	private static String[] TEST;

	private final AwayFromKeyboard afk;
	private final Chat chat;
	private final Cooldowns cooldowns;
	private final Discord discord;
	private final Language lang;
	private final Users users;
	private final List<HalMessageHandler> halFunctions;
	private final boolean handleGriefPrevention;
	private final Pattern claimPattern, trappedPattern, yoooooooooooooooooooooooooooooooooooooooo;
	private final List<Pattern> doNotSayThat;

	public AsyncChatListener(Easterlyn plugin) {
		super(plugin);
		this.afk = plugin.getModule(AwayFromKeyboard.class);
		this.chat = plugin.getModule(Chat.class);
		this.cooldowns = plugin.getModule(Cooldowns.class);
		this.discord = plugin.getModule(Discord.class);
		this.lang = plugin.getModule(Language.class);
		this.users = plugin.getModule(Users.class);

		TEST = lang.getValue("events.chat.test").split("\n");

		halFunctions = new ArrayList<>();
		Chat chat = plugin.getModule(Chat.class);
		halFunctions.add(chat.getHalculator());

		// If, say, the Hal AI fails to load, don't NPE whenever anyone talks.
		halFunctions.removeIf(Objects::isNull);

		handleGriefPrevention = Bukkit.getPluginManager().isPluginEnabled("GriefPrevention");
		if (handleGriefPrevention) {
			unregisterChatListeners();
			claimPattern = Pattern.compile("(^|.*\\W)how\\W.*\\W(claim|protect|lock)(\\W.*|$)", Pattern.CASE_INSENSITIVE);
			trappedPattern = Pattern.compile("(^|\\s)(stuck|trapped(?! (chest|horse)))(\\W|\\s|$)", Pattern.CASE_INSENSITIVE);
		} else {
			claimPattern = null;
			trappedPattern = null;
		}

		yoooooooooooooooooooooooooooooooooooooooo = Pattern.compile("[Yy][Oo]+");

		doNotSayThat = new ArrayList<>();
		for (String pattern : lang.getValue("events.chat.filter").split("\n")) {
			doNotSayThat.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
		}
	}

	/**
	 * Because we send JSON messages, we actually have to remove all recipients from the event and
	 * manually send each one the message.
	 *
	 * To prevent IRC and other chat loggers from picking up chat sent to non-regional channels,
	 * non-regional chat must be cancelled.
	 *
	 * @param event the EasterlynAsyncChatEvent
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
		afk.setActive(event.getPlayer());

		Message message;
		boolean checkSpam = true;
		if (event instanceof EasterlynAsyncChatEvent) {
			EasterlynAsyncChatEvent easterlynEvent = (EasterlynAsyncChatEvent) event;
			message = easterlynEvent.getEasterlynMessage();
			checkSpam = easterlynEvent.checkSpam();
		} else {
			try {
				MessageBuilder mb = new MessageBuilder(getPlugin())
						.setSender(users.getUser(event.getPlayer().getUniqueId()))
						.setMessage(event.getMessage());
				// Ensure message can be sent
				if (!mb.canBuild(true) || !mb.isSenderInChannel(true)) {
					event.setCancelled(true);
					return;
				}
				message = mb.toMessage();

				event.getRecipients().removeIf(p -> !message.getChannel().getListening().contains(p.getUniqueId()));
			} catch (Exception e) {
				event.setCancelled(true);
				e.printStackTrace();
				return;
			}
		}

		final Player player = event.getPlayer();
		String cleaned = ChatColor.stripColor(message.getRawMessage());
		boolean publishGlobally = message.getChannel().getName().equals("#");
		Set<String> names = new HashSet<>();

		if (checkSpam) {
			if (cleaned.equalsIgnoreCase("test")) {
				event.getPlayer().sendMessage(ChatColor.RED + test());
				event.setCancelled(true);
				return;
			}

			event.getRecipients().forEach(uuid -> {
				names.add(player.getName());
				names.add(ChatColor.stripColor(player.getDisplayName()));
			});

			for (String name : names) {
				if (cleaned.equalsIgnoreCase(name)) {
					this.messageLater(player, message.getChannel(),
							lang.getValue("events.chat.ping").replace("{PLAYER}",
									player.getDisplayName()));
					event.setCancelled(true);
					return;
				}
			}
		}

		event.setFormat(message.getConsoleFormat());
		event.setMessage(cleaned);

		if (checkSpam && handleGriefPrevention) {
			if (handleGPChat(event, message)) {
				publishGlobally = false;
			}
			if (event.isCancelled()) {
				return;
			}
		}

		final User sender = message.getSender();

		// Spam detection and handling, woo!
		if (checkSpam && sender != null && !message.getChannel().getName().equals(lang.getValue("chat.spamChannel"))
				&& detectSpam(event, message, names)) {
			publishGlobally = false;
			event.getRecipients().clear();
			event.getRecipients().add(player);
			if (sender.getChatViolationLevel() > 8 && sender.getChatWarnStatus()) {
				messageLater(player, message.getChannel(), lang.getValue("events.chat.mute"));
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
						String.format("mute %s 5m", player.getName()));
				event.setCancelled(true);
				try {
					discord.postReport("Automatically muted " + player.getName()
							+ " for spamming, violation level " + sender.getChatViolationLevel());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
			if (sender.getChatViolationLevel() > 3 && !sender.getChatWarnStatus()) {
				this.messageLater(player, message.getChannel(), lang.getValue("events.chat.spam"));
				sender.setChatWarnStatus(true);
			}
		}

		// Set message format in case of softmute or spam detection
		message.setConsoleFormat(event.getFormat());

		// RegionUtils channels are the only ones that should be appearing in certain plugins
		if (!(message.getChannel() instanceof RegionChannel)) {
			if (!event.isCancelled() && event instanceof EasterlynAsyncChatEvent) {
				((EasterlynAsyncChatEvent) event).setGlobalCancelled(true);
			} else {
				event.setCancelled(true);
			}
		}

		// Flag channel as having been used so it is not deleted.
		message.getChannel().updateLastAccess();

		// Don't actually display tests to anyone.
		if (message.getChannel().getName().equals("@test@")) {
			event.getRecipients().clear();
			return;
		}

		// Manually send messages to each player so we can wrap links, etc.
		message.send(event.getRecipients(), event instanceof EasterlynAsyncChatEvent, true);

		// Post messages to Discord
		try {
			discord.postMessage(message, publishGlobally);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Dummy player should not trigger Hal; he may become one.
		if (player instanceof WrappedSenderPlayer) {
			event.getRecipients().clear();
			return;
		}

		try {
			// Handle Hal functions
			for (HalMessageHandler handler : halFunctions) {
				if (handler.handleMessage(message, event.getRecipients())) {
					break;
				}
			}
		} catch (Exception e) {
			// Just in case, don't allow a Hal integration mistake to break chat.
			e.printStackTrace();
		}

		// No one should receive the final message if it is not cancelled.
		event.getRecipients().clear();
	}

	/**
	 * Handles chat in a way similar to how GP's listener does, sans the extra checks we do not
	 * want.
	 *
	 * @param event the AsyncPlayerChatEvent
	 * @param message the Message
	 * @return true if the sender is soft muted
	 */
	private boolean handleGPChat(final AsyncPlayerChatEvent event, final Message message) {
		if (message.getSender() == null || GriefPrevention.instance == null) {
			return false;
		}

		final DataStore dataStore = GriefPrevention.instance.dataStore;
		final Player player = event.getPlayer();
		final String world = player.getWorld().getName();

		if (!world.equals("Derspit") && !world.equals("DreamBubble")
				&& claimPattern.matcher(message.getRawMessage()).find()) {
			messageLater(player, message.getChannel(), lang.getValue("events.chat.gp.claims"));
		}
		if (trappedPattern.matcher(message.getRawMessage()).find()) {
			// Improvement over GP: Pattern ignores case and matches in substrings of words
			messageLater(player, message.getChannel(), lang.getValue("events.chat.gp.trapped"));
		}

		// Soft-muted chat
		boolean softMute = dataStore.isSoftMuted(player.getUniqueId());
		if (softMute) {
			event.setFormat("[SoftMute] " + event.getFormat());
			String soft = ChatColor.GRAY + "[SoftMute] " + ChatColor.stripColor(message.getConsoleMessage());
			Iterator<Player> iterator = event.getRecipients().iterator();
			while (iterator.hasNext()) {
				Player recipient = iterator.next();
				if (dataStore.isSoftMuted(recipient.getUniqueId())) {
					continue;
				}
				iterator.remove();
				if (recipient.hasPermission("griefprevention.eavesdrop")) {
					recipient.sendMessage(soft);
				}
			}
		}

		// Don't send messages to players ignoring sender or who the sender is ignoring
		Iterator<Player> iterator = event.getRecipients().iterator();
		PlayerData senderData = dataStore.getPlayerData(player.getUniqueId());
		boolean ignorable = !player.hasPermission("griefprevention.notignorable");
		while (iterator.hasNext()) {
			Player recipient = iterator.next();
			UUID uuid = recipient.getUniqueId();
			if (ignorable) {
				if (dataStore.getPlayerData(uuid).ignoredPlayers.containsKey(player.getUniqueId())) {
					iterator.remove();
					continue;
				}
				if (recipient.hasPermission("griefprevention.notignorable")) {
					continue;
				}
			}
			if (senderData.ignoredPlayers.containsKey(uuid)) {
				iterator.remove();
			}
		}
		return softMute;
	}

	private boolean detectSpam(AsyncPlayerChatEvent event, Message message, Set<String> names) {
		final Player player = event.getPlayer();
		final User sender = message.getSender();
		if (sender == null || player.hasPermission("easterlyn.chat.unfiltered")) {
			return false;
		}

		String msg = message.getRawMessage();
		// Remove prefixed "Username: " in #pm
		if (message.getChannel().getName().equals("#pm")) {
			// We want the space in the prefix, add 1 once rather than twice.
			int space = msg.indexOf(' ') + 1;
			if (space > 0) {
				msg = msg.substring(space);
			}
		}

		// Caps filter in regional channels.
		if (!player.hasPermission("easterlyn.chat.spam.caps")
				&& message.getChannel() instanceof RegionChannel) {

			StringBuilder urlStripped = new StringBuilder();
			for (String word : msg.split("\\s")) {
				if (TextUtils.matchURL(word) == null) {
					urlStripped.append(word).append(' ');
				}
			}

			String msgSansURLs = urlStripped.toString().trim();

			if (msgSansURLs.length() > 10 && StringMetric.compareJaroWinkler(msgSansURLs,
					msgSansURLs.toUpperCase()) > .75F) {
				// TODO: verify that StringMetric does not ignore case
				this.toLowerCase(message.getMessageComponent());
			}
		}

		// Strip characters that are not allowed in default channels and partial caps
		if (!player.hasPermission("easterlyn.chat.spam.normalize") && message.getChannel().getOwner() == null
				&& !(message.getChannel() instanceof NickChannel)) {
			this.normalize(message.getMessageComponent(), names);
		}

		msg = msg.toLowerCase();
		String lastMsg = sender.getLastChat();
		sender.setLastChat(msg);
		long lastChat = cooldowns.getRemainder(player, "chat");
		cooldowns.addCooldown(player, "chat", 30000);

		for (Pattern pattern : doNotSayThat) {
			if (pattern.matcher(msg).find()) {
				sender.setChatViolationLevel(sender.getChatViolationLevel() + 2);
				event.setFormat("[Scumbag] " + event.getFormat());
				return true;
			}
		}

		// Mute repeat messages within 30 seconds
		if (!player.hasPermission("easterlyn.chat.spam.repeat") && lastChat > 0 && msg.equals(lastMsg)) {
			// In event of exact duplicates, reach penalization levels at a much faster rate
			int spamCount = sender.getChatViolationLevel();
			if (spamCount == 0) {
				spamCount++;
			} else {
				spamCount *= 2;
			}
			sender.setChatViolationLevel(spamCount);
			event.setFormat("[RepeatChat] " + event.getFormat());
			return true;
		}

		// Cooldown of 1.5 seconds between messages, 3 seconds between short messages.
		if (!player.hasPermission("easterlyn.chat.spam.fast")
				&& (lastChat > 28500 || msg.length() < 12 && lastChat > 27000)) {
			sender.setChatViolationLevel(sender.getChatViolationLevel() + 2);
			event.setFormat("[FastChat] " + event.getFormat());
			return true;
		}

		// Sans links, messages should contain a good symbol/space to length ratio
		String[] words = msg.split(" ");
		int spaces = words.length - 1;
		int length = msg.length();
		int symbols = 0;
		boolean question = false;
		for (String word : words) {
			if (TextUtils.matchURL(word) != null) {
				length -= word.length();
				spaces--;
				continue;
			}
			for (char character : word.toCharArray()) {
				if (!Character.isLetterOrDigit(character)) {
					if (!question && character == '?') {
						// Discount one question mark from spam filtering in case of "?" or "<link> ?"
						question = true;
						length--;
						continue;
					}
					symbols++;
				}
			}
		}

		if (!player.hasPermission("easterlyn.chat.spam.gibberish")
				&& !yoooooooooooooooooooooooooooooooooooooooo.matcher(msg).matches()
				&& (symbols > length / 2 || length > 15 && spaces < length / 10)) {
			sender.setChatViolationLevel(sender.getChatViolationLevel() + 1);
			event.setFormat("[Gibberish] " + event.getFormat());
			return true;
		}

		// Must be more than 25% different from last message
		if (!player.hasPermission("easterlyn.chat.spam.repeat") && lastChat > 0
				&& StringMetric.compareJaroWinkler(msg, lastMsg) > .75F) {
			sender.setChatViolationLevel(sender.getChatViolationLevel() + 2);
			event.setFormat("[SimilarChat] " + event.getFormat());
			return true;
		}

		sender.setChatViolationLevel(0);
		sender.setChatWarnStatus(false);
		return false;
	}

	private void toLowerCase(TextComponent component) {
		if (component.getExtra() != null) {
			for (BaseComponent extra : component.getExtra()) {
				if (extra instanceof TextComponent) {
					// Recursively set lower case
					this.toLowerCase((TextComponent) extra);
				}
			}
		}

		component.setText(component.getText().toLowerCase());
	}

	private void normalize(TextComponent component, Set<String> names) {
		if (component.getExtra() != null) {
			for (BaseComponent extra : component.getExtra()) {
				if (extra instanceof TextComponent) {
					// Recursively normalize
					this.normalize((TextComponent) extra, names);
				}
			}
		}

		String text = Normalizer.normalize(component.getText(), Normalizer.Form.NFD);
		StringBuilder textBuilder = new StringBuilder(text.length());
		int lastSpace = 0;

		for (int index = 0; index < text.length(); ++index) {
			char character = text.charAt(index);

			// Ensure character is whitespace and we aren't encountering 2 whitespace in a row
			if (!Character.isWhitespace(character) || index == lastSpace) {
				continue;
			}

			// Space found, handle word
			String word = text.substring(lastSpace, index);
			// Skip over space character
			lastSpace = index + 1;

			word = this.normalizeWord(word, names);
			textBuilder.append(word).append(' ');
		}
		component.setText(textBuilder.append(this.normalizeWord(text.substring(lastSpace), names)).toString());
	}

	private String normalizeWord(String word, Set<String> names) {

		if (word.isEmpty()) {
			return word;
		}

		if (TextUtils.matchURL(word) != null) {
			return word;
		}

		// Anything goes as long as it's the name of a recipient
		if (names.contains(TextUtils.stripEndPunctuation(word))) {
			return word;
		}

		StringBuilder wordBuilder = new StringBuilder(word.length());

		for (char character : word.toCharArray()) {
			if (isCharacterGloballyLegal(character) || character == ChatColor.COLOR_CHAR) {
				wordBuilder.append(character);
			}
		}

		return wordBuilder.toString();
	}

	private boolean isCharacterGloballyLegal(char character) {
		return character >= ' ' && character <= '}';
	}

	private void messageLater(final Player player, final Channel channel, final String message) {
		if (player.spigot() == null) {
			return;
		}
		final UUID uuid = player.getUniqueId();
		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = Bukkit.getPlayer(uuid);
				if (player != null) {
					chat.getHalBase().setChannel(channel)
							.setMessage(Language.getColor("bot_text") + message).toMessage()
							.send(ImmutableList.of(player));
				}
			}
		}.runTaskLater(getPlugin(), 1L);
	}

	private void unregisterChatListeners() {
		for (RegisteredListener listener : AsyncPlayerChatEvent.getHandlerList().getRegisteredListeners()) {
			String plugin = listener.getPlugin().getName();
			if (plugin.equals("GriefPrevention")) {
				AsyncPlayerChatEvent.getHandlerList().unregister(listener);
				break;
			}
		}
	}

	public static String test() {
		return TEST != null ? TEST[ThreadLocalRandom.current().nextInt(TEST.length)] : "Test successful.";
	}

}
