package co.sblock.discord.listeners;

import co.sblock.discord.Discord;
import co.sblock.discord.modules.RetentionModule;

import sx.blah.discord.handle.IListener;
import sx.blah.discord.handle.impl.events.MessageAcknowledgedEvent;

/**
 * IListener for MessageAcknowledgeEvents.
 * 
 * @author Jikoo
 */
public class DiscordMessageAcknowledgedListener implements IListener<MessageAcknowledgedEvent> {

	private final Discord discord;

	private RetentionModule retention;

	public DiscordMessageAcknowledgedListener(Discord discord) {
		this.discord = discord;
	}

	@Override
	public void handle(MessageAcknowledgedEvent event) {
		getRetentionModule().handleNewMessage(event.getAcknowledgedMessage());
	}

	private RetentionModule getRetentionModule() {
		if (retention == null) {
			retention = discord.getModule(RetentionModule.class);
		}
		return retention;
	}

}
