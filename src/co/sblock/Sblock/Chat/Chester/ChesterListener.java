package co.sblock.Sblock.Chat.Chester;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.gomeow.chester.API.ChesterBroadcastEvent;
import info.gomeow.chester.API.ChesterLogEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.UserData.ChatData;
import co.sblock.Sblock.UserData.User;
import co.sblock.Sblock.Utilities.Log;
import co.sblock.Sblock.Utilities.Regex.RegexUtils;

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
		User c = User.getUser(event.getPlayer().getUniqueId());
		if (c == null || ChatData.getCurrent(c) == null) {
			stopLogging(event);
			return;
		}

		if (!ChatData.getCurrent(c).getName().equals("#")) {
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
		if (cancels > 0) {
			event.getRecipients().clear();
			cancels--;
			Log.getLog("ChesterListener").info("Chat cancelled when logged. " + cancels + " cancels remain.");
			return;
		}
		Channel c = ChannelManager.getChannelManager().getChannel("#");
		if (c == null) {
			event.getRecipients().clear();
			return;
		}
		Set<Player> listeners = new HashSet<Player>();
		for (UUID userID : c.getListening()) {
			listeners.add(Bukkit.getPlayer(userID));
		}
		event.getRecipients().retainAll(listeners);
		event.setMessage(ChatColor.stripColor(event.getMessage()));
	}
}
