package co.sblock.utilities.messages;

import java.io.IOException;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import co.sblock.Sblock;
import co.sblock.module.Module;

/**
 * 
 * 
 * @author Jikoo
 */
public class Slack extends Module {

	private static Slack instance;

	private SlackSession session;
	private LinkedList<SlackMessageWrapper> toPost;

	/**
	 * @see co.sblock.module.Module#onEnable()
	 */
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
				// TODO handle messages from Slack
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

	public synchronized void postMessage(SlackMessageWrapper message) {
		toPost.add(message);
	}

	public synchronized void postMessage(String name, String message, boolean global) {
		postMessage(name, null, message, global);
	}

	public synchronized void postMessage(String name, UUID uuid, String message, boolean global) {
		if (global) {
			toPost.add(new SlackMessageWrapper(Sblock.getInstance().getConfig().getString("main-chat"), message, uuid, name));
		}
		toPost.add(new SlackMessageWrapper(Sblock.getInstance().getConfig().getString("full-log"), message, uuid, name));
	}

	/**
	 * @see co.sblock.module.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		session = null;
		if (toPost != null) {
			toPost.clear();
			toPost = null;
		}
		instance = null;
	}

	/**
	 * @see co.sblock.module.Module#getModuleName()
	 */
	@Override
	protected String getModuleName() {
		return "Slack";
	}

	public static Slack getInstance() {
		return instance;
	}
}
