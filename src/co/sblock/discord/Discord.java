package co.sblock.discord;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.HashBiMap;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.module.Module;
import co.sblock.utilities.PlayerLoader;

import me.itsghost.jdiscord.DiscordAPI;
import me.itsghost.jdiscord.DiscordBuilder;
import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.exception.BadUsernamePasswordException;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.exception.NoLoginDetailsException;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;

import net.md_5.bungee.api.ChatColor;

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

		discord.getEventManager().registerListener(new DiscordListener(this, discord));
		queue = new ConcurrentLinkedQueue<>();
		authentications = HashBiMap.create();
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
		if (uuidString != null) {
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
