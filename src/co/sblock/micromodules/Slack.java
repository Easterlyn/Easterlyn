package co.sblock.micromodules;

import java.io.IOException;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import co.sblock.Sblock;
import co.sblock.commands.chat.AetherCommand;
import co.sblock.module.Module;

/**
 * A Module for logging/reporting to our Slack team.
 * 
 * @author Jikoo
 */
public class Slack extends Module {

	private static Slack instance;

	private SlackSession session;
	private LinkedList<SlackMessageWrapper> toPost;

	@Override
	protected void onEnable() {
		instance = this;

		String token = Sblock.getInstance().getConfig().getString("slack.auth-token");
		if (token == null) {
			getLogger().warning("No token provided in config.yml under slack.auth-token!");
			return;
		}
		session = SlackSessionFactory.createWebSocketSlackSession(token);
		session.addMessagePostedListener(new SlackMessagePostedListener() {
			@Override
			public void onEvent(SlackMessagePosted event, SlackSession session) {
				if (event.getSender() == null) {
					// Ignore anything by a bot.
					return;
				}
				if (event.getSender().getId().equals(session.sessionPersona().getId())) {
					// This session, ignore.
					return;
				}
				if (event.getChannel().isDirect()) {
					// Private message to the bot, ignore
					return;
				}
				if (event.getChannel().getName().equals(getMainChat())) {
					AetherCommand.sendAether(event.getSender().getUserName(), event.getMessageContent());
					return;
				}
				if (event.getChannel().getName().equals(getFullChat())) {
					// Admin command
					// TODO allow a whitelist of commands + grab output for Slack
				}
				getLogger().info(event.getSender().getUserName() + ": " + event.getMessageContent());
			}
		});

		try {
			session.connect();
		} catch (IOException e) {
			getLogger().warning("Error connecting to Slack!");
			session = null;
		}

		toPost = new LinkedList<>();

		new BukkitRunnable() {
			@Override
			public void run() {
				if (session == null) {
					this.cancel();
					return;
				}
				if (toPost.size() == 0) {
					return;
				}
				SlackMessageWrapper wrapper = toPost.removeFirst();
				new BukkitRunnable() {
					@Override
					public void run() {
						SlackChannel channel = session.findChannelByName(wrapper.getChannel());
						if (channel == null) {
							return;
						}
						session.sendMessage(channel, wrapper.getMessage(), null, wrapper.getConfiguration());
					}
				}.runTaskAsynchronously(Sblock.getInstance());
			}
		}.runTaskTimer(Sblock.getInstance(), 0, 20);
	}

	public synchronized void postMessage(String name, String message, boolean global) {
		postMessage(name, null, message, global);
	}

	public synchronized void postMessage(UUID uuid, String message, boolean global) {
		postMessage(null, uuid, message, global);
	}

	public synchronized void postMessage(String name, UUID uuid, String message, boolean global) {
		if (!isEnabled()) {
			return;
		}
		if (global) {
			postMessage(name, uuid, message, getMainChat(), getFullChat());
		} else {
			postMessage(name, uuid, message, getFullChat());
		}
	}

	public synchronized void postMessage(String name, UUID uuid, String message, String... channels) {
		if (!isEnabled()) {
			return;
		}
		if (name == null) {
			OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
			if (offline.hasPlayedBefore()) {
				name = offline.getName();
			} else {
				name = uuid.toString();
			}
		}
		for (String channel : channels) {
			toPost.add(new SlackMessageWrapper(channel, message, uuid, name));
		}
	}

	public synchronized void postReport(String name, UUID uuid, String message) {
		postMessage(name, uuid, message, getReportChat());
	}

	public String getMainChat() {
		return Sblock.getInstance().getConfig().getString("slack.main-chat");
	}

	public String getFullChat() {
		return Sblock.getInstance().getConfig().getString("slack.full-log");
	}

	public String getReportChat() {
		return Sblock.getInstance().getConfig().getString("slack.reports");
	}

	@Override
	protected void onDisable() {
		session = null;
		if (toPost != null) {
			toPost.clear();
			toPost = null;
		}
		instance = null;
	}

	public static Slack getInstance() {
		return instance;
	}

	@Override
	protected String getModuleName() {
		return "Slack";
	}
}
