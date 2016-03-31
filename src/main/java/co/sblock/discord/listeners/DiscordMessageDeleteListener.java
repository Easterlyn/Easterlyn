package co.sblock.discord.listeners;

import co.sblock.discord.Discord;
import co.sblock.discord.modules.RetentionModule;

import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.MessageDeleteEvent;

/**
 * IListener for MessageDeleteEvents.
 * 
 * @author Jikoo
 */
public class DiscordMessageDeleteListener implements IListener<MessageDeleteEvent> {

	private final Discord discord;

	private RetentionModule retention;

	public DiscordMessageDeleteListener(Discord discord) {
		this.discord = discord;
	}

	@Override
	public void handle(MessageDeleteEvent event) {
		// Note that this will be called for every message deleted by the retention module as well.
		// Inefficient, but there's not much that can be done for it without going farther off the Discord4J API.
		getRetentionModule().handleMessageDelete(event.getMessage());
	}

	private RetentionModule getRetentionModule() {
		if (retention == null) {
			retention = discord.getModule(RetentionModule.class);
		}
		return retention;
	}
}
