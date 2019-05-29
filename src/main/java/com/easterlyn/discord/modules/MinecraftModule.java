package com.easterlyn.discord.modules;

import com.easterlyn.chat.ChannelManager;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.Language;
import com.easterlyn.chat.message.MessageBuilder;
import com.easterlyn.discord.Discord;
import com.easterlyn.discord.DiscordPlayer;
import com.easterlyn.discord.abstraction.DiscordModule;
import com.easterlyn.discord.queue.CallPriority;
import com.easterlyn.events.event.EasterlynAsyncChatEvent;
import com.easterlyn.users.UserRank;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.player.PermissionUtils;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DiscordModule for interacting with the Minecraft server from Discord.
 *
 * @author Jikoo
 */
public class MinecraftModule extends DiscordModule {

	private final Users users;
	private final ChannelManager manager;
	private final MessageBuilder builder;
	private final Pattern mention = Pattern.compile("<([@#])(\\d+?)>");

	public MinecraftModule(Discord discord) {
		super(discord);

		this.users = discord.getPlugin().getModule(Users.class);
		this.manager = discord.getPlugin().getModule(Chat.class).getChannelManager();

		// Permission to bypass Discord message filtering (can truly be horrific)
		PermissionUtils.addParent("easterlyn.discord.unfiltered", UserRank.ADMIN.getPermission());

		// future modify MessageBuilder to allow custom name clicks (OPEN_URL discord invite)
		this.builder = new MessageBuilder(discord.getPlugin()).setNameClick("@# ")
				.setChannelClick("@# ").setChannel(this.manager.getChannel("#discord"))
				.setNameHover(TextComponent.fromLegacyText(discord.getPlugin()
						.getModule(Language.class).getValue("chat.user.discordHover")));
	}

	@Override
	public void doSetup() { }

	@Override
	public void doHeartbeat() { }

	public void handleCommand(DiscordPlayer player, String command, IChannel channel) {
		if (player.hasPendingCommand()) {
			getDiscord().postMessage(null, "You already have a pending command. Please be patient.", channel.getLongID());
			return;
		}
		Future<Boolean> future = Bukkit.getScheduler().callSyncMethod(getDiscord().getPlugin(),
				() -> {
					player.startMessages();
					PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, command);
					Bukkit.getPluginManager().callEvent(event);
					getDiscord().getLogger().info(event.getPlayer().getName() + " issued server command: " + event.getMessage());
					return !event.isCancelled() && Bukkit.dispatchCommand(player, event.getMessage().substring(1));
				});

		new BukkitRunnable() {
			@Override
			public void run() {
				int count = 0;
				while (!future.isDone() && count < 20) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						future.cancel(false);
						break;
					}
					count++;
				}
				if (future.isCancelled() || !future.isDone()) {
					getDiscord().postMessage(null, "Command " + command + " from " + player.getName() + " timed out.", channel.getLongID());
					player.stopMessages();
					return;
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException ignored) {}
				String message = player.stopMessages();
				if (message.isEmpty()) {
					return;
				}
				getDiscord().postMessage(null, message, channel.getLongID());
			}
		}.runTaskAsynchronously(getDiscord().getPlugin());
	}

	public void handleChat(IMessage message, Player player) {
		String content = message.getContent();
		if (!player.hasPermission("easterlyn.discord.unfiltered")) {
			int newline = content.indexOf('\n');
			boolean delete = false;
			if (newline > 0) {
				getDiscord().postMessage(null, "Newlines are not allowed in messages to Minecraft, "
						+ message.getAuthor().mention(), message.getChannel().getLongID());
				delete = true;
			} else if (content.length() > 255) {
				getDiscord().postMessage(null, "Messages from Discord may not be over 255 characters, "
						+ message.getAuthor().mention(), message.getChannel().getLongID());
				delete = true;
			}
			if (delete) {
				if (!message.getChannel().isPrivate()) {
					getDiscord().queueMessageDeletion(CallPriority.LOW, message);
				}
				return;
			}
		}
		builder.setSender(users.getUser(player.getUniqueId()))
				.setMessage(sanitizeForMinecraft(content)).setChannel(manager.getChannel("#discord"));
		if (builder.canNotBuild(false)) {
			if (!message.getChannel().isPrivate()) {
				getDiscord().queueMessageDeletion(CallPriority.LOW, message);
			}
			return;
		}
		Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());
		players.removeIf(p -> users.getUser(p.getUniqueId()).getSuppression());
		Bukkit.getPluginManager().callEvent(new EasterlynAsyncChatEvent(true, player, players, builder.toMessage()));
	}

	private String sanitizeForMinecraft(String message) {
		Matcher matcher = mention.matcher(message);
		StringBuilder sb = new StringBuilder();
		int lastMatch = 0;
		while (matcher.find()) {
			String type = matcher.group(1);
			sb.append(message, lastMatch, matcher.start()).append(type);
			long id = Long.parseLong(matcher.group(2));
			if ("@".equals(type)) {
				IUser user = getDiscord().getClient().getUserByID(id);
				if (user != null) {
					sb.append(user.getName());
				} else {
					sb.append(id);
				}
			} else if ("#".equals(type)) {
				IChannel channel = getDiscord().getClient().getChannelByID(id);
				if (channel != null) {
					sb.append(channel.getName());
				} else {
					sb.append(id);
				}
			} else {
				sb.append(id);
			}
			lastMatch = matcher.end();
		}
		sb.append(message.substring(lastMatch));
		return sb.toString();
	}

}
