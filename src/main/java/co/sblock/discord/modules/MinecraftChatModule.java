package co.sblock.discord.modules;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.Chat;
import co.sblock.chat.Color;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.discord.Discord;
import co.sblock.discord.DiscordPlayer;
import co.sblock.discord.abstraction.DiscordModule;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.users.Users;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.HTTP429Exception;

/**
 * DiscordModule for interacting with the Minecraft server from Discord.
 * 
 * @author Jikoo
 */
public class MinecraftChatModule extends DiscordModule {

	private final Users users;
	private final ChannelManager manager;
	private final MessageBuilder builder;
	private final Pattern mention = Pattern.compile("<([@#])(\\d+)>");

	public MinecraftChatModule(Discord discord) {
		super(discord);

		this.users = discord.getPlugin().getModule(Users.class);
		this.manager = discord.getPlugin().getModule(Chat.class).getChannelManager();

		// future modify MessageBuilder to allow custom name clicks (OPEN_URL www.sblock.co/discord)
		this.builder = new MessageBuilder(discord.getPlugin()).setNameClick("@# ").setChannelClick("@# ")
				.setChannel(this.manager.getChannel("#discord"))
				.setNameHover(TextComponent.fromLegacyText(Color.GOOD_EMPHASIS + "Discord Chat\n"
						+ ChatColor.BLUE + ChatColor.UNDERLINE + "www.sblock.co/discord\n"
						+ Color.GOOD + "Channel: #main"));
	}

	@Override
	public void doHeartbeat() { }

	public void handleCommand(DiscordPlayer player, String command, IChannel channel) {
		if (player.hasPendingCommand()) {
			getDiscord().postMessage(Discord.BOT_NAME, "You already have a pending command. Please be patient.", channel.getID());
			return;
		}
		Future<Boolean> future = Bukkit.getScheduler().callSyncMethod(getDiscord().getPlugin(),
				new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						player.startMessages();
						PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, command);
						Bukkit.getPluginManager().callEvent(event);
						getDiscord().getLogger().info(event.getPlayer().getName() + " issued server command: " + event.getMessage());
						return !event.isCancelled() && Bukkit.dispatchCommand(player, event.getMessage().substring(1));
					}
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
					getDiscord().postMessage(Discord.BOT_NAME, "Command " + command + " from " + player.getName() + " timed out.", channel.getID());
					player.stopMessages();
					return;
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) { }
				String message = player.stopMessages();
				if (message.isEmpty()) {
					return;
				}
				getDiscord().postMessage(Discord.BOT_NAME, message, channel.getID());
			}
		}.runTaskAsynchronously(getDiscord().getPlugin());
	}

	public void handleChat(IMessage message, Player player) {
		String content = message.getContent();
		if (!player.hasPermission("sblock.discord.filterexempt")) {
			int newline = content.indexOf('\n');
			boolean delete = false;
			if (newline > 0) {
				getDiscord().postMessage(Discord.BOT_NAME, "Newlines are not allowed in messages to Minecraft, <@"
						+ message.getAuthor().getID() + ">", message.getChannel().getID());
				delete = true;
			} else if (content.length() > 255) {
				getDiscord().postMessage(Discord.BOT_NAME, "Messages from Discord may not be over 255 characters, <@"
						+ message.getAuthor().getID() + ">", message.getChannel().getID());
				delete = true;
			}
			if (delete) {
				try {
					message.delete();
				} catch (MissingPermissionsException | HTTP429Exception | DiscordException e) {
					// Trivial
				}
			}
		}
		builder.setSender(users.getUser(player.getUniqueId()))
				.setMessage(sanitizeForMinecraft(content)).setChannel(manager.getChannel("#discord"));
		if (!builder.canBuild(false)) {
			try {
				message.delete();
			} catch (MissingPermissionsException | HTTP429Exception | DiscordException e) {
				// Trivial
			}
			return;
		}
		Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());
		players.removeIf(p -> users.getUser(p.getUniqueId()).getSuppression());
		Bukkit.getPluginManager().callEvent(new SblockAsyncChatEvent(true, player, players, builder.toMessage()));
	}

	private String sanitizeForMinecraft(String message) {
		Matcher matcher = mention.matcher(message);
		StringBuilder sb = new StringBuilder();
		int lastMatch = 0;
		while (matcher.find()) {
			sb.append(message.substring(lastMatch, matcher.start())).append(matcher.group(1));
			String id = matcher.group(2);
			IUser user = getDiscord().getClient().getUserByID(id);
			if (user == null) {
				sb.append(id);
			} else {
				sb.append(getDiscord().getClient().getUserByID(id).getName());
			}
			lastMatch = matcher.end();
		}
		sb.append(message.substring(lastMatch));
		return sb.toString();
	}

}
