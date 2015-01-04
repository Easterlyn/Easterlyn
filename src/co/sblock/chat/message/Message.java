package co.sblock.chat.message;

import java.util.Collection;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import co.sblock.chat.ColorDef;
import co.sblock.chat.channel.CanonNicks;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.rawmessages.JSONUtil;
import co.sblock.utilities.regex.RegexUtils;

/**
 * Used to better clarify a message's destination prior to formatting.
 * 
 * @author Jikoo
 */
public class Message {

	private final OfflineUser sender;
	private final Channel channel;
	private final String name, unformattedMessage;
	private final CanonNicks nick;
	private final boolean thirdPerson;
	private final String channelHighlightElement, nameElement;

	/**
	 * @param sender
	 * @param senderName
	 * @param channel
	 * @param message
	 * @param thirdPerson
	 */
	Message(OfflineUser sender, String senderName, Channel channel, String message, boolean thirdPerson) {
		this.sender = sender;
		this.name = senderName;
		this.channel = channel;
		this.thirdPerson = thirdPerson;
		if (channel.getType() != ChannelType.RP && sender != null && sender.getPlayer().hasPermission("sblockchat.color")) {
			Player player = sender.getPlayer();
			for (ChatColor c : ChatColor.values()) {
				if (player.hasPermission("sblockchat." + c.name().toLowerCase())) {
					message = c + message;
				}
			}
		}
		if (channel.getType() != ChannelType.RP && (sender != null && channel.isModerator(sender))) {
			message = ChatColor.translateAlternateColorCodes('&', message);
		}
		unformattedMessage = message.replace("\\", "\\\\").replace("\"", "\\\"");
		this.channelHighlightElement = getChannelPrefix(true);
		this.nameElement = getSenderElement();

		if (sender != null && channel.getType() == ChannelType.RP) {
			this.nick = CanonNicks.getNick(channel.getNick(sender));
		} else {
			this.nick = null;
		}
	}

	public OfflineUser getSender() {
		return sender;
	}

	public Channel getChannel() {
		return channel;
	}

	public String getMessage() {
		return unformattedMessage;
	}

	public String getCleanedMessage() {
		return ChatColor.stripColor(unformattedMessage);
	}

	public String getConsoleMessage() {
		return getConsoleFormat().replace("%1$s", sender != null ? channel.getNick(sender) : name).replace("%2$s", unformattedMessage);
	}

	public String getConsoleFormat() {
		ChatColor region = sender != null ? sender.getCurrentRegion().getColor() : ChatColor.WHITE;
		ChatColor globalRank;
		if (sender != null && sender.isOnline()) {
			Player player = sender.getPlayer();
			// Name color fetched from scoreboard, if team invalid perm-based instead.
			try {
				globalRank = ChatColor.getByChar(Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player).getPrefix().charAt(1));
			} catch (IllegalStateException | IllegalArgumentException | NullPointerException e) {
				if (player.hasPermission("group.horrorterror"))
					globalRank = ColorDef.RANK_HORRORTERROR;
				else if (player.hasPermission("group.denizen"))
					globalRank = ColorDef.RANK_DENIZEN;
				else if (player.hasPermission("group.felt"))
					globalRank = ColorDef.RANK_FELT;
				else if (player.hasPermission("group.helper"))
					globalRank = ColorDef.RANK_HELPER;
				else if (player.hasPermission("group.donator"))
					globalRank = ColorDef.RANK_DONATOR;
				else if (player.hasPermission("group.godtier"))
					globalRank = ColorDef.RANK_GODTIER;
				else {
					globalRank = ColorDef.RANK_HERO;
				}
			}
		} else {
			globalRank = ChatColor.WHITE;
		}
		new StringBuilder('[')
				.append(channel.isOwner(sender) ? ChatColor.RED : channel.isModerator(sender)
						? ChatColor.AQUA : ChatColor.GOLD).append(']').append(region)
				.append(thirdPerson ? "> " : " <").append(globalRank).append("%1$s").append(region)
				.append(thirdPerson ? " " : "> ").append(ChatColor.WHITE).append("%2$s");
		return "[" + channel.getName() + "]" + (thirdPerson ? "> " : " <") + "%1$s"
				+ (thirdPerson ? " " : "> ") + "%2$s";
	}

	public boolean isThirdPerson() {
		return thirdPerson;
	}

	private String getChannelPrefix(boolean highlighting) {
		StringBuilder json = new StringBuilder("{\"text\":\"\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"@");
		json.append(channel.getName()).append(" \"},\"extra\":[{\"text\":\"");
		if (highlighting) {
			json.append("!!");
		} else {
			json.append("[");
		}
		json.append("\",\"color\":\"");
		String bracketColor;
		if (highlighting) {
			bracketColor = "aqua";
		} else if (sender != null && sender.getPlayer().hasPermission("sblock.guildleader")) {
			bracketColor = sender.getUserAspect().getColor().name().toLowerCase();
		} else {
			bracketColor = "white";
		}
		json.append(bracketColor);
		json.append("\"},{\"text\":\"").append(channel.getName()).append("\",\"color\":\"");
		json.append(channel.isOwner(sender) ? "red" : channel.isModerator(sender) ? "aqua" : "gold");
		json.append("\"},{\"text\":\"");
		if (highlighting) {
			json.append("!!");
		} else {
			json.append("]");
		}
		json.append("\",\"color\":\"").append(bracketColor).append("\"}]}");

		return json.toString();
	}

	private String getSenderElement() {
		ChatColor region = sender != null ? sender.getCurrentRegion().getColor() : ChatColor.WHITE;

		StringBuilder json = new StringBuilder("{\"text\":\"").append(thirdPerson ? "> " : " <")
				.append("\",\"color\":\"").append(region.name().toLowerCase());
		json.append("\"},{\"text\":\"").append(sender != null ? channel.getNick(sender) : name);

		ChatColor globalRank;
		if (sender != null && sender.isOnline()) {
			Player player = sender.getPlayer();
			// Name color fetched from scoreboard, if team invalid perm-based instead.
			try {
				globalRank = ChatColor.getByChar(Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player).getPrefix().charAt(1));
			} catch (IllegalStateException | IllegalArgumentException | NullPointerException e) {
				if (player.hasPermission("group.horrorterror"))
					globalRank = ColorDef.RANK_HORRORTERROR;
				else if (player.hasPermission("group.denizen"))
					globalRank = ColorDef.RANK_DENIZEN;
				else if (player.hasPermission("group.felt"))
					globalRank = ColorDef.RANK_FELT;
				else if (player.hasPermission("group.helper"))
					globalRank = ColorDef.RANK_HELPER;
				else if (player.hasPermission("group.donator"))
					globalRank = ColorDef.RANK_DONATOR;
				else if (player.hasPermission("group.godtier"))
					globalRank = ColorDef.RANK_GODTIER;
				else {
					globalRank = ColorDef.RANK_HERO;
				}
			}
		} else {
			globalRank = ChatColor.WHITE;
		}

		json.append("\",\"color\":\"").append(globalRank.name().toLowerCase()).append('\"');
		if (sender != null) {
			json.append(",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/m ").append(sender.getPlayerName());
			json.append(" \"},\"hoverEvent\":{\"action\":\"show_item\",\"value\":\"{id:diamond,tag:{display:{Name:\\\"");
			json.append(ChatColor.YELLOW).append(ChatColor.STRIKETHROUGH).append("+---").append(ChatColor.RESET)
					.append(' ').append(globalRank).append(channel.getNick(sender)).append(' ')
					.append(ChatColor.YELLOW).append(ChatColor.STRIKETHROUGH).append("---+\\\",Lore:[\\\"");
			json.append(ChatColor.DARK_AQUA).append(sender.getUserClass().getDisplayName())
					.append(ChatColor.YELLOW).append(" of ").append(sender.getUserAspect().getColor())
					.append(sender.getUserAspect().getDisplayName()).append("\\\",\\\"");
			json.append(ChatColor.YELLOW).append("Dream: ").append(sender.getDreamPlanet().getColor())
					.append(sender.getDreamPlanet().getDisplayName()).append("\\\",\\\"");
			json.append(ChatColor.YELLOW).append("Medium: ").append(sender.getMediumPlanet().getColor())
					.append(sender.getMediumPlanet().getDisplayName()).append("\\\"]}}}\"}");
		}
		json.append("},{\"text\":\"").append(thirdPerson ? " " : "> ")
				.append("\",\"color\":\"").append(region.name().toLowerCase()).append("\"}");

		return json.toString();
	}

	public void send() {
		this.send(false);
	}

	public void send(boolean uncancelledRegionalChat) {
		this.send(getChannel().getListening(), uncancelledRegionalChat);
	}

	public <T> void send(Collection<T> recipients) {
		this.send(recipients, false);
	}

	public <T> void send(Collection<T> recipients, boolean uncancelledRegionalChat) {
		if (uncancelledRegionalChat) {
			Bukkit.getConsoleSender().sendMessage(getConsoleMessage());
		}
		String focusedUnhighlighted = JSONUtil.getWrappedJSON(getChannelPrefix(false), nameElement,
				JSONUtil.toJSONElements(ChatColor.WHITE + unformattedMessage, true, nick));
		String unfocusedUnhighlighted = JSONUtil.getWrappedJSON(getChannelPrefix(false), nameElement,
				JSONUtil.toJSONElements(ChatColor.GRAY + unformattedMessage, true, nick));
		for (T object : recipients) {
			UUID uuid;
			Player player;
			if (object instanceof UUID) {
				uuid = (UUID) object;
				player = Bukkit.getPlayer(uuid);
			} else if (object instanceof Player) {
				player = (Player) object;
				uuid = player.getUniqueId();
			} else {
				throw new RuntimeException("Invalid recipient type: " + object.getClass());
			}
			OfflineUser u = Users.getGuaranteedUser(uuid);
			if (player == null || !u.isOnline() || channel.getType() == ChannelType.REGION && u.getSuppression()) {
				continue;
			}
			if (sender != null && (sender.equals(u) || !sender.getHighlight())) {
				// No self-highlight.
				if (channel.equals(u.getCurrentChannel())) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + u.getPlayerName() + ' ' + focusedUnhighlighted);
				} else {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + u.getPlayerName() + ' ' + unfocusedUnhighlighted);
				}
				continue;
			}

			String message = (channel.equals(u.getCurrentChannel()) ? ChatColor.WHITE : ChatColor.GRAY) + unformattedMessage;
			StringBuilder msg = new StringBuilder();
			Matcher match = Pattern.compile(RegexUtils.ignoreCaseRegex(u.getHighlights(getChannel()).toArray(new String[0]))).matcher(message);
			int lastEnd = 0;
			// For every match, prepend aqua chat color and append previous color
			while (match.find()) {
				msg.append(message.substring(lastEnd, match.start()));
				String last = ChatColor.getLastColors(msg.toString());
				msg.append(ChatColor.AQUA).append(match.group()).append(last);
				lastEnd = match.end();
			}

			if (lastEnd > 0) {
				if (lastEnd < message.length()) {
					msg.append(message.substring(lastEnd));
				}
				// Funtimes sound effects here
				switch ((int) (Math.random() * 20)) {
				case 0:
					player.playSound(player.getLocation(), Sound.ENDERMAN_STARE, 1, 2);
					break;
				case 1:
					player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1, 2);
					break;
				case 2:
				case 3:
					player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1, 1);
					break;
				default:
					player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 2);
				}
				String rawHighlight = JSONUtil.getWrappedJSON(channelHighlightElement, nameElement, JSONUtil.toJSONElements(msg.toString(), true, nick));
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + u.getPlayerName() + ' ' + rawHighlight);
				continue;
			}

			if (channel.equals(u.getCurrentChannel())) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + u.getPlayerName() + ' ' + focusedUnhighlighted);
			} else {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + u.getPlayerName() + ' ' + unfocusedUnhighlighted);
			}
		}
	}
}
