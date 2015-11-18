package co.sblock.discord;

import java.util.HashSet;
import java.util.Set;

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
import me.itsghost.jdiscord.talkable.GroupUser;

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

	public DiscordListener(Discord discord, DiscordAPI api) {
		this.discord = discord;
		this.api = api;
		this.hover = TextComponent.fromLegacyText(Color.GOOD_EMPHASIS + "Discord Chat\n"
				+ ChatColor.BLUE + ChatColor.UNDERLINE + "www.sblock.co/discord\n"
				+ Color.GOOD + "Channel: #main");
	}

	public void onUserChat(UserChatEvent event) {
		if (event.getUser().getUser().getId().equals(api.getSelfInfo().getId())) {
			return;
		}
		Sblock sblock = Sblock.getInstance();
		if (event.getServer() == null || event.getGroup() == null) {
			// TODO accept private messages as commands
			return;
		}
		if (!sblock.getConfig().getString("discord.server").equals(event.getServer().getId())
				|| !sblock.getConfig().getString("discord.chat.main").equals(event.getGroup().getId())) {
			return;
		}
		Player sender = discord.getPlayerFor(event.getUser());
		Channel channel = ChannelManager.getChannelManager().getChannel("#discord");
		Message message = new MessageBuilder().setChannel(channel)
				.setSender(getGroupColor(event.getUser()) + event.getUser().getUser().getUsername())
				.setMessage(sanitize(event.getMsg().getMessage())).setChannel(channel).setNameHover(hover)
				.setNameClick("@# ").setChannelClick("@# ").toMessage();
		// TODO MessageBuilder method overloading: add different click events (open Discord link on name click)
		Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());
		players.removeIf(p -> Users.getGuaranteedUser(p.getUniqueId()).getSuppression());
		Bukkit.getPluginManager().callEvent(new SblockAsyncChatEvent(true, sender, players, message));
	}

	private String sanitize(String chat) {
		// TODO
		// @mentions become <@userid>, undo
		return chat;
	}

	private ChatColor getGroupColor(GroupUser user) {
		switch (user.getRole()) {
		case "@horrorterror":
			return Color.RANK_HORRORTERROR;
		case "@denizen":
			return Color.RANK_DENIZEN;
		case "@felt":
			return Color.RANK_FELT;
		case "@helper":
			return Color.RANK_HELPER;
		case "@donator":
			return Color.RANK_DONATOR;
		default:
			return Color.RANK_HERO;
		}
	}
}
