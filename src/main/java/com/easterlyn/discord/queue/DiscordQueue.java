package com.easterlyn.discord.queue;

import com.easterlyn.discord.Discord;
import com.easterlyn.utilities.tuple.Wrapper;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.regex.Pattern;

/**
 * A Thread for periodically executing DiscordCallables from the given Queue.
 *
 * @author Jikoo
 */
public class DiscordQueue extends Thread {

	// TODO: Move all special queuing handling inside DiscordQueue

	private static final Pattern POTENTIAL_MARKDOWN = Pattern.compile("([_~*])");

	private final Discord discord;
	private final Map<Long, Map<CallType, Queue<DiscordCallable>>> guildQueues;
	private final Map<Long, Map<CallType, Long>> rateLimiting;
	private final long delay;

	private long globalRateLimit = 0;
	private long lastSuccess = System.currentTimeMillis();

	public DiscordQueue(Discord discord, long delay, String threadName) {
		super(threadName);
		this.discord = discord;
		this.guildQueues = new ConcurrentHashMap<>();
		this.rateLimiting = new ConcurrentHashMap<>();
		this.delay = delay;
	}

	@Override
	public void run() {

		while (discord.isEnabled()) {

			if (!discord.isReady()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
				continue;
			}

			long now = System.currentTimeMillis();

			if (!discord.getClient().isReady()) {
				if (lastSuccess - now > 60000) {
					// Bot has been offline for a full minute and reconnection has failed.
					break;
				}
			}

			if (globalRateLimit > now) {
				try {
					Thread.sleep(globalRateLimit - now);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}

			// Bot is connected and ready, we're good, even if an error is thrown by a call.
			lastSuccess = System.currentTimeMillis();

			guildQueues.forEach((guild, guildMap) -> {
				if (globalRateLimit > now) {
					return;
				}
				guildMap.forEach((callType, callTypeQueue) -> {
					if (globalRateLimit > now) {
						return;
					}
					poll(guild, callType, callTypeQueue);
				});
			});

			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	private void poll(long guild, CallType type, Queue<DiscordCallable> queue) {

		if (queue.isEmpty() || !discord.getClient().isReady()) {
			return;
		}

		if (isRateLimited(guild, type)) {
			return;
		}

		DiscordCallable callable = queue.remove();

		try {
			callable.call();
		} catch (RateLimitException e) {
			if (e.isGlobal()) {
				globalRateLimit = e.getRetryDelay() + System.currentTimeMillis() + 100L;
			}
			this.addRateLimit(guild, type, e.getRetryDelay() + 100L);
			// Re-queue rate limited call
			queue.add(callable);
			return;
		} catch (DiscordException e) {
			if (e.getErrorMessage().startsWith("502 error")
					&& e.getErrorMessage().contains("CloudFlare") || callable.retryOnException()) {
				/*
				 * Rather than skip removal in this case to preserve order, we re-add the
				 * DiscordCallable. This does ruin order in the case of messages sent, however, sent
				 * messages do not currently retry. It also allows us to modify the queue from
				 * inside the callables safely.
				 */
				queue.add(callable);
				// Don't log when retrying, we only retry because of a Discord4J fault generally.
			}
			e.printStackTrace();
		} catch (Exception e) {
			// Likely permissions, but can be malformed JSON when odd responses are received
			e.printStackTrace();
		}

		if (callable.getChainedCall() != null) {
			this.queue(callable.getChainedCall());
		}

		// Always rate limit so as to prevent global cap from being hit
		this.addRateLimit(guild, type, type.getRateLimit() + 100L);
	}

	private void addRateLimit(Long guild, CallType type, Long duration) {
		this.rateLimiting.compute(guild, (entryGuild, entryCallTypeMap) -> {
			if (entryCallTypeMap == null) {
				entryCallTypeMap = new ConcurrentHashMap<>();
			}
			entryCallTypeMap.put(type, duration + System.currentTimeMillis());
			return entryCallTypeMap;
		});
	}

	private boolean isRateLimited(Long guild, CallType call) {
		return this.rateLimiting.containsKey(guild) && this.rateLimiting.get(guild)
				.getOrDefault(call, 0L) >= System.currentTimeMillis();
	}

	public DiscordCallable queue(DiscordCallable callable) {
		if (!this.isAlive()) {
			return callable;
		}

		Wrapper<DiscordCallable> callableWrapper = new Wrapper<>();
		callableWrapper.set(callable);

		this.guildQueues.compute(callable.getGuildID(), (guild, guildMap) -> {
			if (guildMap == null) {
				guildMap = new ConcurrentHashMap<>();
			}
			guildMap.compute(callable.getCallType(), (callType, callTypeQueue) -> {
				if (callTypeQueue == null) {
					callTypeQueue = new PriorityBlockingQueue<>();
				}
				if (callType == CallType.MESSAGE_SEND && callable instanceof DiscordMessageCallable) {
					DiscordCallable next = callTypeQueue.peek();
					if (next instanceof DiscordMessageCallable) {
						if (((DiscordMessageCallable) next).addMessage(((DiscordMessageCallable) callable))) {
							callableWrapper.set(next);
							return callTypeQueue;
						}
					}
				}
				if (!callTypeQueue.contains(callable)) {
					callTypeQueue.add(callable);
				}
				return callTypeQueue;
			});
			return guildMap;
		});

		return callableWrapper.get();
	}

	public DiscordCallable queueMessage(IChannel channel, @Nullable String name, String message) {
		return this.queue(new DiscordMessageCallable(channel, name, message));
	}

	private class DiscordMessageCallable extends DiscordCallable {
		private final StringBuffer content = new StringBuffer();
		private final IChannel channel;

		public DiscordMessageCallable(IChannel channel, @Nullable String name, String message) {
			super(channel, CallType.MESSAGE_SEND);
			this.channel = channel;
			this.addMessage(name, message);
		}

		@Override
		public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
			try {
				this.channel.sendMessage(this.content.toString());
			} catch (NoSuchElementException e) {
				// Internal Discord fault, don't log.
			}
		}

		public void addMessage(@Nullable String name, String message) {
			StringBuilder assembledMessage = new StringBuilder();
			if (this.content.length() > 0) {
				assembledMessage.append('\n');
			}
			if (name != null) {
				assembledMessage.append("**").append(POTENTIAL_MARKDOWN.matcher(name).replaceAll("\\\\$1"));
				if (!name.startsWith("* ")) {
					assembledMessage.append(':');
				}
				assembledMessage.append("** ");
			}
			assembledMessage.append(message);
			this.content.append(assembledMessage.toString());
		}

		public boolean addMessage(DiscordMessageCallable callable) {
			for (DiscordCallable chained = this;; chained = chained.getChainedCall()) {
				if (chained == null) {
					return false;
				}
				if (!(chained instanceof DiscordMessageCallable)) {
					continue;
				}
				if (((DiscordMessageCallable) chained).channel.getLongID() == callable.channel.getLongID()) {
					StringBuffer buffer = ((DiscordMessageCallable) chained).content;
					if (buffer.length() > 0) {
						buffer.append('\n');
					}
					buffer.append(callable.content.toString());
					return true;
				}
			}
		}
	}

}
