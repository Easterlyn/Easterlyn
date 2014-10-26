package co.sblock.chat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import co.sblock.chat.channel.CanonNicks;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.rawmessages.EscapedElement;
import co.sblock.utilities.rawmessages.MessageClick;
import co.sblock.utilities.rawmessages.MessageElement;
import co.sblock.utilities.rawmessages.MessageHover;
import co.sblock.utilities.regex.RegexUtils;

/**
 * Used to better clarify a message's destination prior to formatting.
 * 
 * @author Jikoo
 */
public class Message {

	private User sender;
	private String name;
	private Channel channel;
	private String message;
	private String consoleFriendly;
	private boolean escape;
	private boolean thirdPerson;
	private String target;
	private Set<ChatColor> colors;

	public Message(User sender, String message) {
		this(message);
		this.sender = sender;
		if (channel == null && target == null && sender != null) {
			channel = sender.getCurrent();
		}
	}

	public Message(String name, String message) {
		this(message);
		this.name = name;
	}

	private Message(String message) {

		thirdPerson = message.startsWith("#>");
		if (thirdPerson) {
			message = message.substring(2);
		}

		escape = message.length() > 2 && message.charAt(0) != '\\' && message.charAt(1) != '\\';
		if (message.length() > 0 && !escape) {
			message = message.substring(2);
		}

		int space = message.indexOf(' ');
		// Check for @<channel> destination
		if (space > 1 && message.charAt(0) == '@') {
			target = message.substring(1, space);
			message = message.substring(space);
			channel = ChannelManager.getChannelManager().getChannel(target);
		}

		// Trim whitespace created by formatting codes, etc.
		message = RegexUtils.trimExtraWhitespace(message);

		this.message = message;
		this.consoleFriendly = message;

		this.colors = new HashSet<>();
	}

	public User getSender() {
		return sender;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getMessage() {
		return message;
	}

	public String getConsoleMessage() {
		return consoleFriendly;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void addColor(ChatColor color) {
		colors.add(color);
	}

	public boolean validate(boolean notify) {
		if (sender == null) {
			notify = false;
		}

		if (channel == null) {
			if (notify) {
				if (target != null) {
					sender.sendMessage(ChatMsgs.errorInvalidChannel(target));
				} else {
					sender.sendMessage(ChatMsgs.errorNoCurrent());
				}
			}
			return false;
		}

		// No sending of blank messages.
		if (RegexUtils.appearsEmpty(message)) {
			if (notify) {
				sender.sendMessage(ChatMsgs.errorEmptyMessage());
			}
			return false;
		}

		// No sender and no name, invalid message.
		if (sender == null) {
			return name != null;
		}

		// No sending messages to global chats while ignoring them.
		if (channel.getType() == ChannelType.REGION && sender.isSuppressing()) {
			if (notify) {
				sender.sendMessage(ChatMsgs.errorSuppressingGlobal());
			}
			return false;
		}

		// Must be in target channel to send messages.
		if (!channel.getListening().contains(sender.getUUID())) {
			if (notify) {
				sender.sendMessage(ChatMsgs.errorNotListening(channel.getName()));
			}
			return false;
		}

		// Nicks required in RP channels.
		if (channel.getType() == ChannelType.RP && !channel.hasNick(sender)) {
			if (notify) {
				sender.sendMessage(ChatMsgs.errorNickRequired(channel.getName()));
			}
			return false;
		}

		return true;
	}

	public void send() {

		// Check if the message is valid
		if (sender == null && name == null || channel == null) {
			return;
		}

		// Get channel formatting
		String channelPrefixing = channel.formatMessage(sender, thirdPerson);
		if (sender == null) {
			channelPrefixing = channelPrefixing.replaceFirst("<nonhuman>", name);
		} else if (sender.getPlayer().hasPermission("sblockchat.color")) {
			Player player = sender.getPlayer();
			for (ChatColor c : ChatColor.values()) {
				if (player.hasPermission("sblockchat." + c.name().toLowerCase())) {
					colors.add(c);
				}
			}
		}
		MessageElement rawMsg = new MessageElement(channelPrefixing, colors.toArray(new ChatColor[0]));

		// Send console chat message
		if (channel.getType() != ChannelType.REGION) {
			Bukkit.getConsoleSender().sendMessage(channelPrefixing + getConsoleMessage());
		}

		// Create raw message and wrap links Minecraft would recognize
		message = wrapLinks(rawMsg, message);

		for (UUID uuid : channel.getListening()) {
			User u = UserManager.getUser(uuid);
			if (u == null) {
				channel.removeListening(uuid);
				continue;
			}
			if (channel.getType() == ChannelType.REGION && u.isSuppressing()) {
				continue;
			}
			if (sender != null && sender.equals(u)) {
				// No self-highlight.
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + u.getPlayerName() + " " + message);
			} else {
				u.rawHighlight(message, channel.getNick(u));
			}
		}
	}

	private String wrapLinks(MessageElement rawMsg, String message) {
		Matcher match = Pattern.compile("((https?://)?(([\\w-_]+\\.)+([a-zA-Z]{2,4}))((#|/)\\S*)?)(\\s|\\z)").matcher(message);
		int lastEnd = 0;
		String lastColor = new String();
		while (match.find()) {
			rawMsg.addExtra(processMessageSegment(lastColor + message.substring(lastEnd, match.start())));
			lastColor = ChatColor.getLastColors(rawMsg.toString());
			String url = match.group(1);
			// If URL does not start with http:// or https:// the client will crash. Client autofills this for normal links.
			if (!match.group().matches("https?://.*")) {
				url = "http://" + url;
			}
			rawMsg.addExtra(new EscapedElement("[" + match.group(3) + "]" + match.group(8), ChatColor.BLUE)
					.addClickEffect(new MessageClick(MessageClick.ClickEffect.OPEN_URL, url))
					.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT, url)));
			lastEnd = match.end();
		}
		rawMsg.addExtra(processMessageSegment(lastColor + message.substring(lastEnd)));
		return rawMsg.toString();
	}

	private MessageElement processMessageSegment(String substring) {
		MessageElement msg = null;;
		// Could do this more cleanly with casting, but ifs will work for now.
		if (sender != null && channel.getType() == ChannelType.RP) {
			CanonNicks nick = CanonNicks.getNick(channel.getNick(sender));
			if (escape) {
				msg = new EscapedElement(nick.applyQuirk(substring), nick.getColor());
			} else {
				msg = new MessageElement(nick.applyQuirk(substring), nick.getColor());
			}
		} else {
			if (sender != null && channel.isModerator(sender)) {
				// Colors for channel mods!
				// TODO new MessageElement per new color to prevent color reset on wrap
//				Matcher matcher = Pattern.compile("&[0-9A-FK-NRa-fk-nr]").matcher(substring);
//				int lastEnd = 0;
//				MessageElement latestMsg = null;
//				while (matcher.find()) {
//					if (msg == null) {
//						msg = escape ? new EscapedElement(substring.substring(0, matcher.start()))
//								: new MessageElement(substring.substring(0, matcher.start()));
//						
//					}
//				}

				substring = ChatColor.translateAlternateColorCodes('&', substring);
				msg = escape ? new EscapedElement(substring) : new MessageElement(substring);
			} else {
				msg = escape ? new EscapedElement(substring) : new MessageElement(substring);
			}
		}
		return msg;
	}
}