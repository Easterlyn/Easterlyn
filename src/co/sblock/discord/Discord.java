package co.sblock.discord;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.HashBiMap;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
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
import me.itsghost.jdiscord.Server;
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

	private static Discord instance;

	private DiscordAPI discord;
	private Server server;
	private ConcurrentLinkedQueue<Triple<String, String, String>> queue;
	private HashBiMap<UUID, String> authentications;
	private BaseComponent[] hover;
	private Pattern mention;

	@Override
	protected void onEnable() {
		instance = this;
		Sblock sblock = Sblock.getInstance();
		String login = sblock.getConfig().getString("discord.login");
		String password = sblock.getConfig().getString("discord.password");

		if (login == null || password == null) {
			getLogger().severe("Unable to connect to Discord, no username or password!");
			this.disable();
			return;
		}

		this.discord = new DiscordBuilder(login, password).build();

		try {
			this.discord.login();
		} catch (NoLoginDetailsException | BadUsernamePasswordException | DiscordFailedToConnectException e) {
			e.printStackTrace();
			this.discord = null;
			this.disable();
			return;
		}
		while (!discord.isLoaded()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		StringBuilder sb = new StringBuilder();
		for (Server server : discord.getAvailableServers()) {
			getLogger().info("Available channels in " + server.getName() + " (" + server.getId() + "):");
			for (Group group : server.getGroups()) {
				sb.append(group.getName()).append(':').append(group.getId()).append(' ');
			}
			sb.deleteCharAt(sb.length() - 1);
			getLogger().info(sb.toString());
			sb.delete(0, sb.length());
		}
		sb = null;

		authentications = HashBiMap.create();
		hover = TextComponent.fromLegacyText(Color.GOOD_EMPHASIS + "Discord Chat\n"
				+ ChatColor.BLUE + ChatColor.UNDERLINE + "www.sblock.co/discord\n"
				+ Color.GOOD + "Channel: #main");
		mention = Pattern.compile("<@(\\d+)>");
		discord.getEventManager().registerListener(new DiscordListener(this, discord));

		queue = new ConcurrentLinkedQueue<>();
		server = discord.getServerById(sblock.getConfig().getString("discord.server"));

		new BukkitRunnable() {
			@Override
			public void run() {
				if (discord == null || server == null) {
					this.cancel();
					return;
				}
				if (queue.isEmpty()) {
					return;
				}
				Triple<String, String, String> triple = queue.poll();
				Group group = null;
				for (Group visible : server.getGroups()) {
					if (visible.getId().equals(triple.getLeft())) {
						group = visible;
						break;
					}
				}
				if (group == null) {
					return;
				}
				discord.getSelfInfo().setUsername(triple.getMiddle());
				group.sendMessage(triple.getRight());
			}
		}.runTaskTimerAsynchronously(sblock, 20L, 20L);
	}

	public void postMessage(String name, String message, boolean global) {
		if (discord == null) {
			return;
		}
		Sblock sblock = Sblock.getInstance();
		if (global) {
			postMessage(name, message, sblock.getConfig().getString("discord.chat.main"),
					sblock.getConfig().getString("discord.chat.log"));
		} else {
			postMessage(name, message, sblock.getConfig().getString("discord.chat.log"));
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
		postMessage(name, message, Sblock.getInstance().getConfig().getString("discord.chat.reports"));
	}

	public HashBiMap<UUID, String> getAuthCodes() {
		return authentications;
	}

	@Override
	protected void onDisable() {
		if (this.discord != null) {
			this.discord.stop();
			this.discord = null;
		}
		instance = null;
	}

	@Override
	protected String getModuleName() {
		return "Discord";
	}

	protected DiscordPlayer getPlayerFor(GroupUser user) {
		String uuidString = Sblock.getInstance().getConfig().getString("discord.users." + user.getUser().getId());
		if (uuidString == null) {
			return null;
		}
		UUID uuid = UUID.fromString(uuidString);
		Player player = PlayerLoader.getPlayer(uuid);
		if (player instanceof DiscordPlayer) {
			return (DiscordPlayer) player;
		}
		// PlayerLoader loads a PermissiblePlayer, wrapping a wrapper would be silly.
		DiscordPlayer dplayer = new DiscordPlayer(user, player.getPlayer());
		PlayerLoader.modifyCachedPlayer(dplayer);
		return dplayer;
	}

	protected void handleCommandFor(DiscordPlayer player, String command, Group group) {
		Future<String> future = Bukkit.getScheduler().callSyncMethod(Sblock.getInstance(),
				new Callable<String>() {
					@Override
					public String call() throws Exception {
						player.startMessages();
						PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/" + command);
						Bukkit.getPluginManager().callEvent(event);
						if (!event.isCancelled()) {
							Bukkit.dispatchCommand(player, command);
						}
						return ChatColor.stripColor(player.stopMessages());
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
					group.sendMessage("Command " + command + " from " + player.getName() + " timed out.");
					return;
				}
				try {
					group.sendMessage(future.get());
				} catch (InterruptedException | ExecutionException e) {
					group.sendMessage("Command " + command + " from " + player.getName() + " encountered an error.");
				}
			}
		}.runTaskAsynchronously(Sblock.getInstance());
	}

	protected void postMessageFor(UserChatEvent event, Player player) {
		Channel channel = ChannelManager.getChannelManager().getChannel("#discord");
		Message message = new MessageBuilder().setChannel(channel)
				.setSender(Users.getGuaranteedUser(player.getUniqueId()))
				.setMessage(sanitize(event.getMsg())).setChannel(channel).setNameHover(hover)
				.setNameClick("@# ").setChannelClick("@# ").toMessage();
		// TODO MessageBuilder method overloading: add different click events (open Discord link on name click)
		Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());
		players.removeIf(p -> Users.getGuaranteedUser(p.getUniqueId()).getSuppression());
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

	public static Discord getInstance() {
		return instance;
	}

}
