package com.easterlyn.chat.message;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.RegionChannel;
import com.easterlyn.micromodules.AwayFromKeyboard;
import com.easterlyn.micromodules.Cooldowns;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.JSONUtil;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Used to better clarify a message's destination prior to formatting.
 * 
 * @author Jikoo
 */
public class Message {

	private final Language lang;
	private final User sender;
	private final Channel channel;
	private final boolean thirdPerson;
	private final TextComponent channelComponent, channelHighlightComponent, nameComponent;
	private String name, consoleFormat, unformattedMessage;
	private TextComponent messageComponent;

	protected Message(Language lang, User sender, String name, Channel channel, String message,
			String consoleFormat, boolean thirdPerson, TextComponent channelComponent,
			TextComponent channelHighlightComponent, TextComponent nameComponent,
			TextComponent messageComponent) {
		this.lang = lang;
		this.sender = sender;
		this.name = name;
		this.channel = channel;
		this.thirdPerson = thirdPerson;
		this.unformattedMessage = message;
		this.consoleFormat = consoleFormat;
		this.channelComponent = channelComponent;
		this.channelHighlightComponent = channelHighlightComponent;
		this.nameComponent = nameComponent;
		this.messageComponent = messageComponent;
	}

	public User getSender() {
		return sender;
	}

	public String getSenderName() {
		return name;
	}

	public Channel getChannel() {
		return channel;
	}

	public String getMessage() {
		return unformattedMessage;
	}

	public void setMessage(String message) {
		if (message == null) {
			throw new IllegalArgumentException("Message cannot be null!");
		}
		this.unformattedMessage = message;
		this.messageComponent = new TextComponent(JSONUtil.fromLegacyText(message));
	}

	public String getConsoleMessage() {
		return String.format(getConsoleFormat(), name, unformattedMessage);
	}

	public String getDiscordMessage() {
		// In the future we may allow formatting for all users.
		return unformattedMessage;
	}

	public void setConsoleFormat(String consoleFormat) {
		this.consoleFormat = consoleFormat;
	}

	public String getConsoleFormat() {
		return consoleFormat;
	}

	public boolean isThirdPerson() {
		return thirdPerson;
	}

	public Channel parseReplyChannel() {
		// All Messages have a click event
		String atChannel = channelComponent.getClickEvent().getValue();

		if (atChannel.length() < 2 || atChannel.charAt(0) != '@') {
			return getChannel();
		}

		int end = atChannel.indexOf(' ');
		if (end == -1) {
			end = atChannel.length();
		}

		Channel channel = getChannel().getChannelManager().getChannel(atChannel.substring(1, end));

		return channel == null ? getChannel() : channel;
	}

	public void send() {
		this.send(getChannel().getListening());
	}

	public <T> void send(Collection<T> recipients) {
		this.send(recipients, true, true);
	}

	public <T> void send(Collection<T> recipients, boolean highlight) {
		this.send(recipients, true, highlight);
	}

	public <T> void send(Collection<T> recipients, boolean log, boolean doHighlight) {
		if (log || !(channel instanceof RegionChannel)) {
			Logger.getLogger("Minecraft").info(getConsoleMessage());
		}

		// Support plugins changing the sender's display name in the between Message construction and sending.
		if (this.sender != null && !this.name.equals(this.sender.getDisplayName())) {
			String newName = this.sender.getDisplayName();
			Pattern namePattern = Pattern.compile("(\\s*)\\Q" + this.name + "\\E(\\s*)");
			for (BaseComponent component : nameComponent.getExtra()) {
				if (!(component instanceof TextComponent)) {
					continue;
				}
				TextComponent textComponent = (TextComponent) component;
				textComponent.setText(namePattern.matcher(textComponent.getText()).replaceAll("$1" + newName.replace("$", "\\$") + "$2"));
			}
			this.name = newName;
		}

		Easterlyn plugin = channel.getPlugin();
		AwayFromKeyboard afk = plugin.getModule(AwayFromKeyboard.class);
		Cooldowns cooldowns = plugin.getModule(Cooldowns.class);
		Users users = plugin.getModule(Users.class);

		for (T object : recipients) {
			UUID uuid;
			Player player;
			User recipient;
			if (object instanceof UUID) {
				uuid = (UUID) object;
				player = Bukkit.getPlayer(uuid);
				recipient = users.getUser(uuid);
			} else if (object instanceof Player) {
				player = (Player) object;
				uuid = player.getUniqueId();
				recipient = users.getUser(uuid);
			} else if (object instanceof User) {
				recipient = (User) object;
				uuid = recipient.getUUID();
				player = recipient.getPlayer();
			} else {
				throw new RuntimeException("Invalid recipient type: " + object.getClass());
			}

			if (player == null || !recipient.isOnline() || player.spigot() == null
					|| channel instanceof RegionChannel && recipient.getSuppression()) {
				continue;
			}

			BaseComponent message = messageComponent.duplicate();

			if (channel.equals(recipient.getCurrentChannel())) {
				message.setColor(ChatColor.WHITE);
			} else {
				message.setColor(ChatColor.GRAY);
			}

			if (!doHighlight || sender != null && (sender.equals(recipient) || !recipient.getHighlight())) {
				// Highlights are disabled for this message or the sender is this recipient.
				player.spigot().sendMessage(channelComponent, nameComponent, message);
				continue;
			}

			boolean highlight = false;

			StringBuilder patternString = new StringBuilder("(^|\\W)(\\Q");
			int baseLength = patternString.length();
			for (String highlightString : recipient.getHighlights(getChannel())) {
				if (patternString.length() > baseLength) {
					patternString.append("\\E|\\Q");
				}
				patternString.append(highlightString);
			}
			patternString.append("\\E)(\\W|$)");
			Pattern pattern;
			try {
				pattern = Pattern.compile(patternString.toString(), Pattern.CASE_INSENSITIVE);
			} catch (PatternSyntaxException e) {
				// Display name most likely contains unescaped characters in a nick channel
				pattern = Pattern.compile(recipient.getPlayerName(), Pattern.CASE_INSENSITIVE);
			}
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

			if (sender != null && highlight && !afk.isActive(player)) {
				sender.sendMessage(lang.getValue("chat.user.away").replace("{PLAYER}", player.getDisplayName()));
				// TODO Discord support
			}

			if (highlight && cooldowns.getRemainder(player, "highlight") == 0
					&& !channel.getName().equals("#pm")) {
				// Fun sound effects! Sadly, ender dragon kill is a little long even at 2x
				switch ((int) (Math.random() * 20)) {
				case 0:
					player.playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_STARE, 1, 2);
					break;
				case 1:
					player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 2);
					break;
				case 2:
				case 3:
					player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
					break;
				default:
					player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
				}
				cooldowns.addCooldown(player, "highlight", 30000);
			}
			player.spigot().sendMessage(highlight ? channelHighlightComponent : channelComponent, nameComponent, message);
		}
	}

}
