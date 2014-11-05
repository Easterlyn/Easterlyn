package co.sblock.chat.ai;

import java.io.File;
import java.io.FileWriter;
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
import co.sblock.chat.SblockChat;
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

	public MegaHal() {
		hal = new JMegaHal();

		String regexBase = RegexUtils.ignoreCaseRegex("hal", "dirk");
		exactPattern = Pattern.compile(createExactRegex(regexBase));
		Log.getLog("MegaHal").info("Compiled exact regex: " + exactPattern.toString());
		whitespacePattern = Pattern.compile(createWhitespaceRegex(regexBase));
		Log.getLog("MegaHal").info("Compiled whitespace regex: " + whitespacePattern.toString());
		pendingMessages = Collections.synchronizedSet(new LinkedHashSet<String>());

		ratelimit = new ConcurrentHashMap<>();

		save = new HalLogSavingTask();
		save.runTaskTimer(Sblock.getInstance(), 600L, 600L);

		ignoreMatches = new HashSet<>();
		ignoreMatches.add(Pattern.compile("^.*[Bb]([Ee]|[Aa])[Nn][Zz]([Uu]|[Ee])?[Rr][Ff]([Ll][Ee][Ss]?)?.*$"));
		ignoreMatches.add(Pattern.compile("^[Hh][Aa]Ll][Cc]([Uu][Ll][Aa][Tt][Ee])? .*$"));

		loadHal();
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
			triggerResponse(msg.getChannel(), msg.getConsoleMessage());
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

	public void log(String message) {
		message = ChatColor.stripColor(message);
		// TODO strip more stuff we don't want
		for (Pattern pattern : ignoreMatches) {
			if (pattern.matcher(message).find()) {
				return;
			}
		}
		if (message.isEmpty() || message.startsWith("((") || isTrigger(message)) {
			return;
		}
		pendingMessages.add(message);
		if (save.getTaskId() == -1) {
			Log.getLog("MegaHal").warning("Log saving task was stopped! Restarting...");
			save.runTaskTimer(Sblock.getInstance(), 0L, 600L);
		}
		hal.add(message);
	}

	public void triggerResponse(final Channel channel, final String message) {
		new BukkitRunnable() {
			@Override
			public void run() {
				String word = selectRandomWord(message);
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
		File halFile = new File(Sblock.getInstance().getDataFolder(), "hal.log");
		try (PrintWriter writer = new PrintWriter(new FileWriter(halFile, true))) {
			for (String s : pendingMessages) {
				writer.println(s);
			}
			writer.close();
			pendingMessages.clear();
		} catch (IOException e) {
			Log.getLog("MegaHal").err(e);
		}
	}

	public void loadHal() {

		File chester = new File("plugins/Chester/brain.chester");
		if (chester.exists()) {
			File chesterBackup = new File("plugins/Chester/brain.backup");
			if (chesterBackup.exists()) {
				Log.getLog("MegaHal").warning("Chester backup already exists, please manually fix yo shit.");
			} else {
				try {
					FileUtils.copyFile(chester, chesterBackup);
					List<String> chesterLogs = FileUtils.readLines(chester);
					Channel hash = SblockChat.getChat().getChannelManager().getChannel("#");
					for (String string : chesterLogs) {
						Message message = new Message("Conversion", string);
						message.setChannel(hash);
						message.prepare();
						log(message);
					}
					saveLogs();
				} catch (IOException e) {
					Log.getLog("MegaHal").err(e);
				}
				chester.delete();
			}
		}

		File halFile = new File(Sblock.getInstance().getDataFolder(), "hal.log");
		if (halFile.exists()) {
			try {
				List<String> halLogs = FileUtils.readLines(halFile);
				for (String string : halLogs) {
					hal.add(string);
				}
			} catch (IOException e) {
				Log.getLog("MegaHal").err(e);
			}
		}
	}
}
