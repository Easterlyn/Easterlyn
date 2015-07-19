package co.sblock.chat.message;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.Color;
import co.sblock.chat.channel.CanonNick;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.NickChannel;
import co.sblock.chat.channel.RPChannel;
import co.sblock.chat.channel.RegionChannel;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.messages.JSONUtil;
import co.sblock.utilities.messages.RegexUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Builder for Messages. In most cases, set sender, channel, and finally message. In the event that
 * channel must be fixed, set it last to prevent @#channel changing. However, in the case of nick
 * channels, this can result in chat colors not being allowed when they should be.
 * 
 * @author Jikoo
 */
public class MessageBuilder {

	private static final Pattern URL_PATTERN = Pattern.compile("^(https?://)?(([\\w-_]+\\.)+([a-zA-Z]{2,4}))((#|/)\\S*)?$");
	private static final TextComponent HIGHLIGHTED_BRACKET;
	private static final String ITEM_NAME;
	private static final String LORE_CLASS_OF_ASPECT;
	private static final String LORE_DREAM;
	private static final String LORE_MEDIUM;

	static {
		HIGHLIGHTED_BRACKET = new TextComponent("!!");
		HIGHLIGHTED_BRACKET.setColor(ChatColor.AQUA);

		StringBuilder sb = new StringBuilder();
		sb.append(ChatColor.YELLOW).append(ChatColor.STRIKETHROUGH).append("+--")
				.append(ChatColor.AQUA).append(ChatColor.RESET).append(" %s%s ")
				.append(ChatColor.YELLOW).append(ChatColor.STRIKETHROUGH).append("--+");
		ITEM_NAME = sb.toString();

		sb.delete(0, sb.length());
		sb.append(ChatColor.DARK_AQUA).append("%s").append(ChatColor.YELLOW)
				.append(" of %s%s");
		LORE_CLASS_OF_ASPECT = sb.toString();

		sb.delete(0, sb.length());
		sb.append(ChatColor.YELLOW).append("Dream: %s%s");
		LORE_DREAM = sb.toString();

		sb.delete(0, sb.length());
		sb.append(ChatColor.YELLOW).append("Medium: %s%s");
		LORE_MEDIUM = sb.toString();
	}

	private OfflineUser sender = null;
	private String senderName = null;
	private Channel channel = null;
	private String message = null;
	private boolean thirdPerson = false;
	private String atChannel = null;
	private ItemStack hover;
	private String channelClick, nameClick;
	private TextComponent[] messageComponents;

	public MessageBuilder setSender(OfflineUser sender) {
		if (this.sender != null || this.senderName != null) {
			throw new IllegalArgumentException("Sender is already set!");
		}
		this.sender = sender;
		if (this.channel == null) {
			this.channel = sender.getCurrentChannel();
		}
		return this;
	}

	public MessageBuilder setSender(String name) {
		if (this.sender != null || this.senderName != null) {
			throw new IllegalArgumentException("Sender is already set!");
		}
		this.senderName = name;
		return this;
	}

	public MessageBuilder setChannel(Channel channel) {
		this.channel = channel;
		return this;
	}

	public MessageBuilder setMessage(TextComponent... messageComponents) {
		if (messageComponents.length == 0) {
			throw new IllegalArgumentException("Message components must exist!");
		}
		this.messageComponents = messageComponents;
		this.message = TextComponent.toPlainText(messageComponents);
		return this;
	}

	public MessageBuilder setMessage(String message) {
		if (this.messageComponents != null) {
			this.messageComponents = null;
		}
		// Set @<channel> destination
		this.atChannel = null;
		int space = message.indexOf(' ');
		if (space > 1 && message.charAt(0) == '@') {
			this.atChannel = message.substring(1, space);
			message = message.substring(space);
			this.channel = ChannelManager.getChannelManager().getChannel(this.atChannel);
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
			channel.getListening().forEach(uuid -> {
				names.add(Users.getGuaranteedUser(uuid).getPlayerName());
			});
			StringBuilder sb = new StringBuilder();
			for (String word : Normalizer.normalize(message, Normalizer.Form.NFD).split(" ")) {
				if (word.isEmpty()) {
					continue;
				}
				if (URL_PATTERN.matcher(word).find()) {
					continue;
				}

				// Anything goes as long as it's the name of a recipient
				if (names.contains(word)) {
					sb.append(word).append(' ');
					continue;
				}

				// I'm aware that this will strip a couple cases that belong capitalized,
				// but no self-respecting person sings Old MacDonald anyway.
				boolean stripUpper = stripUpper(word);

				for (char character : word.toCharArray()) {
					if (isCharacterGloballyIllegal(character) || character == ChatColor.COLOR_CHAR) {
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
			message = sb.toString().replaceAll("[tT]ilde?s?", "");
		}

		// Trim whitespace created by formatting codes, etc.
		message = RegexUtils.trimExtraWhitespace(message);

		this.message = message;
		return this;
	}

	private boolean stripUpper(String word) {
		for (int i = 0, upper = 0, total = 0; i < word.length(); i++) {
			char character = word.charAt(i);
			if (Character.isAlphabetic(character)) {
				boolean startsUpper = Character.isUpperCase(character);
				for (; i < word.length(); i++) {
					character = word.charAt(i);
					if (Character.isAlphabetic(character)) {
						total++;
						if (Character.isUpperCase(character)) {
							upper++;
						}
					}
				}
				return !startsUpper && upper > 0 || upper != total;
			}
		}
		return false;
	}

	private boolean isCharacterGloballyIllegal(char character) {
		return character > '\u001F' && character < '\u007E';
	}

	public MessageBuilder setThirdPerson(boolean thirdPerson) {
		this.thirdPerson = thirdPerson;
		return this;
	}

	public MessageBuilder setNameHover(ItemStack hover) {
		this.hover = hover;
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
		if (RegexUtils.appearsEmpty(this.message)) {
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
		ChatColor channelRank = channel.isOwner(sender) ? ChatColor.RED
				: channel.isModerator(sender) ? ChatColor.AQUA : ChatColor.GOLD;

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
		if (player != null) {
			// Name color fetched from scoreboard, if team invalid perm-based instead.
			try {
				globalRank = ChatColor.getByChar(Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player).getPrefix().charAt(1));
			} catch (IllegalStateException | IllegalArgumentException | NullPointerException e) {
				if (player.hasPermission("group.horrorterror"))
					globalRank = Color.RANK_HORRORTERROR;
				else if (player.hasPermission("sblock.denizen"))
					globalRank = Color.RANK_DENIZEN;
				else if (player.hasPermission("sblock.felt"))
					globalRank = Color.RANK_FELT;
				else if (player.hasPermission("sblock.helper"))
					globalRank = Color.RANK_HELPER;
				else if (player.hasPermission("sblock.donator"))
					globalRank = Color.RANK_DONATOR;
				else if (player.hasPermission("sblock.godtier"))
					globalRank = Color.RANK_GODTIER;
				else {
					globalRank = Color.RANK_HERO;
				}
			}
		} else {
			globalRank = ChatColor.WHITE;
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

		if (hover == null && sender != null) {
			hover = new ItemStack(Material.DIAMOND);
			ItemMeta meta = hover.getItemMeta();
			meta.setDisplayName(String.format(ITEM_NAME, globalRank, sender.getDisplayName()));
			ArrayList<String> lore = new ArrayList<>();
			lore.add(String.format(LORE_CLASS_OF_ASPECT, sender.getUserClass().getDisplayName(),
					sender.getUserAspect().getColor(), sender.getUserAspect().getDisplayName()));
			lore.add(String.format(LORE_DREAM, sender.getDreamPlanet().getColor(), sender
					.getDreamPlanet().getDisplayName()));
			lore.add(String.format(LORE_MEDIUM, sender.getMediumPlanet().getColor(), sender
					.getMediumPlanet().getDisplayName()));
			meta.setLore(lore);
			hover.setItemMeta(meta);
		}

		TextComponent nameComponent = new TextComponent(components.toArray(new BaseComponent[components.size()]));
		if (nameClick == null && player != null) {
			nameClick = new StringBuilder("/m ").append(player.getName()).append(' ').toString();
		}
		nameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, nameClick));
		if (hover != null) {
			nameComponent.setHoverEvent(JSONUtil.getItemHover(hover));
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

		return new Message(this.sender, this.senderName, this.channel, this.message,
				this.thirdPerson, channelComponent, channelHighlightComponent, nameComponent,
				messageComponent);
	}
}
