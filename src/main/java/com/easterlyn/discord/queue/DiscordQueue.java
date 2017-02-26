package com.easterlyn.discord.queue;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import com.easterlyn.discord.Discord;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

/**
 * A Thread for periodically executing DiscordCallables from the given Queue.
 * 
 * @author Jikoo
 */
public class DiscordQueue extends Thread {

	private final Discord discord;
	private final Map<String, Map<CallType, Queue<DiscordCallable>>> guildQueues;
	private final Map<String, Map<CallType, Long>> rateLimiting;
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

	private void poll(String guild, CallType type, Queue<DiscordCallable> queue) {

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

		// Always rate limit so as to prevent global cap from being hit
		this.addRateLimit(guild, type, type.getRateLimit() + 100L);
	}

	private void addRateLimit(String guild, CallType type, Long duration) {
		this.rateLimiting.compute(guild, (entryGuild, entryCallTypeMap) -> {
			if (entryCallTypeMap == null) {
				entryCallTypeMap = new ConcurrentHashMap<>();
			}
			entryCallTypeMap.put(type, duration + System.currentTimeMillis());
			return entryCallTypeMap;
		});
	}

	private boolean isRateLimited(String guild, CallType call) {
		return this.rateLimiting.containsKey(guild) && this.rateLimiting.get(guild)
				.getOrDefault(call, 0L) >= System.currentTimeMillis();
	}

	public void queue(DiscordCallable callable) {
		if (!this.isAlive()) {
			return;
		}

		this.guildQueues.compute(callable.getGuildID(), (guild, guildMap) -> {
			if (guildMap == null) {
				guildMap = new ConcurrentHashMap<>();
			}
			guildMap.compute(callable.getCallType(), (callType, callTypeQueue) -> {
				if (callTypeQueue == null) {
					callTypeQueue = new PriorityBlockingQueue<>();
				}
				callTypeQueue.add(callable);
				return callTypeQueue;
			});
			return guildMap;
		});
	}

}
