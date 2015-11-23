package co.sblock.discord;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.HashBiMap;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.Chat;
import co.sblock.chat.Color;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.module.Module;
import co.sblock.users.Users;
import co.sblock.utilities.PlayerLoader;

import me.itsghost.jdiscord.DiscordAPI;
import me.itsghost.jdiscord.DiscordBuilder;
import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.exception.BadUsernamePasswordException;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.exception.NoLoginDetailsException;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;
import me.itsghost.jdiscord.talkable.User;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * A Module for managing messaging to and from Discord.
 * 
 * @author Jikoo
 */
public class Discord extends Module {

	private DiscordAPI discord;
	private ConcurrentLinkedQueue<Triple<String, String, String>> queue;
	private HashBiMap<UUID, String> authentications;
	private BaseComponent[] hover;
	private Pattern mention;
	private ChannelManager manager;

	public Discord(Sblock plugin) {
		super(plugin);
	}

	@Override
	protected void onEnable() {
		String login = getPlugin().getConfig().getString("discord.login");
		String password = getPlugin().getConfig().getString("discord.password");

		if (login == null || password == null) {
			getLogger().severe("Unable to connect to Discord, no username or password!");
			disable();
			return;
		}

		discord = new DiscordBuilder(login, password).build();
		discord.getEventManager().registerListener(new DiscordLoadedListener(this, discord));

		try {
			discord.login();
		} catch (NoLoginDetailsException | BadUsernamePasswordException | DiscordFailedToConnectException e) {
			e.printStackTrace();
			discord = null;
			disable();
			return;
		}

		manager = getPlugin().getModule(Chat.class).getChannelManager();;

		authentications = HashBiMap.create();
		hover = TextComponent.fromLegacyText(Color.GOOD_EMPHASIS + "Discord Chat\n"
				+ ChatColor.BLUE + ChatColor.UNDERLINE + "www.sblock.co/discord\n"
				+ Color.GOOD + "Channel: #main");
		mention = Pattern.compile("<@(\\d+)>");
		discord.getEventManager().registerListener(new DiscordListener(this, discord));

		queue = new ConcurrentLinkedQueue<>();
	}

	protected void startPostingMessages() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (discord == null) {
					cancel();
					return;
				}
				if (queue.isEmpty()) {
					return;
				}
				Triple<String, String, String> triple = queue.poll();
				Group group = discord.getGroupById(triple.getLeft());
				if (group == null) {
					return;
				}
				discord.getSelfInfo().setUsername(triple.getMiddle());
				group.sendMessage(triple.getRight());
			}
		}.runTaskTimerAsynchronously(getPlugin(), 20L, 20L);
	}

	public void logMessage(String message) {
		postMessage("Sbot", message, getPlugin().getConfig().getString("discord.chat.log"));
	}

	public void postMessage(Message message, boolean global) {
		FileConfiguration config = getPlugin().getConfig();
		if (global) {
			postMessage(message.getSenderName(), message.getDiscordMessage(),
					config.getString("discord.chat.main"));
		}
		postMessage(message.getSenderName(), message.getConsoleMessage(),
				config.getString("discord.chat.log"));
	}

	public void postMessage(String name, String message, boolean global) {
		FileConfiguration config = getPlugin().getConfig();
		if (global) {
			postMessage(name, message, config.getString("discord.chat.main"),
					config.getString("discord.chat.log"));
		} else {
			postMessage(name, message, config.getString("discord.chat.log"));
		}
	}

	public void postMessage(String name, String message, String... channels) {
		if (!isEnabled()) {
			return;
		}
		name = ChatColor.stripColor(name);
		message = ChatColor.stripColor(message);
		for (String channel : channels) {
			queue.add(new ImmutableTriple<>(channel, name, message));
		}
	}

	public void postReport(String name, String message) {
		postMessage(name, message, getPlugin().getConfig().getString("discord.chat.reports"));
	}

	public HashBiMap<UUID, String> getAuthCodes() {
		return authentications;
	}

	@Override
	protected void onDisable() {
		if (discord != null) {
			discord.stop();
			discord = null;
		}
	}

	@Override
	public String getName() {
		return "Discord";
	}

	protected DiscordPlayer getPlayerFor(GroupUser user) {
		String uuidString = getPlugin().getConfig().getString("discord.users." + user.getUser().getId());
		if (uuidString == null) {
			return null;
		}
		UUID uuid = UUID.fromString(uuidString);
		Player player = PlayerLoader.getPlayer(uuid);
		if (player instanceof DiscordPlayer) {
			return (DiscordPlayer) player;
		}
		// PlayerLoader loads a PermissiblePlayer, wrapping a wrapper would be silly.
		DiscordPlayer dplayer = new DiscordPlayer(this, user, player.getPlayer());
		PlayerLoader.modifyCachedPlayer(dplayer);
		return dplayer;
	}

	protected void handleCommandFor(DiscordPlayer player, String command, Group group) {
		if (player.hasPendingCommand()) {
			postMessage("Sbot", "You alread have a pending command. Please be patient.", group.getId());
			return;
		}
		Future<Boolean> future = Bukkit.getScheduler().callSyncMethod(getPlugin(),
				new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						player.startMessages();
						PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/" + command);
						Bukkit.getPluginManager().callEvent(event);
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
					postMessage("Sbot", "Command " + command + " from " + player.getName() + " timed out.", group.getId());
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
				postMessage("Sbot", message, group.getId());
			}
		}.runTaskAsynchronously(getPlugin());
	}

	protected void postMessageFor(UserChatEvent event, Player player) {
		Channel channel = manager.getChannel("#discord");
		// future re-use MessageBuilder
		Message message = new MessageBuilder(getPlugin()).setChannel(channel)
				.setSender(Users.getGuaranteedUser(getPlugin(), player.getUniqueId()))
				.setMessage(sanitize(event.getMsg())).setChannel(channel).setNameHover(hover)
				.setNameClick("@# ").setChannelClick("@# ").toMessage();
		// TODO MessageBuilder method overloading: add different click events (open Discord link on name click)
		Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());
		players.removeIf(p -> Users.getGuaranteedUser(getPlugin(), p.getUniqueId()).getSuppression());
		Bukkit.getPluginManager().callEvent(new SblockAsyncChatEvent(true, player, players, message));
	}

	private String sanitize(me.itsghost.jdiscord.message.Message msg) {
		String message = msg.getMessage();
		Matcher matcher = mention.matcher(message);
		StringBuilder sb = new StringBuilder();
		int lastMatch = 0;
		while (matcher.find()) {
			sb.append(message.substring(lastMatch, matcher.start())).append('@');
			String id = matcher.group(1);
			User user = discord.getUserById(id);
			if (user == null) {
				sb.append(id);
			} else {
				sb.append(discord.getUserById(id).getUsername());
			}
			lastMatch = matcher.end();
		}
		sb.append(message.substring(lastMatch));
		return sb.toString();
	}

	public ChatColor getGroupColor(GroupUser user) {
		// future when jDiscord updates, multiple roles will be supported
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
