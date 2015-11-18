package co.sblock.discord;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.module.Module;
import co.sblock.utilities.DiscordPlayer;

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
	private ConcurrentLinkedQueue<Triple<String, String, String>> queue;

	@Override
	protected void onEnable() {
		instance = this;

		// TODO don't store as plaintext
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
		final Server server = discord.getServerById(sblock.getConfig().getString("discord.server"));

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

	protected Player getPlayerFor(GroupUser user) {
		// FIXME get user's groups and set permissions
		return new DiscordPlayer(user.getUser().getUsername(), user.getRole());
	}

	public static Discord getInstance() {
		return instance;
	}

}
