package co.sblock.discord;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.handle.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.HTTP429Exception;

/**
 * IListener for MessageRecievedEvents.
 * 
 * @author Jikoo
 */
public class DiscordMessageReceivedListener implements IListener<MessageReceivedEvent> {

	private final Discord discord;
	private final Cache<String, Boolean> warnings;

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
		if (author.getID().equals(discord.getAPI().getOurUser().getID())) {
			// More jDiscord handling - no clue if MessageRecieved is fired when our messages are acknowledged
			return;
		}
		String msg = event.getMessage().getContent();
		if (msg.startsWith("/link ")) {
			String register = msg.substring(6);
			Object uuid = discord.getAuthCodes().getIfPresent(register);
			if (uuid == null || !(uuid instanceof UUID)) {
				discord.postMessage("Sbot", "Invalid registration code!", event.getMessage().getChannel().getID());
				return;
			}
			discord.postMessage("Sbot", "Registration complete!", event.getMessage().getChannel().getID());
			discord.getAuthCodes().invalidate(uuid);
			discord.getAuthCodes().invalidate(register);
			discord.addLink((UUID) uuid, author);
			return;
		}
		IChannel channel = event.getMessage().getChannel();
		boolean main = !(channel instanceof IPrivateChannel)
				&& discord.getConfig().getString("discord.server").equals(channel.getGuild().getID())
				&& discord.getConfig().getString("discord.chat.main").equals(channel.getID());
		boolean command = msg.length() > 0 && msg.charAt(0) == '/';
		if (!main && !command) {
			return;
		}
		if (command) {
			if (discord.handleDiscordCommandFor(msg, author, channel)) {
				return;
			}
		}
		DiscordPlayer sender = discord.getPlayerFor(author);
		if (sender == null) {
			if (main) {
				try {
					event.getMessage().delete();
				} catch (MissingPermissionsException | HTTP429Exception | DiscordException e) {
					discord.getLogger().warning("Unable to delete messages in main chat!");
				}
			}
			String id = author.getID();
			if (warnings.getIfPresent(id) != null) {
				return;
			}
			warnings.put(id, true);
			discord.postMessage("Sbot", "<@" + id
					+ ">, you must run /link in Minecraft to use this feature!", channel.getID());
			return;
		}
		if (command) {
			discord.handleMinecraftCommandFor(sender, msg.substring(1), channel);
			return;
		}
		if (main) {
			discord.handleChatToMinecraft(event.getMessage(), sender);
			return;
		}
	}

}
