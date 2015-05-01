package co.sblock.chat.message;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.ColorDef;
import co.sblock.chat.channel.CanonNick;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.users.OfflineUser;
import co.sblock.utilities.rawmessages.JSONUtil;
import co.sblock.utilities.regex.RegexUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;

import net.minecraft.server.v1_8_R2.NBTTagCompound;

/**
 * 
 * 
 * @author Jikoo
 */
public class MessageBuilder {

	private static final TextComponent HIGHLIGHTED_BRACKET;

	static {
		HIGHLIGHTED_BRACKET = new TextComponent("!!");
		HIGHLIGHTED_BRACKET.setColor(ChatColor.AQUA);
	}

	private OfflineUser sender = null;
	private String senderName = null;
	private Channel channel = null;
	private String message = null;
	private boolean thirdPerson = false;
	private String atChannel = null;
	private ItemStack hover;
	private String channelClick, nameClick;

	public MessageBuilder setSender(OfflineUser sender) {
		this.sender = sender;
		if (this.channel == null) {
			this.channel = sender.getCurrentChannel();
		}
		return this;
	}

	public MessageBuilder setSender(String name) {
		this.senderName = name;
		return this;
	}

	public MessageBuilder setChannel(Channel channel) {
		this.channel = channel;
		return this;
	}

	public MessageBuilder setMessage(String message) {
		// Set @<channel> destination
		this.atChannel = null;
		int space = message.indexOf(' ');
		if (space > 1 && message.charAt(0) == '@') {
			this.atChannel = message.substring(1, space);
			message = message.substring(space);
			this.channel = ChannelManager.getChannelManager().getChannel(this.atChannel);
		}

		// Anyone can use color codes in nick channels. Channel mods can use color codes in non-rp channels
		if (channel != null && channel.getType() != ChannelType.RP 
				&& (channel.getType() == ChannelType.NICK || sender != null && channel.isModerator(sender))) {
			message = ChatColor.translateAlternateColorCodes('&', message);
		}

		// Trim whitespace created by formatting codes, etc.
		message = RegexUtils.trimExtraWhitespace(message);

		this.message = message;
		return this;
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
		informSender = this.sender != null ? informSender : false;

		// Channel must exist
		if (this.channel == null) {
			if (informSender && this.atChannel != null) {
				this.sender.sendMessage(ChatMsgs.errorInvalidChannel(atChannel));
			} else if (informSender) {
				this.sender.sendMessage(ChatMsgs.errorNoCurrent());
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

		// Shockingly, muted users are not allowed to talk.
		if (this.sender.getMute()) {
			if (informSender) {
				this.sender.sendMessage(ChatColor.RED + "You are muted!");
			}
			return false;
		}

		// No sending messages to global chats while ignoring them.
		if (this.channel.getType() == ChannelType.REGION && this.sender.getSuppression()) {
			if (informSender) {
				this.sender.sendMessage(ChatColor.RED
						+ "You cannot talk in a global channel while suppressing!\nUse "
						+ ChatColor.AQUA + "/suppress" + ChatColor.RED + " to toggle.");
			}
			return false;
		}

		// Nicks required in RP channels.
		if (this.channel.getType() == ChannelType.RP && !this.channel.hasNick(sender)) {
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

		// A channel must be set for these checks
		Player player = sender != null ? sender.getPlayer() : null;

		if (channel.getOwner() == null && (player == null || !player.hasPermission("sblock.felt"))) {
			StringBuilder sb = new StringBuilder();
			for (char character : Normalizer.normalize(message, Normalizer.Form.NFD).toCharArray()) {
				if (character > '\u001F' && character < '\u007E' || character == ChatColor.COLOR_CHAR) {
					sb.append(character);
				}
			}
			// Fuck you.
			message = sb.toString().replaceAll("tilde?s?", "");
		}

		// Greentext must be at least 4 letters long and the second character must be a letter.
		// E.G. >mfw people do it wrong
		// instead of > lol le edgy meme
		if (message.length() > 3 && message.charAt(0) == '>' && Character.isLetter(message.charAt(1))
				&& (player == null || player.hasPermission("sblockchat.greentext"))) {
			message = ChatColor.GREEN + message;
		}

		// Prepend chat colors to every message if sender has permission
		if (channel.getType() != ChannelType.RP && player != null && player.hasPermission("sblockchat.color")) {
			for (ChatColor c : ChatColor.values()) {
				if (player.hasPermission("sblockchat." + c.name().toLowerCase())) {
					message = c + message;
					break;
				}
			}
		}

		// Canon nicks for RP channels
		CanonNick nick = null;
		if (sender != null && channel.getType() == ChannelType.RP) {
			nick = CanonNick.getNick(channel.getNick(sender));
			message = nick.getPrefix() + message;
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
					globalRank = ColorDef.RANK_HORRORTERROR;
				else if (player.hasPermission("sblock.denizen"))
					globalRank = ColorDef.RANK_DENIZEN;
				else if (player.hasPermission("sblock.felt"))
					globalRank = ColorDef.RANK_FELT;
				else if (player.hasPermission("sblock.helper"))
					globalRank = ColorDef.RANK_HELPER;
				else if (player.hasPermission("sblock.donator"))
					globalRank = ColorDef.RANK_DONATOR;
				else if (player.hasPermission("sblock.godtier"))
					globalRank = ColorDef.RANK_GODTIER;
				else {
					globalRank = ColorDef.RANK_HERO;
				}
			}
		} else {
			globalRank = ChatColor.WHITE;
		}

		// > Name | <Name
		component = new TextComponent(sender != null ? channel.getNick(sender) : senderName);
		component.setColor(globalRank);
		components.add(component);

		// > Name | <Name>
		component = new TextComponent(thirdPerson ? " " : "> ");
		component.setColor(region);
		components.add(component);

		if (hover == null && sender != null) {
			hover = new ItemStack(Material.DIAMOND);
			ItemMeta meta = hover.getItemMeta();
			meta.setDisplayName(new StringBuilder().append(ChatColor.YELLOW)
					.append(ChatColor.STRIKETHROUGH).append("+--").append(ChatColor.AQUA)
					.append(ChatColor.RESET).append(' ').append(globalRank)
					.append(sender.getDisplayName()).append(' ').append(ChatColor.YELLOW)
					.append(ChatColor.STRIKETHROUGH).append("--+").toString());
			ArrayList<String> lore = new ArrayList<>();
			lore.add(new StringBuilder().append(ChatColor.DARK_AQUA)
					.append(sender.getUserClass().getDisplayName()).append(ChatColor.YELLOW)
					.append(" of ").append(sender.getUserAspect().getColor())
					.append(sender.getUserAspect().getDisplayName()).toString());
			lore.add(new StringBuilder().append(ChatColor.YELLOW).append("Dream: ")
					.append(sender.getDreamPlanet().getColor())
					.append(sender.getDreamPlanet().getDisplayName()).toString());
			lore.add(new StringBuilder().append(ChatColor.YELLOW).append("Medium: ")
					.append(sender.getMediumPlanet().getColor())
					.append(sender.getMediumPlanet().getDisplayName()).toString());
			meta.setLore(lore);
			hover.setItemMeta(meta);
		}

		TextComponent nameComponent = new TextComponent(components.toArray(new BaseComponent[components.size()]));
		if (nameClick == null && player != null) {
			nameClick = new StringBuilder("/m ").append(player.getName()).append(' ').toString();
		}
		nameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, nameClick));
		if (hover != null) {
			net.minecraft.server.v1_8_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(hover);
			nameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM,
					new BaseComponent[] { new TextComponent(nmsStack.save(new NBTTagCompound()).toString()) }));
		}

		// MESSAGE ELEMENT: Your text here.
		TextComponent messageComponent = new TextComponent(JSONUtil.getJson(message, nick));

		return new Message(this.sender, this.senderName, this.channel, this.message,
				this.thirdPerson, channelComponent, channelHighlightComponent, nameComponent,
				messageComponent);
	}
}
