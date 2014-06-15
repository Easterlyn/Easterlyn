package co.sblock.chat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import co.sblock.chat.channel.CanonNicks;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.users.User;
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
	private boolean escape;
	private boolean thirdPerson;
	private Set<ChatColor> colors;

	public Message(User sender, String message) {
		this(message);
		this.sender = sender;
	}

	public Message(String name, String message) {
		this(message);
		this.name = name;
	}

	private Message(String message) {

		// TODO make this less order-specific, e.g. allow @# #>
		thirdPerson = message.startsWith("#>");
		if (thirdPerson) {
			message = message.substring(2);
		}

		escape = message.length() > 0 && message.charAt(0) != '\\';
		if (!escape) {
			message = message.substring(1);
		}

		this.message = message;

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

		int space = message.indexOf(' ');
		// Check for @<channel> destination
		if (message.charAt(0) == '@' && space > 1) {
			String target = message.substring(1, space);
			message = message.substring(space);
			// If channel was already set, probably being overridden by Chester.
			if (channel == null) {
				channel = ChannelManager.getChannelManager().getChannel(target);
				if (channel == null) {
					if (notify) {
						sender.sendMessage(ChatMsgs.errorInvalidChannel(target));
					}
					return false;
				}
			}
		} else if (sender != null) {
			channel = sender.getCurrent();
			if (channel == null) {
				if (notify) {
					sender.sendMessage(ChatMsgs.errorNoCurrent());
				}
				return false;
			}
		}

		if (sender == null) {
			if (name != null) {
				return true;
			}
			return false;
		}

		// No sending messages to global chats while ignoring them
		if (channel.getType() == ChannelType.REGION && sender.isSuppressing()) {
			if (notify) {
				sender.sendMessage(ChatMsgs.errorSuppressingGlobal());
			}
			return false;
		}

		// Must be in target channel to send messages
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

		// Trim whitespace created by formatting codes, etc.
		message = RegexUtils.trimExtraWhitespace(message);
		if (message.length() > 1 && RegexUtils.appearsEmpty(message.substring(0 , 2).equals("#>") ? message.substring(2) : message)) {
			return;
		}

		// Get channel formatting
		String channelPrefixing = channel.formatMessage(sender, thirdPerson);
		if (sender == null) {
			channelPrefixing = channelPrefixing.replaceFirst("<nonhuman>", name);
		}
		MessageElement rawMsg = new MessageElement(channelPrefixing, colors.toArray(new ChatColor[0]));

		// Send console chat message
		if (channel.getType() != ChannelType.REGION) {
			Bukkit.getConsoleSender().sendMessage(channelPrefixing + message);
		}

		// Create raw message and wrap links Minecraft would recognize
		message = wrapLinks(rawMsg, message);

		for (UUID uuid : channel.getListening()) {
			User u = User.getUser(uuid);
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
		Matcher match = Pattern.compile("(https?://)?(([\\w]+\\.)+([a-zA-Z]{2,4}))((#|/).*\\b)?").matcher(message);
		int lastEnd = 0;
		String lastColor = new String();
		while (match.find()) {
			rawMsg.addExtra(processMessageSegment(lastColor + message.substring(lastEnd, match.start())));
			lastColor = ChatColor.getLastColors(rawMsg.toString());
			String url = match.group();
			// If URL does not start with http:// or https:// the client will crash. Client autofills this for normal links.
			if (!match.group().matches("https?://.*")) {
				url = "http://" + url;
			}
			rawMsg.addExtra(new EscapedElement("[" + match.group(2) + "]", ChatColor.BLUE)
					.addClickEffect(new MessageClick(MessageClick.ClickEffect.OPEN_URL, url))
					.addHoverEffect(new MessageHover(MessageHover.HoverEffect.SHOW_TEXT, url)));
			lastEnd = match.end();
		}
		rawMsg.addExtra(processMessageSegment(lastColor + message.substring(lastEnd)));
		return rawMsg.toString();
	}

	private MessageElement processMessageSegment(String substring) {
		MessageElement msg;
		// TODO allow links to not be unreadable by quirking here
		// Could do this more cleanly with casting, but ifs will work for now.
		if (sender != null && channel.getType() == ChannelType.RP) {
			CanonNicks nick = CanonNicks.getNick(channel.getNick(sender));
			if (escape) {
				msg = new EscapedElement(nick.applyQuirk(substring), nick.getColor());
			} else {
				msg = new MessageElement(nick.applyQuirk(substring), nick.getColor());
			}
		} else {
			if (sender != null && channel.isChannelMod(sender)) {
				// Colors for channel mods!
				substring = ChatColor.translateAlternateColorCodes('&', substring);
			}
			if (escape) {
				msg = new EscapedElement(substring, colors.toArray(new ChatColor[0]));
			} else {
				msg = new MessageElement(substring, colors.toArray(new ChatColor[0]));
			}
		}
		return msg;
	}
}
