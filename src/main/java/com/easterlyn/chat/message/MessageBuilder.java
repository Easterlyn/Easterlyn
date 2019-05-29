package com.easterlyn.chat.message;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.ChannelManager;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.Language;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.NickChannel;
import com.easterlyn.chat.channel.RegionChannel;
import com.easterlyn.users.User;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.JSONUtil;
import com.easterlyn.utilities.TextUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder for Messages. In most cases, set sender, channel, and finally message. In the event that
 * channel must be fixed, set it last to prevent @#channel changing. However, in the case of nick
 * channels, this can result in chat colors not being allowed when they should be.
 *
 * @author Jikoo
 */
public class MessageBuilder {

	private static final TextComponent HIGHLIGHTED_BRACKET;
	private static final String CONSOLE_FORMAT, CONSOLE_FORMAT_THIRD;
	private static final Pattern NAME_PATTERN, REAL_NAME_PATTERN, RANK_PATTERN, CLASS_PATTERN, ASPECT_PATTERN;
	private static BaseComponent[] NAME_HOVER;

	static {
		HIGHLIGHTED_BRACKET = new TextComponent("!!");
		HIGHLIGHTED_BRACKET.setColor(ChatColor.AQUA);

		CONSOLE_FORMAT = "%1$s[%2$s%3$s%1$s]%4$s <%5$s%6$s%4$s> " + ChatColor.WHITE + "%6$s";
		CONSOLE_FORMAT_THIRD = CONSOLE_FORMAT.replace(">", "").replace(" <", "> ");

		NAME_PATTERN = Pattern.compile("\\{PLAYER}");
		REAL_NAME_PATTERN = Pattern.compile("\\{REALNAME}");
		RANK_PATTERN = Pattern.compile("\\{RANK}");
		CLASS_PATTERN = Pattern.compile("\\{CLASS}");
		ASPECT_PATTERN = Pattern.compile("\\{ASPECT}"); // TODO -> Affinity
	}

	private final Language lang;
	private final ChannelManager manager;

	private User sender = null;
	private String senderName = null;
	private Channel channel = null;
	private String message = null;
	private boolean thirdPerson = false;
	private String atChannel = null;
	private String channelClick, nameClick;
	private TextComponent[] messageComponents;
	private BaseComponent[] nameHover;

	public MessageBuilder(Easterlyn plugin) {
		this.lang = plugin.getModule(Language.class);
		this.manager = plugin.getModule(Chat.class).getChannelManager();
		this.setup();
	}

	private void setup() {
		if (NAME_HOVER == null) {
			NAME_HOVER = TextComponent.fromLegacyText(lang.getValue("chat.user.hover"));
		}
	}

	public MessageBuilder setSender(User sender) {
		this.sender = sender;
		this.senderName = sender.getDisplayName();
		if (this.channel == null) {
			this.channel = sender.getCurrentChannel();
		}
		return this;
	}

	public MessageBuilder setSender(String name) {
		this.senderName = name;
		this.sender = null;
		return this;
	}

	public MessageBuilder setChannel(Channel channel) {
		this.channel = channel;
		return this;
	}

	public MessageBuilder setMessage(TextComponent... messageComponents) {
		return setMessage(TextComponent.toPlainText(messageComponents), messageComponents);
	}

	public MessageBuilder setMessage(String message, TextComponent... messageComponents) {
		if (messageComponents.length == 0) {
			throw new IllegalArgumentException("Message components must exist!");
		}
		if (message == null) {
			throw new IllegalArgumentException("Message cannot be null!");
		}
		this.messageComponents = messageComponents;
		this.message = message;
		return this;
	}

	public MessageBuilder setMessage(String message) {
		if (message == null) {
			throw new IllegalArgumentException("Message cannot be null!");
		}
		if (this.messageComponents != null) {
			this.messageComponents = null;
		}

		// Set @<channel> destination
		this.atChannel = null;
		int space = message.indexOf(' ');
		if (space > 1 && message.charAt(0) == '@') {
			this.atChannel = message.substring(1, space);
			message = message.substring(space);
			this.channel = manager.getChannel(this.atChannel);
		}

		// Channel mods can use colors in any channel, anyone can use color codes in nick channels.
		if (channel != null && (channel instanceof NickChannel
				|| sender != null && channel.isModerator(sender))) {
			message = ChatColor.translateAlternateColorCodes('&', message);
		}

		// Trim whitespace created by formatting codes, etc.
		message = TextUtils.trimExtraWhitespace(message);

		this.message = message;
		return this;
	}

	public MessageBuilder setThirdPerson(boolean thirdPerson) {
		this.thirdPerson = thirdPerson;
		return this;
	}


	/**
	 * Sets Message display name tooltip text.
	 * <p>
	 * If the tooltip will not change between uses, it is preferred that you use
	 * {@link MessageBuilder#setNameHover(BaseComponent...)} and store the value.
	 *
	 * @param hover the String to display
	 *
	 * @return the MessageBuilder
	 */
	public MessageBuilder setNameHover(String hover) {
		this.nameHover = TextComponent.fromLegacyText(hover);
		return this;
	}

	/**
	 * Sets Message display name tooltip text.
	 *
	 * @param hover the TextComponent to display
	 *
	 * @return the MessageBuilder
	 */
	public MessageBuilder setNameHover(BaseComponent... hover) {
		this.nameHover = hover;
		return this;
	}

	public MessageBuilder setNameClick(String suggestion) {
		this.nameClick = suggestion;
		return this;
	}

	public MessageBuilder setChannelClick(String suggestion) {
		this.channelClick = suggestion;
		return this;
	}

	public boolean canNotBuild(boolean informSender) {
		informSender = this.sender != null && informSender;

		// Channel must exist
		if (this.channel == null) {
			if (informSender && this.atChannel != null) {
				this.sender.sendMessage(this.lang.getValue("chat.error.invalidChannel").replace("{CHANNEL}", this.atChannel));
			} else if (informSender) {
				this.sender.sendMessage(this.lang.getValue("chat.error.noCurrentChannel"));
			}
			return true;
		}

		// No sending of blank messages.
		if (TextUtils.appearsEmpty(this.message)) {
			if (informSender) {
				this.sender.sendMessage(this.lang.getValue("chat.error.emptyMessage"));
			}
			return true;
		}

		// No sender and no name, invalid message.
		if (this.sender == null) {
			return this.senderName == null;
		}

		// No sending messages to global chats while ignoring them.
		if (this.channel instanceof RegionChannel && this.sender.getSuppression()) {
			if (informSender) {
				this.sender.sendMessage(ChatColor.RED
						+ "You cannot talk in a global channel while suppressing!\nUse "
						+ ChatColor.AQUA + "/suppress" + ChatColor.RED + " to toggle.");
			}
			return true;
		}

		return false;
	}

	public boolean isSenderNotInChannel(boolean informSender) {
		// Must be in target channel to send messages.
		if (sender == null || this.channel.getListening().contains(this.sender.getUUID())) {
			return false;
		}
		if (informSender) {
			this.sender.sendMessage(ChatColor.RED + "You are not listening to " + ChatColor.GOLD + channel.getName());
		}
		return true;
	}

	public Message toMessage() {
		if (canNotBuild(false)) {
			throw new RuntimeException("Someone did something stupid with chat!");
		}

		Player player = sender != null ? sender.getPlayer() : null;

		// Greentext must be at least 4 letters long and the second character must be a letter.
		// E.G. >mfw people do it wrong
		// instead of > lol le edgy meme
		if (message.length() > 3 && message.charAt(0) == '>' && Character.isLetter(message.charAt(1))
				&& (player == null || player.hasPermission("easterlyn.chat.greentext"))) {
			message = ChatColor.GREEN + message;
		}

		// Prepend chat colors to every message if sender has permission
		if (player != null && player.hasPermission("easterlyn.chat.color")) {
			for (ChatColor c : ChatColor.values()) {
				if (player.hasPermission("easterlyn.chat.color." + c.name().toLowerCase())) {
					message = c + message;
					break;
				}
			}
		}

		// CHANNEL ELEMENT: [#channel]
		ChatColor channelBracket;
		if (player != null && player.hasPermission("easterlyn.guildleader")) {
			channelBracket = sender.getUserAffinity().getColor();
		} else {
			channelBracket = ChatColor.WHITE;
		}
		ChatColor channelRank = channel.isOwner(sender) ? Language.getColor("channel.owner")
				: channel.isModerator(sender) ? Language.getColor("channel.mod") : Language.getColor("channel.member");

		LinkedList<TextComponent> channelHighlightComponents = new LinkedList<>();
		LinkedList<TextComponent> components = new LinkedList<>();

		// [ | !!
		channelHighlightComponents.add(HIGHLIGHTED_BRACKET);
		TextComponent component = new TextComponent("[");
		component.setColor(channelBracket);
		components.add(component);

		// [#channel | !!#channel
		component = new TextComponent(channel.getName());
		component.setColor(channelRank);
		channelHighlightComponents.add(component);
		components.add(component);

		// [#channel] | !!#channel!!
		channelHighlightComponents.add(HIGHLIGHTED_BRACKET);
		component = new TextComponent("]");
		component.setColor(channelBracket);
		components.add(component);

		TextComponent channelComponent = new TextComponent(components.toArray(new BaseComponent[0]));
		if (channelClick == null) {
			channelClick = '@' + channel.getName() + ' ';
		}
		channelComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, channelClick));
		TextComponent channelHighlightComponent = new TextComponent(channelHighlightComponents.toArray(new BaseComponent[0]));
		channelHighlightComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, channelClick));

		components.clear();

		// NAME ELEMENT: <Name>
		ChatColor region = ChatColor.WHITE;

		// > | <
		component = new TextComponent(thirdPerson ? "> " : " <");
		component.setColor(region);
		components.add(component);

		ChatColor globalRank = null;
		String rankName = null;
		String realName = "";
		if (player != null) {
			// Permission-based rank colors, greatest to least rank
			UserRank[] ranks = UserRank.values();
			for (int i = ranks.length - 1; i >= 0; --i) {
				UserRank rank = ranks[i];
				if (player.hasPermission(rank.getPermission())) {
					globalRank = rank.getColor();
					rankName = rank.getFriendlyName();
					break;
				}
			}

			// Override rank color with scoreboard color if possible
			try {
				Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
				if (team != null && team.getPrefix().length() > 0) {
					ChatColor color = ChatColor.getByChar(team.getPrefix().charAt(team.getPrefix().length() - 1));
					if (color != null) {
						globalRank = color;
					}
				}
			} catch (IllegalStateException | IllegalArgumentException e) {
				// Scoreboard's screwed up, all good. Rank color will display.
			}

			// Default to... default
			if (globalRank == null) {
				globalRank = UserRank.MEMBER.getColor();
			}
			if (rankName == null) {
				rankName = UserRank.MEMBER.getFriendlyName();
			}

			if (channel instanceof NickChannel && ((NickChannel) channel).hasNick(sender)) {
				senderName = ((NickChannel) channel).getNick(sender);
			} else {
				senderName = sender.getDisplayName();
			}
			if (!senderName.equals(player.getName())) {
				realName = lang.getValue("chat.user.realname").replace("{PLAYER}", player.getName());
			}
		} else {
			globalRank = ChatColor.WHITE;
			rankName = "Bot/Service";
		}

		// > Name | <Name
		component = new TextComponent(sender != null
				? channel instanceof NickChannel ? ((NickChannel) channel).getNick(sender)
						: sender.getDisplayName() : senderName);
		component.setColor(globalRank);
		components.add(component);

		// > Name | <Name>
		component = new TextComponent(thirdPerson ? " " : "> ");
		component.setColor(region);
		components.add(component);

		if (nameHover == null && sender != null) {
			nameHover = new BaseComponent[NAME_HOVER.length];

			for (int i = 0; i < NAME_HOVER.length; i++) {
				TextComponent hoverElement = (TextComponent) NAME_HOVER[i].duplicate();
				String text = hoverElement.getText();

				Matcher matcher = NAME_PATTERN.matcher(text);
				if (matcher.find()) {
					text = matcher.replaceAll(senderName);
					hoverElement.setColor(globalRank);
				}

				matcher = REAL_NAME_PATTERN.matcher(text);
				if (matcher.find()) {
					text = matcher.replaceAll(realName);
					hoverElement.setColor(globalRank);
				}

				matcher = RANK_PATTERN.matcher(text);
				if (matcher.find()) {
					text = matcher.replaceAll(rankName);
					hoverElement.setColor(globalRank);
				}

				matcher = CLASS_PATTERN.matcher(text);
				if (matcher.find()) {
					text = matcher.replaceAll(sender.getUserClass().getDisplayName());
				}

				matcher = ASPECT_PATTERN.matcher(text);
				if (matcher.find()) {
					text = matcher.replaceAll(sender.getUserAffinity().getDisplayName());
					hoverElement.setColor(sender.getUserAffinity().getColor());
				}

				hoverElement.setText(text);
				nameHover[i] = hoverElement;
			}

			/*
			 * Yes, this is stupid.
			 *
			 * The client assumes that the first JSON element of an unwrapped array is the
			 * containing element.
			 *
			 * This means that since our first element has formatting, all following elements not
			 * explicitly declared to not have formatting inherit its formatting. It's a client
			 * limitation that can only be bypassed by wrapping all the elements of the array into
			 * a new array as extra.
			 */
			nameHover = new TextComponent[] { new TextComponent(nameHover) };
		}

		TextComponent nameComponent = new TextComponent(components.toArray(new BaseComponent[0]));
		if (nameClick == null && player != null) {
			nameClick = "/m " + player.getName() + ' ';
		}
		nameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, nameClick));
		if (nameHover != null) {
			nameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, nameHover));
		}

		// MESSAGE ELEMENT: Your text here.
		TextComponent messageComponent;
		if (messageComponents != null) {
			messageComponent = new TextComponent(messageComponents);
		} else {
			messageComponent = new TextComponent(JSONUtil.fromLegacyText(message));
		}

		String consoleFormat = String.format(thirdPerson ? CONSOLE_FORMAT_THIRD : CONSOLE_FORMAT,
				channelBracket, channelRank, channel.getName(), region, globalRank, "%s");

		return new Message(this.lang, this.sender, this.senderName, this.channel, this.message,
				consoleFormat, this.thirdPerson, channelComponent, channelHighlightComponent,
				nameComponent, messageComponent);
	}
}
