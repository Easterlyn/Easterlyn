package co.sblock.chat.chester;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import info.gomeow.chester.API.ChesterBroadcastEvent;
import info.gomeow.chester.API.ChesterLogEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import co.sblock.chat.Message;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.users.User;
import co.sblock.utilities.Log;
import co.sblock.utilities.regex.RegexUtils;

/**
 * @author Jikoo
 */
public class ChesterListener implements Listener {

	private Pattern pattern, whitespacePattern;

	private List<String> triggers;

	private List<Channel> pendingResponses;

	public ChesterListener() {
		triggers = Bukkit.getPluginManager().getPlugin("Chester").getConfig().getStringList("triggerwords");
		pattern = Pattern.compile(RegexUtils.ignoreCaseRegex(triggers.toArray(new String[0])));
		whitespacePattern = Pattern.compile(createRegex());
		Log.getLog("ChesterListener").info("Compiled regex: " + pattern.toString());
		pendingResponses = new ArrayList<>();
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
			event.setCancelled(true);
			return;
		}

		Message sentMessage = new Message(User.getUser(event.getPlayer().getUniqueId()), event.getMessage());

		if (!sentMessage.validate(false)
				|| sentMessage.getChannel().getAccess() == AccessLevel.PRIVATE
				|| sentMessage.getChannel().getType() == ChannelType.RP
				|| sentMessage.getChannel().getType() == ChannelType.NICK
				|| sentMessage.getMessage().isEmpty()) {
			event.setCancelled(true);
			return;
		}

		for (String s : triggers) {
			if (sentMessage.getMessage().equalsIgnoreCase(s)) {
				event.setCancelled(true);
			}
		}

		// No need to allow @channel or escape formatting to persist.
		event.setMessage(ChatColor.stripColor(sentMessage.getMessage()));

		// If Chester isn't triggered, we're done.
		if (!pattern.matcher(event.getMessage()).find()) {
			return;
		}

		// Because Chester keeps logging blank lines, freeze all logging that actually contains triggers.
		event.setCancelled(true);

		// Stops indirect triggers, e.g. "HALlway"
		if (!whitespacePattern.matcher(event.getMessage()).find()) {
			return;
		}

		pendingResponses.add(sentMessage.getChannel());
	}

	@EventHandler
	public void onChesterTalk(ChesterBroadcastEvent event) {
		// No matter the outcome, we do not want to use the inbuilt chat feature.
		event.getRecipients().clear();
		if (pendingResponses.size() == 0) {
			return;
		}

		// Allows Hal to highlight players
		Message m = new Message("Lil Hal", ChatColor.RED
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', event.getMessage())));
		m.setChannel(pendingResponses.get(0));
		m.addColor(ChatColor.RED);
		if (m.validate(false)) {
			m.send();
		}

		pendingResponses.remove(0);
	}

	public static List<String> getTriggers() {
		return Bukkit.getPluginManager().getPlugin("Chester").getConfig().getStringList("triggerwords");
	}
}
