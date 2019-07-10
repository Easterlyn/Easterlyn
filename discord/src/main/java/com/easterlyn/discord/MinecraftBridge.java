package com.easterlyn.discord;

import com.easterlyn.EasterlynCore;
import com.easterlyn.EasterlynChat;
import com.easterlyn.EasterlynDiscord;
import com.easterlyn.chat.event.UserChatEvent;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.event.SimpleListener;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public class MinecraftBridge {

	private final EasterlynDiscord plugin;
	private final DiscordClient client;
	private final Cache<Snowflake, Boolean> warnings;
	private final Pattern mention = Pattern.compile("<([@#]|:\\w+:)(\\d+)>");

	public MinecraftBridge(EasterlynDiscord plugin, DiscordClient client) {
		this.plugin = plugin;
		this.client = client;

		this.warnings = CacheBuilder.newBuilder().weakKeys().weakValues()
				.expireAfterWrite(2, TimeUnit.MINUTES).build();
	}

	public void setup() {
		PermissionUtil.addParent("easterlyn.command.unlogged", UserRank.MODERATOR.getPermission());

		client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {
			if (!event.getMessage().getAuthor().isPresent() || event.getMessage().getAuthor().get().isBot()) {
				return;
			}

			User author = event.getMessage().getAuthor().get();
			String msg = event.getMessage().getContent().orElse("");
			MessageChannel channel = event.getMessage().getChannel().block();

			if (channel == null) {
				return;
			}

			boolean command = msg.length() > 0 && msg.charAt(0) == '/';

			if (command) {
				if (channel instanceof TextChannel) {
					event.getMessage().delete();
				}
				// TODO command handling changes
//				if (plugin.handleDiscordCommand(msg, author, channel)) {
//					return;
//				}
			}

			boolean main = channel instanceof TextChannel && plugin.getChannelIDs(ChannelType.MAIN).stream()
					.anyMatch(mainChannel -> mainChannel.getId().equals(channel.getId()));

			if (!main && !command) {
				return;
			}

			DiscordUser sender = plugin.getUser(author.getId());
			if (sender == null) {
				Snowflake id = author.getId();
				if (warnings.getIfPresent(id) != null) {
					return;
				}
				warnings.put(id, true);
				channel.createMessage(author.getMention() + ", you must run /link in Minecraft to use this feature!");
				return;
			}

			if (command) {
				handleCommand(sender, msg, channel);
				return;
			}

			handleDiscordChat(sender, event.getMessage());
		});

		UserChatEvent.getHandlerList().register(new SimpleListener<>(UserChatEvent.class, event -> {
			// TODO may want to try to softdepend on chat instead of hard
			if (event.isAsynchronous()) {
				handleMinecraftChat(event);
			} else {
				// Ensure handling is done asynchronously even if chat is somehow sent on main thread
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> handleMinecraftChat(event));
			}
		}, plugin));

		PlayerCommandPreprocessEvent.getHandlerList().register(new SimpleListener<>(PlayerCommandPreprocessEvent.class, event -> {
			if (event.getPlayer().hasPermission("easterlyn.command.unlogged")) {
				return;
			}
			RegisteredServiceProvider<EasterlynCore> registration = plugin.getServer().getServicesManager().getRegistration(EasterlynCore.class);
			if (registration == null) {
				return;
			}

			int space = event.getMessage().indexOf(' ');
			String commandName = event.getMessage().substring(1, space > 0 ? space : event.getMessage().length()).toLowerCase();
			SimpleCommandMap commandMap = registration.getProvider().getSimpleCommandMap();
			if (commandMap == null) {
				return;
			}
			Command command = commandMap.getCommand(commandName);
			if (command != null && !plugin.getConfig().getStringList("command-log-blacklist").contains(command.getName())) {
				plugin.postMessage(ChannelType.LOG, event.getPlayer().getName() + " issued command: " + event.getMessage());
			}

		}, plugin, EventPriority.MONITOR));
	}

	private void handleCommand(DiscordUser user, String command, MessageChannel channel) {
		// TODO
	}

	private void handleDiscordChat(DiscordUser user, Message message) {
		if (!message.getContent().isPresent() && message.getAttachments().isEmpty() || !message.getAuthor().isPresent()) {
			return;
		}
		String content = message.getContent().orElse("");
		for (Attachment attachment : message.getAttachments()) {
			if (!content.isEmpty()) {
				content = content.concat(" ");
			}
			content = content.concat(attachment.getProxyUrl());
		}
		if (!user.hasPermission("easterlyn.discord.unfiltered")) {
			MessageChannel channel = null;
			if (content.indexOf('\n') > 0) {
				channel = message.getChannel().block();
				if (channel != null) {
					channel.createMessage("Newlines are not allowed in messages to Minecraft, "
							+ message.getAuthor().get().getMention());
				}
			} else if (content.length() > 255) {
				channel = message.getChannel().block();
				if (channel != null) {
					channel.createMessage("Messages from Discord may not be over 255 characters, "
							+ message.getAuthor().get().getMention());
				}
			}
			if (channel != null) {
				message.addReaction(ReactionEmoji.unicode(":no_entry_sign:"));
				return;
			}
		}

		new UserChatEvent(user, EasterlynChat.DEFAULT, sanitizeForMinecraft(content)).send();
	}

	private String sanitizeForMinecraft(String message) {
		Matcher matcher = mention.matcher(message);
		StringBuilder sb = new StringBuilder();
		int lastMatch = 0;
		while (matcher.find()) {
			String type = matcher.group(1);
			sb.append(message, lastMatch, matcher.start()).append(type);
			Snowflake id = Snowflake.of(matcher.group(2));
			if ("@".equals(type)) {
				DiscordUser user = plugin.getUser(id);
				if (user != null) {
					sb.append('@').append(user.getDisplayName());
				} else {
					sb.append(id);
				}
			} else if ("#".equals(type)) {
				Channel channel = client.getChannelById(id).block();
				if (channel instanceof GuildChannel) {
					sb.append(((GuildChannel) channel).getName());
				} else {
					sb.append(id);
				}
			} else if (type.length() < 1 || type.charAt(0) != ':') {
				// Skip custom emoji, matcher group 1 is the full emoji name.
				sb.append(id);
			}
			lastMatch = matcher.end();
		}
		sb.append(message.substring(lastMatch));
		return sb.toString();
	}

	private void handleMinecraftChat(UserChatEvent event) {
		StringBuilder builder = new StringBuilder();
		if (event.isThirdPerson()) {
			builder.append("* ");
		}
		builder.append('`').append(event.getUser().getDisplayName()).append("`");
		if (!event.isThirdPerson()) {
			builder.append(": ");
		}
		builder.append(ChatColor.stripColor(event.getMessage())
				.replace("@everyone", "@Everyone").replace("@here", "@Here"));

		String message = builder.toString();

		if (event.getChannel().getName().equals(EasterlynChat.DEFAULT.getName())) {
			plugin.postMessage(ChannelType.MAIN, message);
		}

		plugin.postMessage(ChannelType.LOG, message);

	}

}
