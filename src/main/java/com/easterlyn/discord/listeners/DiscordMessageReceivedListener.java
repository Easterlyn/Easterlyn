package com.easterlyn.discord.listeners;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.easterlyn.discord.Discord;
import com.easterlyn.discord.DiscordPlayer;
import com.easterlyn.discord.modules.MinecraftModule;
import com.easterlyn.discord.queue.CallPriority;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * IListener for MessageRecievedEvents.
 * 
 * @author Jikoo
 */
public class DiscordMessageReceivedListener implements IListener<MessageReceivedEvent> {

	private final Discord discord;
	private final Cache<Long, Boolean> warnings;

	private MinecraftModule minecraft;

	public DiscordMessageReceivedListener(Discord discord) {
		this.discord = discord;
		this.warnings = CacheBuilder.newBuilder().weakKeys().weakValues()
				.expireAfterWrite(2, TimeUnit.MINUTES).build();
	}

	@Override
	public void handle(MessageReceivedEvent event) {
		IUser author = event.getMessage().getAuthor();
		if (author == null) {
			// In jDiscord, this is additional context for links, etc.
			// Don't know if it's a problem with Discord4J yet
			return;
		}

		if (discord.getClient().getOurUser().equals(author)) {
			// More jDiscord handling - no clue if MessageReceived is fired when our messages are acknowledged
			return;
		}

		if (author.isBot()) {
			return;
		}

		String msg = event.getMessage().getContent();
		if (msg.startsWith("/link ")) {
			String register = msg.substring(6);
			Object uuid = discord.getAuthCodes().getIfPresent(register);
			if (uuid == null || !(uuid instanceof UUID)) {
				discord.postMessage(discord.getBotName(), "Invalid registration code!", event.getMessage().getChannel().getLongID());
				return;
			}
			discord.postMessage(discord.getBotName(), "Registration complete!", event.getMessage().getChannel().getLongID());
			discord.getAuthCodes().invalidate(uuid);
			discord.getAuthCodes().invalidate(register);
			discord.addLink((UUID) uuid, author);
			return;
		}
		IChannel channel = event.getMessage().getChannel();
		boolean main = !channel.isPrivate() && discord.getMainChannelID(channel.getGuild()) == channel.getLongID();
		boolean command = msg.length() > 0 && msg.charAt(0) == '/';
		if (!main && !command) {
			return;
		}
		if (command) {
			if (!channel.isPrivate()) {
				discord.queueMessageDeletion(CallPriority.MEDIUM, event.getMessage());
			}
			if (discord.handleDiscordCommand(msg, author, channel)) {
				return;
			}
		}
		DiscordPlayer sender = discord.getDiscordPlayerFor(author);
		if (sender == null) {
			if (main && !command) {
				discord.queueMessageDeletion(CallPriority.MEDIUM, event.getMessage());
			}
			long id = author.getLongID();
			if (warnings.getIfPresent(id) != null) {
				return;
			}
			warnings.put(id, true);
			discord.postMessage(discord.getBotName(), author.mention()
					+ ", you must run /link in Minecraft to use this feature!", channel.getLongID());
			return;
		}
		if (command) {
			getMCModule().handleCommand(sender, msg, channel);
			return;
		}
		if (main) {
			if (msg.equalsIgnoreCase("cough")) {
				// Fuck you, Rob. Just use /list or say hi like a normal person.
				return;
			}
			getMCModule().handleChat(event.getMessage(), sender);
			return;
		}
	}

	private MinecraftModule getMCModule() {
		if (minecraft == null) {
			minecraft = discord.getModule(MinecraftModule.class);
		}
		return minecraft;
	}

}
