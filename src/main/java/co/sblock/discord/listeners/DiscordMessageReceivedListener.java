package co.sblock.discord.listeners;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import co.sblock.discord.Discord;
import co.sblock.discord.DiscordPlayer;
import co.sblock.discord.abstraction.CallPriority;
import co.sblock.discord.modules.MinecraftModule;
import co.sblock.discord.modules.RetentionModule;

import sx.blah.discord.handle.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * IListener for MessageRecievedEvents.
 * 
 * @author Jikoo
 */
public class DiscordMessageReceivedListener implements IListener<MessageReceivedEvent> {

	private final Discord discord;
	private final Cache<String, Boolean> warnings;

	private MinecraftModule minecraft;
	private RetentionModule retention;

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
		if (author.getID().equals(discord.getClient().getOurUser().getID())) {
			// More jDiscord handling - no clue if MessageReceived is fired when our messages are acknowledged
			return;
		}

		this.getRetentionModule().handleNewMessage(event.getMessage());

		String msg = event.getMessage().getContent();
		if (msg.startsWith("/link ")) {
			String register = msg.substring(6);
			Object uuid = discord.getAuthCodes().getIfPresent(register);
			if (uuid == null || !(uuid instanceof UUID)) {
				discord.postMessage(discord.getBotName(), "Invalid registration code!", event.getMessage().getChannel().getID());
				return;
			}
			discord.postMessage(discord.getBotName(), "Registration complete!", event.getMessage().getChannel().getID());
			discord.getAuthCodes().invalidate(uuid);
			discord.getAuthCodes().invalidate(register);
			discord.addLink((UUID) uuid, author);
			return;
		}
		IChannel channel = event.getMessage().getChannel();
		boolean main = discord.getMainChannel().equals(channel.getID());
		boolean command = msg.length() > 0 && msg.charAt(0) == '/';
		if (!main && !command) {
			return;
		}
		if (command) {
			discord.queueMessageDeletion(event.getMessage(), CallPriority.MEDIUM);
			if (discord.handleDiscordCommand(msg, author, channel)) {
				return;
			}
		}
		DiscordPlayer sender = discord.getDiscordPlayerFor(author);
		if (sender == null) {
			if (main && !command) {
				discord.queueMessageDeletion(event.getMessage(), CallPriority.MEDIUM);
			}
			String id = author.getID();
			if (warnings.getIfPresent(id) != null) {
				return;
			}
			warnings.put(id, true);
			discord.postMessage(discord.getBotName(), "<@" + id
					+ ">, you must run /link in Minecraft to use this feature!", channel.getID());
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

	private RetentionModule getRetentionModule() {
		if (retention == null) {
			retention = discord.getModule(RetentionModule.class);
		}
		return retention;
	}

}
