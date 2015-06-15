package co.sblock.chat.message;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import co.sblock.chat.Color;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.NickChannel;
import co.sblock.chat.channel.RegionChannel;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.Log;
import co.sblock.utilities.messages.Slack;
import co.sblock.utilities.regex.RegexUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Used to better clarify a message's destination prior to formatting.
 * 
 * @author Jikoo
 */
public class Message {

	private final OfflineUser sender;
	private final Channel channel;
	private final String name, unformattedMessage;
	private final boolean thirdPerson;

	private final TextComponent channelComponent, channelHighlightComponent, nameComponent, messageComponent;

	Message(OfflineUser sender, String senderName, Channel channel, String message,
			boolean thirdPerson, TextComponent channelComponent,
			TextComponent channelHighlightComponent, TextComponent nameComponent,
			TextComponent messageComponent) {
		this.sender = sender;
		this.name = senderName;
		this.channel = channel;
		this.thirdPerson = thirdPerson;
		this.unformattedMessage = message;
		this.channelComponent = channelComponent;
		this.channelHighlightComponent = channelHighlightComponent;
		this.nameComponent = nameComponent;
		this.messageComponent = messageComponent;
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
		StringBuilder nameBuilder = new StringBuilder();
		if (sender != null) {
			boolean hasNick = channel instanceof NickChannel && ((NickChannel) channel).hasNick(sender);
			if (hasNick) {
				nameBuilder.append(((NickChannel) channel).getNick(sender)).append(" (");
			}
			nameBuilder.append(sender.getPlayerName());
			if (hasNick) {
				nameBuilder.append(')');
			}
		} else {
			nameBuilder.append(name);
		}
		return String.format(getConsoleFormat(), nameBuilder.toString(), unformattedMessage);
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
		return new StringBuilder().append(ChatColor.WHITE).append('[')
				.append(channel.isOwner(sender) ? ChatColor.RED : channel.isModerator(sender)
						? ChatColor.AQUA : ChatColor.GOLD).append(channel.getName())
				.append(ChatColor.WHITE).append(']').append(region)
				.append(thirdPerson ? "> " : " <").append(globalRank).append("%1$s").append(region)
				.append(thirdPerson ? " " : "> ").append(ChatColor.WHITE).append("%2$s").toString();
	}

	public boolean isThirdPerson() {
		return thirdPerson;
	}

	public void send() {
		this.send(getChannel().getListening());
	}

	public <T> void send(Collection<T> recipients) {
		this.send(recipients, false);
	}

	public <T> void send(Collection<T> recipients, boolean normalChat) {
		String consoleMessage = getConsoleMessage();
		if (!normalChat || channel instanceof RegionChannel) {
			if (recipients.size() < channel.getListening().size()) {
				consoleMessage = "[SoftMute] " + consoleMessage;
			}
			Log.anonymousInfo(getConsoleMessage());
		}

		Slack.getInstance().postMessage(sender != null ? sender.getPlayerName() : name,
				sender != null ? sender.getUUID() : null, consoleMessage,
				channel.getName().equals("#"));

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
			if (player == null || !u.isOnline() || player.spigot() == null
					|| channel instanceof RegionChannel && u.getSuppression()) {
				continue;
			}

			BaseComponent message = messageComponent.duplicate();

			if (channel.equals(u.getCurrentChannel())) {
				message.setColor(ChatColor.WHITE);
			} else {
				message.setColor(ChatColor.GRAY);
			}

			if (sender != null && (sender.equals(u) || !sender.getHighlight())) {
				// No self-highlight.
				player.spigot().sendMessage(channelComponent, nameComponent, message);
				continue;
			}

			boolean highlight = false;

			Pattern pattern = Pattern.compile(RegexUtils.ignoreCaseRegex(u.getHighlights(getChannel()).toArray(new String[0])));
			for (BaseComponent component : message.getExtra()) {
				TextComponent text = (TextComponent) component;
				String componentMessage = text.getText();
				Matcher match = pattern.matcher(text.getText());
				List<BaseComponent> components = new LinkedList<>();
				int lastEnd = 0;
				while (match.find()) {
					components.add(new TextComponent(componentMessage.substring(lastEnd, match.start())));
					TextComponent highlightComponent = new TextComponent(match.group());
					highlightComponent.setColor(ChatColor.AQUA);
					components.add(highlightComponent);
					lastEnd = match.end();
				}
				if (lastEnd == 0) {
					continue;
				}
				highlight = true;
				components.add(new TextComponent(componentMessage.substring(lastEnd)));
				text.setText("");
				text.setExtra(components);
			}

			if (highlight) {
				if (!channel.getName().equals("#pm")) {
					// Fun sound effects! Sadly, ender dragon kill is a little long even at 2x
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
				}
			}
			player.spigot().sendMessage(highlight ? channelHighlightComponent : channelComponent, nameComponent, message);
		}
	}
}
