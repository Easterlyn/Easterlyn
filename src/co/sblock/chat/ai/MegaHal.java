package co.sblock.chat.ai;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import org.jibble.jmegahal.JMegaHal;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.NickChannel;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.utilities.Log;
import co.sblock.utilities.general.Cooldowns;
import co.sblock.utilities.messages.JSONUtil;
import co.sblock.utilities.messages.RegexUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * Sblock's JMegaHal implementation - Chester is just too buggy and difficult to customize.
 * 
 * @author Jikoo
 */
public class MegaHal extends HalMessageHandler {

	private final Pattern exactPattern, whitespacePattern;
	private final JMegaHal hal;
	private final Set<String> pendingMessages;
	private final HalLogSavingTask save;
	private final Set<Pattern> ignoreMatches;
	private final ItemStack hover;
	private int fileNum;
	private final MessageBuilder noSpam;

	public MegaHal() {
		hal = new JMegaHal();
		fileNum = 0;

		exactPattern = Pattern.compile("^(hal|dirk)$", Pattern.CASE_INSENSITIVE);
		whitespacePattern = Pattern.compile("(^|\\W)(hal|dirk)(\\W|$)", Pattern.CASE_INSENSITIVE);
		pendingMessages = Collections.synchronizedSet(new LinkedHashSet<String>());

		ignoreMatches = new HashSet<>();
		ignoreMatches.add(whitespacePattern);
		ignoreMatches.add(RegexUtils.URL_PATTERN);
		ignoreMatches.add(Pattern.compile("^.*b(e|a)n(j(ie|y))? ?z(u|e)?rf(les?)?.*$", Pattern.CASE_INSENSITIVE));
		ignoreMatches.add(Pattern.compile("^halc(ulate)? .*$", Pattern.CASE_INSENSITIVE));
		ignoreMatches.add(Pattern.compile("^evhal(uate)? .*$", Pattern.CASE_INSENSITIVE));
		ignoreMatches.add(Pattern.compile("^.*dad(dy)?.*$", Pattern.CASE_INSENSITIVE));

		hover = new ItemStack(Material.BARRIER);
		ItemMeta hoverMeta = hover.getItemMeta();
		hoverMeta.setDisplayName(ChatColor.RED + "Artificial Intelligence");
		hoverMeta.setLore(Arrays.asList(new String[] {
				Color.BAD_EMPHASIS + "Sblock is not responsible",
				Color.BAD_EMPHASIS + "for anything Hal says.", "",
				Color.BAD_EMPHASIS + "Unless it's awesome.", "",
				Color.COMMAND + "/join #halchat" + Color.BAD + " to spam usage."}));
		hover.setItemMeta(hoverMeta);

		noSpam = new MessageBuilder().setSender(ChatColor.DARK_RED + "Lil Hal")
				.setNameClick("/join #halchat").setNameHover(hover).setChannelClick("@#halchat ")
				.setMessage( JSONUtil.fromLegacyText(ChatColor.RED + "To spam with me, join #halchat."));

		loadHal();

		save = new HalLogSavingTask();
		save.runTaskTimer(Sblock.getInstance(), 6000L, 6000L);
	}

	@Override
	public boolean handleMessage(Message msg, Collection<Player> recipients) {
		if (msg.getSender() == null || msg.getChannel() instanceof NickChannel) {
			return true;
		}
		Player sender = msg.getSender().getPlayer();
		if (sender == null) {
			return true;
		}
		String message = ChatColor.stripColor(msg.getMessage());
		if (isTrigger(message)) {
			if (isOnlyTrigger(message)) {
				// Set sender on fire or some shit
				msg.getSender().sendMessage(Color.HAL.replaceFirst("#", msg.getChannel().getName()) + "What?");
				return true;
			}
			String channel = msg.getChannel().getName();
			if (!channel.equals("#halchat")) {
				Cooldowns cooldowns = Cooldowns.getInstance();
				if (cooldowns.getGlobalRemainder("megahal" + channel) > 0) {
					// Still on cooldown, warn a bitch
					noSpam.setChannel(msg.getChannel());
					noSpam.toMessage().send(Arrays.asList(sender));
					Log.getLog("MegaHal").info("Warned " + msg.getSender().getPlayerName() + " about spamming Hal");
					return true;
				} else {
					cooldowns.addGlobalCooldown(channel, 2500L);
				}
			}
			HashSet<UUID> recipientUUIDs = new HashSet<>();
			recipients.forEach(player -> recipientUUIDs.add(player.getUniqueId()));
			triggerResponse(recipientUUIDs, msg.getChannel(), message, true);
		} else if (msg.getChannel().getAccess() != AccessLevel.PRIVATE) {
			log(msg, message);
		}
		return true;
	}

	public boolean isTrigger(String message) {
		return whitespacePattern.matcher(message).find();
	}

	public boolean isOnlyTrigger(String message) {
		return exactPattern.matcher(message).find();
	}

	public void log(Message message, String msg) {
		if (message.getChannel().getAccess() == AccessLevel.PRIVATE
				|| message.getChannel() instanceof NickChannel) {
			return;
		}
		log(msg);
	}

	public synchronized void log(String message) {
		message = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message));
		if (message.isEmpty() || message.length() < 15 || message.startsWith("(")
				|| message.contains("/") || isTrigger(message)) {
			return;
		}
		// CHAT: strip more stuff we don't want
		for (Pattern pattern : ignoreMatches) {
			if (pattern.matcher(message).find()) {
				return;
			}
		}
		pendingMessages.add(message);
		if (save.getTaskId() == -1) {
			Log.getLog("MegaHal").warning("Log saving task was stopped! Restarting...");
			save.runTaskTimer(Sblock.getInstance(), 0L, 6000L);
		}
		hal.add(message);
	}

	public void triggerResponse(final Channel channel, final String message, final boolean filter) {
		triggerResponse(channel.getListening(), channel, message, filter);
	}

	public synchronized void triggerResponse(final Collection<UUID> recipients, final Channel channel, final String message, final boolean filter) {
		new BukkitRunnable() {
			@Override
			public void run() {
				String word;
				if (filter) {
					word = selectRandomWord(message);
				} else {
					word = message;
				}
				word = word == null ? hal.getSentence() : hal.getSentence(word);
				word = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', word));
				Message msg = new MessageBuilder().setSender(ChatColor.DARK_RED + "Lil Hal")
						.setMessage(ChatColor.RED + word).setChannel(channel)
						.setChannelClick("@#halchat ").setNameClick("/join #halchat")
						.setNameHover(hover).toMessage();
				msg.send(recipients);
			}
		}.runTaskAsynchronously(Sblock.getInstance());
	}

	private String selectRandomWord(String message) {
		if (message == null) {
			return null;
		}
		String[] words = message.split(" ");
		String word = words[(int) (Math.random() * words.length)];
		// Fewer non-word matches
		word = word.replaceAll("[^A-Za-z\\-_]", "");
		if (word.isEmpty()) {
			return null;
		}
		return word;
	}

	public class HalLogSavingTask extends BukkitRunnable {
		@Override
		public void run() {
			saveLogs();
		}
	}

	public void saveLogs() {
		if (pendingMessages.size() == 0) {
			return;
		}
		try (FileWriter filewriter = new FileWriter(getFirstAvailableHalFile(), true);
				PrintWriter writer = new PrintWriter(filewriter)) {
			for (String s : pendingMessages) {
				writer.println(s);
			}
			pendingMessages.clear();
		} catch (IOException e) {
			Log.getLog("MegaHal").err(e);
		}
		if (pendingMessages.size() > 0) {
			saveLogs();
		}
	}

	private File getFirstAvailableHalFile() {
		File folder = new File("plugins/Sblock/MegaHal");
		if (!folder.exists()) {
			folder.mkdir();
			return new File(folder, "hal-0.log");
		}
		while (true) {
			File log = new File(folder, "hal-" + fileNum + ".log");
			if (log.exists() && log.length() > 1048000L) { // ~1M
				fileNum++;
				Log.getLog("MegaHal").info("Hal log too large, rolling to " + fileNum);
				continue;
			}
			return log;
		}
	}

	public void loadHal() {

		new BukkitRunnable() {
			@Override
			public void run() {
				File logDir = new File("plugins/Sblock/MegaHal");
				if (logDir.exists()) {
					File[] logs = logDir.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.matches("hal-[0-9]+\\.log");
						}
					});
					for (File file : logs) {
						try {
							List<String> halLogs = FileUtils.readLines(file);
							for (String string : halLogs) {
								hal.add(string);
							}
						} catch (IOException e) {
							Log.getLog("MegaHal").err(e);
						}
					}
				}
				Log.getLog("MegaHal").info("Finished loading Hal logs.");
			}
		}.runTaskAsynchronously(Sblock.getInstance());
	}
}
