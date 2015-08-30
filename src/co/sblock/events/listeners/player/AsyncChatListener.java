package co.sblock.events.listeners.player;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.chat.Chat;
import co.sblock.chat.Color;
import co.sblock.chat.ai.HalMessageHandler;
import co.sblock.chat.ai.Halper;
import co.sblock.chat.channel.RegionChannel;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.micromodules.Slack;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.Cooldowns;
import co.sblock.utilities.JSONUtil;
import co.sblock.utilities.RegexUtils;
import co.sblock.utilities.WrappedSenderPlayer;

import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;

import net.md_5.bungee.api.ChatColor;

/**
 * Listener for PlayerAsyncChatEvents.
 * 
 * @author Jikoo
 */
public class AsyncChatListener implements Listener {

	private final LinkedHashSet<HalMessageHandler> halFunctions;
	private final String[] tests = new String[] {"It is certain.", "It is decidedly so.",
			"Without a doubt.", "Yes, definitely.", "You may rely on it.", "As I see, yes.",
			"Most likely.", "Outlook good.", "Yes.", "Signs point to yes.",
			"Reply hazy, try again.", "Ask again later.", "Better not tell you now.",
			"Cannot predict now.", "Concentrate and ask again.", "Don't count on it.",
			"My reply is no.", "My sources say no.", "Outlook not so good.", "Very doubtful.",
			"Testing complete. Proceeding with operation.", "A critical fault has been discovered while testing.",
			"Error: Test results contaminated.", "tset", "PONG."};
	private final boolean handleGriefPrevention;
	private final Pattern claimPattern, trappedPattern;

	public AsyncChatListener() {
		Permission permission;
		try {
			permission = new Permission("sblock.spam.chat", PermissionDefault.OP);
			Bukkit.getPluginManager().addPermission(permission);
		} catch (IllegalArgumentException e) {
			permission = Bukkit.getPluginManager().getPermission("sblock.spam.chat");
			permission.setDefault(PermissionDefault.OP);
		}
		permission.addParent("sblock.command.*", true).recalculatePermissibles();
		permission.addParent("sblock.felt", true).recalculatePermissibles();

		halFunctions = new LinkedHashSet<>();
		halFunctions.add(new Halper());
		halFunctions.add(Chat.getChat().getHalculator());
		// MegaHal function should be last as it (by design) handles any message passed to it.
		// Insert any additional functions above.
		halFunctions.add(Chat.getChat().getHal());

		handleGriefPrevention = Bukkit.getPluginManager().isPluginEnabled("GriefPrevention");
		if (handleGriefPrevention) {
			unregisterChatListeners();
			claimPattern = Pattern.compile("(^|.*\\W)how\\W.*\\W(claim|protect|lock)(\\W.*|$)", Pattern.CASE_INSENSITIVE);
			trappedPattern = Pattern.compile("(^|\\s)(stuck|trapped(?! chest))(\\W|\\s|$)", Pattern.CASE_INSENSITIVE);
		} else {
			claimPattern = null;
			trappedPattern = null;
		}
	}

	/**
	 * Because we send JSON messages, we actually have to remove all recipients from the event and
	 * manually send each one the message.
	 * 
	 * To prevent IRC and other chat loggers from picking up chat sent to non-regional channels,
	 * non-regional chat must be cancelled.
	 * 
	 * @param event the SblockAsyncChatEvent
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
		Message message;
		boolean checkSpam = true;
		if (event instanceof SblockAsyncChatEvent) {
			SblockAsyncChatEvent sblockEvent = (SblockAsyncChatEvent) event;
			message = sblockEvent.getSblockMessage();
			checkSpam = sblockEvent.checkSpam();
		} else {
			try {
				MessageBuilder mb = new MessageBuilder().setSender(Users.getGuaranteedUser(event.getPlayer().getUniqueId()))
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
		String cleaned = ChatColor.stripColor(message.getMessage());

		if (checkSpam) {
			if (cleaned.equalsIgnoreCase("test")) {
				event.getPlayer().sendMessage(ChatColor.RED + tests[(int) (Math.random() * 25)]);
				event.setCancelled(true);
				return;
			}

			for (Player recipient : event.getRecipients()) {
				if (cleaned.equalsIgnoreCase(recipient.getName())) {
					event.getPlayer().sendMessage(
							ChatColor.RED + "Names are short and easy to include in a sentence, "
									+ player.getDisplayName() + ". Please do it.");
					event.setCancelled(true);
					return;
				}
			}

			if (Chat.getChat().getHal().isOnlyTrigger(cleaned)) {
				player.sendMessage(Color.HAL + "What?");
				event.setCancelled(true);
				return;
			}

			if (message.getChannel() instanceof RegionChannel && rpMatch(cleaned)) {
				player.sendMessage(Color.HAL
						+ "RP is not allowed in the main chat. Join #rp or #fanrp using /focus!");
				event.setCancelled(true);
				return;
			}
		}

		event.setFormat(message.getConsoleFormat());
		event.setMessage(cleaned);

		if (checkSpam && handleGriefPrevention) {
			handleGPChat(event, message);
			if (event.isCancelled()) {
				return;
			}
		}

		final OfflineUser sender = message.getSender();

		// Spam detection and handling, woo!
		if (checkSpam && sender != null && !message.getChannel().getName().equals("#halchat")
				&& detectSpam(event, message)) {
			event.getRecipients().clear();
			event.getRecipients().add(player);
			if (sender.getChatViolationLevel() > 8 && sender.getChatWarnStatus()) {
				sendMessageOnDelay(player, Color.HAL.replace("#", message.getChannel().getName())
						+ "You were asked not to spam. This mute will last 5 minutes.");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
						String.format("mute %s 5m", player.getName()));
				Slack.getInstance().postReport(player.getName(), player.getUniqueId(),
						"Automatically muted for spamming, violation level " + sender.getChatViolationLevel());
				event.setCancelled(true);
				return;
			}
			if (sender.getChatViolationLevel() > 3 && !sender.getChatWarnStatus()) {
				sendMessageOnDelay(player, Color.HAL.replace("#", message.getChannel().getName())
						+ "You appear to be spamming. Please slow down chat.");
				sender.setChatWarnStatus(true);
			}
		}

		// Set message format in case of softmute or spam detection
		message.setConsoleFormat(event.getFormat());

		// Region channels are the only ones that should be appearing in certain plugins
		if (!(message.getChannel() instanceof RegionChannel)) {
			if (!event.isCancelled() && event instanceof SblockAsyncChatEvent) {
				((SblockAsyncChatEvent) event).setGlobalCancelled(true);
			} else {
				event.setCancelled(true);
			}
		}

		// Flag channel as having been used so it is not deleted.
		message.getChannel().updateLastAccess();

		// Manually send messages to each player so we can wrap links, etc.
		message.send(event.getRecipients(), !(event instanceof SblockAsyncChatEvent));

		// Dummy player should not trigger Hal; he may become one.
		if (player instanceof WrappedSenderPlayer) {
			event.getRecipients().clear();
			return;
		}

		// Handle Hal functions
		for (HalMessageHandler handler : halFunctions) {
			if (handler.handleMessage(message, event.getRecipients())) {
				break;
			}
		}

		// No one should receive the final message if it is not cancelled.
		event.getRecipients().clear();
	}

	public boolean rpMatch(String message) {
		if (message.matches("([hH][oO][nN][kK] ?)+")) {
			return true;
		}
		return false;
	}

	private void handleGPChat(final AsyncPlayerChatEvent event, final Message message) {
		if (message.getSender() == null || GriefPrevention.instance == null) {
			return;
		}

		final DataStore dataStore = GriefPrevention.instance.dataStore;
		final Player player = event.getPlayer();
		final String world = player.getWorld().getName();

		if (!world.equals("Derspit") && !world.equals("DreamBubble")
				&& claimPattern.matcher(message.getMessage()).find()) {
			sendMessageOnDelay(player,Color.GOOD
					+ "For information about claims, watch https://www.youtube.com/watch?v=VDsjXB-BaE0&list=PL8YpI023Cthye5jUr-KGHGfczlNwgkdHM&index=1");
		}
		if (trappedPattern.matcher(message.getMessage()).find()) {
			// Improvement over GP: Pattern ignores case and matches in substrings of words
			sendMessageOnDelay(player, Color.GOOD + "Trapped in someone's land claim? Try "
					+ Color.COMMAND + "/trapped");
		}

		// Soft-muted chat
		if (dataStore.isSoftMuted(player.getUniqueId())) {
			event.setFormat("[SoftMute] " + event.getFormat());
			String soft = new StringBuilder().append(ChatColor.GRAY).append("[SoftMute] ")
					.append(ChatColor.stripColor(message.getConsoleMessage())).toString();
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

		// Fix for GP issue: SoftMuted players cannot ignore others - don't return as soon as softmute is handled.

		// Don't send messages to players ignoring sender or who the sender is ignoring
		Iterator<Player> iterator = event.getRecipients().iterator();
		PlayerData data = dataStore.getPlayerData(player.getUniqueId());
		while (iterator.hasNext()) {
			UUID uuid = iterator.next().getUniqueId();
			if (data.ignoredPlayers.containsKey(uuid)) {
				iterator.remove();
				continue;
			}
			if (dataStore.getPlayerData(uuid).ignoredPlayers.containsKey(player.getUniqueId())) {
				iterator.remove();
				continue;
			}
		}
	}

	private boolean detectSpam(AsyncPlayerChatEvent event, Message message) {
		final Player player = event.getPlayer();
		final OfflineUser sender = message.getSender();
		if (sender == null || player.hasPermission("sblock.spam.chat")) {
			return false;
		}

		String msg = message.getMessage();

		// Caps filter only belongs in regional channels.
		if (message.getChannel() instanceof RegionChannel && msg.length() > 3
				&& StringUtils.getLevenshteinDistance(msg, msg.toUpperCase()) < msg.length() * .25) {
			StringBuilder msgBuilder = new StringBuilder();
			for (String word : msg.split(" ")) {
				if (RegexUtils.URL_PATTERN.matcher(word).find()) {
					msgBuilder.append(word);
				} else {
					msgBuilder.append(word.toLowerCase());
				}
				msgBuilder.append(' ');
			}
			if (msgBuilder.length() > 0) {
				msgBuilder.deleteCharAt(msgBuilder.length() - 1);
			}
			message.setMessage(msgBuilder.toString());
		}

		msg = msg.toLowerCase();
		String lastMsg = sender.getLastMessage();
		sender.setLastChat(msg);
		message.getChannel().setLastMessage(msg);
		long lastChat = Cooldowns.getInstance().getRemainder(player, "chat");
		Cooldowns.getInstance().addCooldown(player, "chat", 3000);

		// Mute repeat messages
		if (msg.equals(lastMsg) || message.equals(message.getChannel().getLastMessage())) {
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
		if (lastChat > 1500 || msg.length() < 5 && lastChat > 0) {
			sender.setChatViolationLevel(sender.getChatViolationLevel() + 1);
			event.setFormat("[FastChat] " + event.getFormat());
			return true;
		}

		// Sans links, messages should contain a good symbol/space to length ratio
		String[] words = msg.split(" ");
		int spaces = words.length - 1;
		int length = msg.length();
		int symbols = 0;
		for (String word : words) {
			if (RegexUtils.URL_PATTERN.matcher(word).find()) {
				length -= word.length();
				spaces--;
				continue;
			}
			for (char character : word.toCharArray()) {
				if (!Character.isLetterOrDigit(character)) {
					symbols++;
				}
			}
		}
		if (symbols > length / 2 || length > 15 && spaces < length / 10) {
			sender.setChatViolationLevel(sender.getChatViolationLevel() + 1);
			event.setFormat("[Gibberish] " + event.getFormat());
			return true;
		}

		// Must be more than 25% different from last message
		if (StringUtils.getLevenshteinDistance(msg, lastMsg) < msg.length() * .25) {
			sender.setChatViolationLevel(sender.getChatViolationLevel() + 1);
			event.setFormat("[SimilarChat] " + event.getFormat());
			return true;
		}

		sender.setChatViolationLevel(0);
		sender.setChatWarnStatus(false);
		return false;
	}

	private void sendMessageOnDelay(final Player player, final String message) {
		if (player.spigot() == null) {
			return;
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				if (player != null) {
					player.spigot().sendMessage(JSONUtil.fromLegacyText(message));
				}
			}
		}.runTaskLater(Sblock.getInstance(), 10L);
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
}
