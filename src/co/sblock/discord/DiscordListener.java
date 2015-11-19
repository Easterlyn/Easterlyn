package co.sblock.discord;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.Color;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.users.Users;

import me.itsghost.jdiscord.DiscordAPI;
import me.itsghost.jdiscord.event.EventListener;
import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.talkable.User;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * EventListener for jDiscord events.
 * 
 * @author Jikoo
 */
public class DiscordListener implements EventListener {

	private final Discord discord;
	private final DiscordAPI api;
	private final BaseComponent[] hover;
	private final Pattern mention;

	public DiscordListener(Discord discord, DiscordAPI api) {
		this.discord = discord;
		this.api = api;
		this.hover = TextComponent.fromLegacyText(Color.GOOD_EMPHASIS + "Discord Chat\n"
				+ ChatColor.BLUE + ChatColor.UNDERLINE + "www.sblock.co/discord\n"
				+ Color.GOOD + "Channel: #main");
		this.mention = Pattern.compile("<@(\\d+)>");
	}

	public void onUserChat(UserChatEvent event) {
		if (event.getUser().getUser().getId().equals(api.getSelfInfo().getId())) {
			return;
		}
		Sblock sblock = Sblock.getInstance();
		String msg = event.getMsg().getMessage();
		if (event.getServer() == null) {
			if (msg.startsWith("/link ")) {
				String register = msg.substring(6);
				if (!discord.getAuthCodes().containsValue(register)) {
					event.getGroup().sendMessage("Invalid registration code!");
					return;
				}
				event.getGroup().sendMessage("Registration complete!");
				UUID link = discord.getAuthCodes().inverse().remove(register);
				sblock.getConfig().set("discord.users." + event.getUser().getUser().getId(), link.toString());
				sblock.saveConfig();
			}
			// TODO accept private messages as commands
			return;
		}
		if (!sblock.getConfig().getString("discord.server").equals(event.getServer().getId())
				|| !sblock.getConfig().getString("discord.chat.main").equals(event.getGroup().getId())) {
			return;
		}
		Player sender = discord.getPlayerFor(event.getUser());
		if (sender == null) {
			event.getMsg().deleteMessage();
			discord.postMessage("Sbot", "<@" + event.getUser().getUser().getId()
							+ ">, you must link your Discord account ingame by running /link before you can talk!",
					event.getGroup().getId());
			return;
		}
		Channel channel = ChannelManager.getChannelManager().getChannel("#discord");
		Message message = new MessageBuilder().setChannel(channel)
				.setSender(Users.getGuaranteedUser(sender.getUniqueId()))
				.setMessage(sanitize(event.getMsg())).setChannel(channel).setNameHover(hover)
				.setNameClick("@# ").setChannelClick("@# ").toMessage();
		// TODO MessageBuilder method overloading: add different click events (open Discord link on name click)
		Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());
		players.removeIf(p -> Users.getGuaranteedUser(p.getUniqueId()).getSuppression());
		Bukkit.getPluginManager().callEvent(new SblockAsyncChatEvent(true, sender, players, message));
	}

	private String sanitize(me.itsghost.jdiscord.message.Message msg) {
		String message = msg.getMessage();
		Matcher matcher = mention.matcher(message);
		StringBuilder sb = new StringBuilder();
		int lastMatch = 0;
		while (matcher.find()) {
			sb.append(message.substring(lastMatch, matcher.start())).append('@');
			String id = matcher.group(1);
			User user = api.getUserById(id);
			if (user == null) {
				sb.append(id);
			} else {
				sb.append(api.getUserById(id).getUsername());
			}
			lastMatch = matcher.end();
		}
		sb.append(message.substring(lastMatch));
		return sb.toString();
	}
}
