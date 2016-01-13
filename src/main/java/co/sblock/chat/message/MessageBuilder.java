package co.sblock.chat.message;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.Chat;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.Color;
import co.sblock.chat.channel.CanonNick;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.NickChannel;
import co.sblock.chat.channel.RPChannel;
import co.sblock.chat.channel.RegionChannel;
import co.sblock.users.User;
import co.sblock.users.Users;
import co.sblock.utilities.JSONUtil;
import co.sblock.utilities.TextUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Builder for Messages. In most cases, set sender, channel, and finally message. In the event that
 * channel must be fixed, set it last to prevent @#channel changing. However, in the case of nick
 * channels, this can result in chat colors not being allowed when they should be.
 * 
 * @author Jikoo
 */
public class MessageBuilder {

	private static final TextComponent HIGHLIGHTED_BRACKET;
	private static final String CONSOLE_FORMAT;
	private static final String CONSOLE_FORMAT_THIRD;
	private static final BaseComponent[] NAME_HOVER;

	static {
		HIGHLIGHTED_BRACKET = new TextComponent("!!");
		HIGHLIGHTED_BRACKET.setColor(ChatColor.AQUA);

		NAME_HOVER = new TextComponent[9];

		// +-- Name --+
		TextComponent component = new TextComponent("+--");
		component.setColor(Color.GOOD);
		component.setStrikethrough(true);
		NAME_HOVER[0] = component;
		component = new TextComponent(" %s ");
		NAME_HOVER[1] = component;
		component = new TextComponent("--+\n");
		component.setColor(Color.GOOD);
		component.setStrikethrough(true);
		NAME_HOVER[2] = component;

		// Rank (Normal server rank)
		component = new TextComponent("%s\n");
		NAME_HOVER[3] = component;

		// Class of Aspect
		component = new TextComponent("%s of %s\n");
		NAME_HOVER[4] = component;

		// Dream: Dream
		component = new TextComponent("Dream: ");
		component.setColor(Color.GOOD);
		NAME_HOVER[5] = component;
		component = new TextComponent("%s\n");
		NAME_HOVER[6] = component;

		// Medium: Medium
		component = new TextComponent("Medium: ");
		component.setColor(Color.GOOD);
		NAME_HOVER[7] = component;
		component = new TextComponent("%s");
		NAME_HOVER[8] = component;

		StringBuilder sb = new StringBuilder();
		sb.append("%1$s[%2$s%3$s%1$s]%4$s <%5$s%6$s%4$s> ").append(ChatColor.WHITE).append("%6$s");
		CONSOLE_FORMAT = sb.toString();
		CONSOLE_FORMAT_THIRD = CONSOLE_FORMAT.replace(">", "").replace(" <", "> ");
	}

	private final Sblock plugin;
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

	public MessageBuilder(Sblock plugin) {
		this.plugin = plugin;
		this.manager = plugin.getModule(Chat.class).getChannelManager();
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

		// Anyone can use color codes in nick channels. Channel mods can use color codes in non-rp channels
		if (channel != null && !(channel instanceof RPChannel)
				&& (channel instanceof NickChannel || sender != null && channel.isModerator(sender))) {
			message = ChatColor.translateAlternateColorCodes('&', message);
		}

		Player player = sender != null ? sender.getPlayer() : null;
		// Strip characters that are not allowed in default channels and partial caps
		if (channel != null && channel.getOwner() == null && !(channel instanceof NickChannel)
				&& (player == null || !player.hasPermission("sblock.felt"))) {
			ArrayList<String> names = new ArrayList<String>();
			Users users = plugin.getModule(Users.class);
			channel.getListening().forEach(uuid -> {
				names.add(users.getUser(uuid).getPlayerName());
			});
			StringBuilder sb = new StringBuilder();
			for (String word : Normalizer.normalize(message, Normalizer.Form.NFD).split(" ")) {
				if (word.isEmpty()) {
					continue;
				}
				if (TextUtils.URL_PATTERN.matcher(word).find()) {
					sb.append(word).append(' ');
					continue;
				}

				// Anything goes as long as it's the name of a recipient
				if (names.contains(TextUtils.stripEndPunctuation(word))) {
					sb.append(word).append(' ');
					continue;
				}

				// I'm aware that this will strip a couple cases that belong capitalized,
				// but no self-respecting person sings Old MacDonald anyway.
				boolean stripUpper = stripUpper(word);

				for (char character : word.toCharArray()) {
					if (isCharacterGloballyLegal(character) || character == ChatColor.COLOR_CHAR) {
						if (stripUpper) {
							character = Character.toLowerCase(character);
						}
						sb.append(character);
					}
				}
				sb.append(' ');
			}
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			message = sb.toString().replaceAll("[tT][iI][lL][dD][AEae]?[sS]?", "").replace("GOG", "GOD")
					.replaceAll("([Gg])og", "$1od").replace("JEGUS", "JESUS")
					.replaceAll("([Jj])egus", "$1esus");
		}

		// Trim whitespace created by formatting codes, etc.
		message = TextUtils.trimExtraWhitespace(message);

		this.message = message;
		return this;
	}

	private boolean stripUpper(String word) {
		for (int i = 0, upper = 0, total = 0; i < word.length(); i++) {
			char character = word.charAt(i);
			if (Character.isAlphabetic(character)) {
				boolean startsUpper = Character.isUpperCase(character);
				for (i++; i < word.length(); i++) {
					character = word.charAt(i);
					if (Character.isAlphabetic(character)) {
						total++;
						if (Character.isUpperCase(character)) {
							upper++;
						}
					}
				}
				return !startsUpper && upper > 0 || upper != 0 && upper != total;
			}
		}
		return false;
	}

	private boolean isCharacterGloballyLegal(char character) {
		//  character >= ' ' and character <= }
		return character > '\u001F' && character < '\u007E';
	}

	public MessageBuilder setThirdPerson(boolean thirdPerson) {
		this.thirdPerson = thirdPerson;
		return this;
	}


	/**
	 * Sets Message display name tooltip text.
	 * <p>
	 * If the tooltip will not change between uses, it is preferred that you use
	 * {@link MessageBuilder#setNameHover(TextComponent)} and store the value.
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

	public boolean canBuild(boolean informSender) {
		informSender = this.sender != null && informSender;

		// Channel must exist
		if (this.channel == null) {
			if (informSender && this.atChannel != null) {
				this.sender.sendMessage(ChatMsgs.errorInvalidChannel(atChannel));
			} else if (informSender) {
				this.sender.sendMessage(ChatMsgs.errorCurrentChannelNull());
			}
			return false;
		}

		// No sending of blank messages.
		if (TextUtils.appearsEmpty(this.message)) {
			if (informSender) {
				this.sender.sendMessage(ChatMsgs.errorEmptyMessage());
			}
			return false;
		}

		// No sender and no name, invalid message.
		if (this.sender == null) {
			return this.senderName != null;
		}

		// No sending messages to global chats while ignoring them.
		if (this.channel instanceof RegionChannel && this.sender.getSuppression()) {
			if (informSender) {
				this.sender.sendMessage(ChatColor.RED
						+ "You cannot talk in a global channel while suppressing!\nUse "
						+ ChatColor.AQUA + "/suppress" + ChatColor.RED + " to toggle.");
			}
			return false;
		}

		// Nicks required in RP channels.
		if (this.channel instanceof RPChannel && !((RPChannel) this.channel).hasNick(sender)) {
			if (informSender) {
				this.sender.sendMessage(ChatColor.GOLD + channel.getName() + ChatColor.RED
						+ " is a roleplay channel, a nick is required. Check " + ChatColor.AQUA
						+ "/nick list");
			}
			return false;
		}

		return true;
	}

	public boolean isSenderInChannel(boolean informSender) {
		// Must be in target channel to send messages.
		if (sender == null || this.channel.getListening().contains(this.sender.getUUID())) {
			return true;
		}
		if (informSender) {
			this.sender.sendMessage(ChatColor.RED + "You are not listening to " + ChatColor.GOLD + channel.getName());
		}
		return false;
	}

	public Message toMessage() {
		if (!canBuild(false)) {
			throw new RuntimeException("Someone did something stupid with chat!");
		}

		Player player = sender != null ? sender.getPlayer() : null;

		// Greentext must be at least 4 letters long and the second character must be a letter.
		// E.G. >mfw people do it wrong
		// instead of > lol le edgy meme
		if (message.length() > 3 && message.charAt(0) == '>' && Character.isLetter(message.charAt(1))
				&& (player == null || player.hasPermission("sblockchat.greentext"))) {
			message = ChatColor.GREEN + message;
		}

		// Prepend chat colors to every message if sender has permission
		if (!(channel instanceof RPChannel) && player != null && player.hasPermission("sblockchat.color")) {
			for (ChatColor c : ChatColor.values()) {
				if (player.hasPermission("sblockchat." + c.name().toLowerCase())) {
					message = c + message;
					break;
				}
			}
		}

		// Canon nicks for RP channels
		CanonNick nick = null;
		if (sender != null && channel instanceof RPChannel) {
			nick = CanonNick.getNick(((RPChannel) channel).getNick(sender));
		}

		// CHANNEL ELEMENT: [#channel]
		ChatColor channelBracket;
		if (player != null && player.hasPermission("sblock.guildleader")) {
			channelBracket = sender.getUserAspect().getColor();
		} else {
			channelBracket = ChatColor.WHITE;
		}
		ChatColor channelRank = channel.isOwner(sender) ? Color.CHANNEL_OWNER
				: channel.isModerator(sender) ? Color.CHANNEL_MOD : Color.CHANNEL_MEMBER;

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

		TextComponent channelComponent = new TextComponent(components.toArray(new BaseComponent[components.size()]));
		if (channelClick == null) {
			channelClick = new StringBuilder("@").append(channel.getName()).append(' ').toString();
		}
		channelComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, channelClick));
		TextComponent channelHighlightComponent = new TextComponent(channelHighlightComponents.toArray(new BaseComponent[channelHighlightComponents.size()]));
		channelHighlightComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, channelClick));

		components.clear();

		// NAME ELEMENT: <Name>
		ChatColor region = sender != null ? sender.getCurrentRegion().getColor() : ChatColor.WHITE;

		// > | <
		component = new TextComponent(thirdPerson ? "> " : " <");
		component.setColor(region);
		components.add(component);

		ChatColor globalRank;
		String rankName;
		if (player != null) {
			// Permission-based rank colors
			if (player.hasPermission("group.horrorterror")) {
				globalRank = Color.RANK_HORRORTERROR;
				rankName = "Horrorterror";
			} else if (player.hasPermission("sblock.denizen")) {
				globalRank = Color.RANK_DENIZEN;
				rankName = "Denizen";
			} else if (player.hasPermission("sblock.felt")) {
				globalRank = Color.RANK_FELT;
				rankName = "Felt";
			} else if (player.hasPermission("sblock.helper")) {
				globalRank = Color.RANK_HELPER;
				rankName = "Helper";
			} else if (player.hasPermission("sblock.donator")) {
				globalRank = Color.RANK_DONATOR;
				rankName = "Donator";
			} else if (player.hasPermission("sblock.godtier")) {
				globalRank = Color.RANK_GODTIER;
				rankName = "Godtier";
			} else {
				globalRank = Color.RANK_HERO;
				rankName = "Hero";
			}
			// Override rank color with scoreboard color if possible
			try {
				Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
				if (team != null && team.getPrefix() != null) {
					ChatColor color = ChatColor.getByChar(team.getPrefix().charAt(team.getPrefix().length() - 1));
					if (color != null) {
						globalRank = color;
					}
				}
			} catch (IllegalStateException | IllegalArgumentException e) {
				// Scoreboard's screwed up, all good. Rank color will display.
			}
			StringBuilder nameBuilder = new StringBuilder();
			boolean hasNick = channel instanceof NickChannel && ((NickChannel) channel).hasNick(sender);
			if (hasNick) {
				nameBuilder.append(((NickChannel) channel).getNick(sender)).append(" (");
			}
			nameBuilder.append(sender.getPlayerName());
			if (hasNick) {
				nameBuilder.append(')');
			}
			senderName = nameBuilder.toString();
		} else {
			globalRank = ChatColor.WHITE;
			rankName = "Bot/Service";
		}

		// > Name | <Name
		component = new TextComponent(nick != null ? nick.getDisplayName() : sender != null
				? channel instanceof NickChannel ? ((NickChannel) channel).getNick(sender)
						: sender.getDisplayName() : senderName);
		component.setColor(nick != null ? nick.getNameColor() : globalRank);
		components.add(component);

		// > Name | <Name>
		component = new TextComponent(thirdPerson ? " " : "> ");
		component.setColor(region);
		components.add(component);

		if (nameHover == null && sender != null) {
			nameHover = new BaseComponent[NAME_HOVER.length];

			nameHover[0] = NAME_HOVER[0];

			// Set name and global rank color
			component = (TextComponent) NAME_HOVER[1].duplicate();
			component.setText(String.format(component.getText(), senderName));
			component.setColor(globalRank);
			nameHover[1] = component;

			nameHover[2] = NAME_HOVER[2];

			// Global rank descriptor
			component = (TextComponent) NAME_HOVER[3].duplicate();
			component.setText(String.format(component.getText(), rankName));
			component.setColor(globalRank);
			nameHover[3] = component;

			// Class of Aspect
			component = (TextComponent) NAME_HOVER[4].duplicate();
			component.setText(String.format(component.getText(), sender.getUserClass()
					.getDisplayName(), sender.getUserAspect().getDisplayName()));
			component.setColor(sender.getUserAspect().getColor());
			nameHover[4] = component;

			nameHover[5] = NAME_HOVER[5];

			// Dream planet
			component = (TextComponent) NAME_HOVER[6].duplicate();
			component.setText(String.format(component.getText(), sender.getDreamPlanet().getDisplayName()));
			component.setColor(sender.getDreamPlanet().getColor());
			nameHover[6] = component;

			nameHover[7] = NAME_HOVER[7];

			// Medium planet
			component = (TextComponent) NAME_HOVER[8].duplicate();
			component.setText(String.format(component.getText(), sender.getMediumPlanet().getDisplayName()));
			component.setColor(sender.getMediumPlanet().getColor());
			nameHover[8] = component;

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

		TextComponent nameComponent = new TextComponent(components.toArray(new BaseComponent[components.size()]));
		if (nameClick == null && player != null) {
			nameClick = new StringBuilder("/m ").append(player.getName()).append(' ').toString();
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
			messageComponent = new TextComponent(JSONUtil.getJson(message, nick));
		}

		// Console prettiness
		if (nick != null) {
			message = nick.getColor() + message;
		}
		String consoleFormat = String.format(thirdPerson ? CONSOLE_FORMAT_THIRD : CONSOLE_FORMAT,
				channelBracket, channelRank, channel.getName(), region, globalRank, "%s");

		return new Message(this.sender, this.senderName, this.channel, this.message, consoleFormat,
				this.thirdPerson, channelComponent, channelHighlightComponent, nameComponent,
				messageComponent);
	}
}
