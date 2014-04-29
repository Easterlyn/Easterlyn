package co.sblock.chat.chester;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.gomeow.chester.API.ChesterBroadcastEvent;
import info.gomeow.chester.API.ChesterLogEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import co.sblock.CommandListener;
import co.sblock.chat.ColorDef;
import co.sblock.chat.ChannelManager;
import co.sblock.users.User;
import co.sblock.utilities.Log;
import co.sblock.utilities.regex.RegexUtils;

/**
 * @author Jikoo
 */
public class ChesterListener implements CommandListener, Listener {

	private int cancels = 0;

	private Pattern pattern, whitespacePattern;

	private List<String> triggers;

	public ChesterListener() {
		triggers = Bukkit.getPluginManager().getPlugin("Chester").getConfig().getStringList("triggerwords");
		pattern = Pattern.compile(RegexUtils.ignoreCaseRegex(triggers.toArray(new String[0])));
		whitespacePattern = Pattern.compile(createRegex());
		Log.getLog("ChesterListener").info("Compiled regex: " + pattern.toString());
	}

	private String createRegex() {
		StringBuilder regex = new StringBuilder().append("(\\W|\\A)");
		regex.append(pattern.toString());
		regex.append("(\\W|\\Z|\\z)");
		return regex.toString();
	}

	@EventHandler
	public void onChesterLog(ChesterLogEvent event) {
		User user = User.getUser(event.getPlayer().getUniqueId());
		if (user == null ||user.getCurrent() == null || user.isMute() || user.isSuppressing()) {
			stopLogging(event);
			return;
		}

		if (!user.getCurrent().getName().equals("#")) {
			if (!event.getMessage().startsWith("@# ")) {
				stopLogging(event);
				return;
			}
		} else {
			if (event.getMessage().charAt(0) == '@' && !event.getMessage().startsWith("@# ")) {
				stopLogging(event);
				return;
			}
		}

		if (event.getMessage().startsWith("@# ") && event.getMessage().length() > 3) {
			event.setMessage(event.getMessage().substring(3));
		}

		for (String s : triggers) {
			if (event.getMessage().equalsIgnoreCase(s)) {
				Log.getLog("ChesterListener").info("Not logging: Chat equals trigger \"" + event.getMessage() + "\"");
				event.setCancelled(true);
				return;
			}
		}

		stopIndirectTrigger(event);
	}

	private void stopLogging(ChesterLogEvent event) {
		Matcher m = pattern.matcher(event.getMessage());
		if (m.find()) {
			cancels++;
			Log.getLog("ChesterListener").info("Match found: " + m.group() + ". Cancels: " + cancels);
		}
		event.setCancelled(true);
	}

	private void stopIndirectTrigger(ChesterLogEvent event) {
		Matcher m = pattern.matcher(event.getMessage());
		if (m.find() && !whitespacePattern.matcher(event.getMessage()).find()) {
			cancels++;
			Log.getLog("ChesterListener").info("Inexact match found: " + m.group() + ". Cancels: " + cancels);
		}
	}

	@EventHandler
	public void onChesterTalk(ChesterBroadcastEvent event) {
		// No matter the outcome, we do not want to use the inbuilt chat feature.
		event.getRecipients().clear();
		if (cancels > 0) {
			cancels--;
			Log.getLog("ChesterListener").info("Chat cancelled when logged. " + cancels + " cancels remain.");
			return;
		}

		String message = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', event.getMessage()));
		if (message.startsWith("#>")) {
			int space = message.indexOf(' ');
			if (space == -1) {
				return;
			}
			message = ColorDef.HAL_ME + message.substring(space);
		} else {
			message = ColorDef.HAL + message;
		}

		// Allows Hal to highlight players
		ChannelManager.getChannelManager().getChannel("#").sendToAll(null, message, false);
	}
}
