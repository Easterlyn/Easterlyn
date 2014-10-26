package co.sblock.chat.ai;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import net.minecraft.util.org.apache.commons.io.FileUtils;

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
import co.sblock.utilities.threadsafe.SetGenerator;

/**
 * Sblock's JMegaHal implementation - Chester is just too buggy and difficult to customize.
 * 
 * @author Jikoo
 */
public class MegaHal {

	private Pattern exactPattern, whitespacePattern;
	private JMegaHal hal;
	private Set<String> pendingMessages;
	private HalLogSavingTask save;

	public MegaHal() {
		hal = new JMegaHal();

		String regexBase = RegexUtils.ignoreCaseRegex("hal", "dirk");
		exactPattern = Pattern.compile(createExactRegex(regexBase));
		Log.getLog("MegaHal").info("Compiled exact regex: " + exactPattern.toString());
		whitespacePattern = Pattern.compile(createWhitespaceRegex(regexBase));
		Log.getLog("MegaHal").info("Compiled whitespace regex: " + whitespacePattern.toString());
		pendingMessages = SetGenerator.generate();

		save = new HalLogSavingTask();
		save.runTaskTimer(Sblock.getInstance(), 600L, 600L);

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
			triggerResponse(msg.getChannel(), msg.getConsoleMessage());
		} else {
			if (msg.getChannel().getType() == ChannelType.RP || msg.getChannel().getType() == ChannelType.NICK
					|| msg.getChannel().getAccess() == AccessLevel.PRIVATE) {
				return;
			}
			log(msg.getConsoleMessage());
		}
	}

	public boolean isTrigger(String message) {
		return whitespacePattern.matcher(message).find();
	}

	public boolean isOnlyTrigger(String message) {
		return exactPattern.matcher(message).find();
	}

	public void log(String message) {
		message = ChatColor.stripColor(message);
		// TODO strip more stuff we don't want
		// Most strips are purely for the sake of handling conversion from Chester's logs and will be removed post-release
		if (message.isEmpty() || message.startsWith("((") || message.contains("benzrf")
				|| message.matches("^[Hh][Aa]Ll][Cc]([Uu][Ll][Aa][Tt][Ee])? .*$")
				|| isTrigger(message)) {
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
				Message msg = new Message("Lil Hal", word == null ? hal.getSentence(): hal.getSentence(word));
				msg.setChannel(channel);
				msg.send();
			}
		}.runTaskAsynchronously(Sblock.getInstance());
	}

	private String selectRandomWord(String message) {
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
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(halFile));
			for (String s : pendingMessages) {
				writer.write(s + '\n');
			}
			writer.close();
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
					for (String string : chesterLogs) {
						log(string);
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
				List<String> halLogs = FileUtils.readLines(chester);
				for (String string : halLogs) {
					hal.add(string);
				}
			} catch (IOException e) {
				Log.getLog("MegaHal").err(e);
			}
		}
	}
}
