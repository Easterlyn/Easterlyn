package co.sblock.chat.ai;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import net.minecraft.util.org.apache.commons.io.FileUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import org.jibble.jmegahal.JMegaHal;

import co.sblock.Sblock;
import co.sblock.chat.ColorDef;
import co.sblock.chat.Message;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.utilities.Log;
import co.sblock.utilities.regex.RegexUtils;

/**
 * Sblock's JMegaHal implementation - Chester is just too buggy and difficult to customize.
 * 
 * @author Jikoo
 */
public class MegaHal {

	private Pattern exactPattern, whitespacePattern;
	private JMegaHal hal;
	private Set<String> pendingMessages;
	private Map<String, Long> ratelimit;
	private HalLogSavingTask save;
	private Set<Pattern> ignoreMatches;
	private int fileNum;

	public MegaHal() {
		hal = new JMegaHal();
		fileNum = 0;

		String regexBase = RegexUtils.ignoreCaseRegex("hal", "dirk");
		exactPattern = Pattern.compile(createExactRegex(regexBase));
		whitespacePattern = Pattern.compile(createWhitespaceRegex(regexBase));
		pendingMessages = Collections.synchronizedSet(new LinkedHashSet<String>());

		ratelimit = new ConcurrentHashMap<>();

		ignoreMatches = new HashSet<>();
		ignoreMatches.add(Pattern.compile("^.*[Bb]([Ee]|[Aa])[Nn][Zz]([Uu]|[Ee])?[Rr][Ff]([Ll][Ee][Ss]?)?.*$"));
		ignoreMatches.add(Pattern.compile("^[Hh][Aa]Ll][Cc]([Uu][Ll][Aa][Tt][Ee])? .*$"));

		loadHal();

		save = new HalLogSavingTask();
		save.runTaskTimer(Sblock.getInstance(), 6000L, 6000L);
	}

	private String createExactRegex(String regexBase) {
		StringBuilder regex = new StringBuilder().append("\\A");
		regex.append(regexBase);
		regex.append("\\Z");
		return regex.toString();
	}

	private String createWhitespaceRegex(String regexBase) {
		StringBuilder regex = new StringBuilder().append("(\\W|\\A)");
		regex.append(regexBase);
		regex.append("(\\W|\\Z|\\z)");
		return regex.toString();
	}

	public void handleMessage(Message msg) {
		if (isTrigger(msg.getConsoleMessage())) {
			if (isOnlyTrigger(msg.getConsoleMessage())) {
				// Set sender on fire or some shit
				msg.getSender().sendMessage(ColorDef.HAL.replaceFirst("#", msg.getChannel().getName()) + "What?");
				return;
			}
			if (msg.getSender() == null || msg.getChannel().getAccess() == AccessLevel.PRIVATE
					|| msg.getChannel().getType() == ChannelType.NICK || msg.getChannel().getType() == ChannelType.RP) {
				return;
			}
			String channel = msg.getChannel().getName();
			if (!channel.equals("#halchat")) {
				if (ratelimit.containsKey(channel) && ratelimit.get(msg.getChannel().getName()) > System.currentTimeMillis()) {
					// Still on cooldown, warn a bitch
					msg.getSender().getPlayer().sendMessage(ColorDef.HAL.replaceFirst("#", channel) + "If you want to spam with me, run /sc c #halchat");
					Bukkit.getConsoleSender().sendMessage("Warned " + msg.getSender().getPlayerName() + " about spamming Hal");
					return;
				} else {
					ratelimit.put(channel, System.currentTimeMillis() + 1500L);
				}
			}
			triggerResponse(msg.getChannel(), msg.getConsoleMessage(), true);
		} else {
			log(msg);
		}
	}

	public boolean isTrigger(String message) {
		return whitespacePattern.matcher(message).find();
	}

	public boolean isOnlyTrigger(String message) {
		return exactPattern.matcher(message).find();
	}

	public void log(Message message) {
		if (message.containsLinks() || !message.escape() || message.getChannel().getAccess() == AccessLevel.PRIVATE
				|| message.getChannel().getType() == ChannelType.NICK || message.getChannel().getType() == ChannelType.RP) {
			return;
		}
		log(message.getConsoleMessage());
	}

	public synchronized void log(String message) {
		message = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message));
		if (message.isEmpty() || message.length() < 15 || message.startsWith("((") || isTrigger(message)) {
			return;
		}
		// TODO strip more stuff we don't want
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

	public synchronized void triggerResponse(final Channel channel, final String message, final boolean filter) {
		new BukkitRunnable() {
			@Override
			public void run() {
				String word;
				if (filter) {
					word = selectRandomWord(message);
				} else {
					word = message;
				}
				Message msg = new Message("Lil Hal", word == null ? hal.getSentence() : hal.getSentence(word));
				msg.setChannel(channel);
				msg.addColor(ChatColor.RED);
				msg.send();
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
