package co.sblock.discord.listeners;

import co.sblock.discord.Discord;
import co.sblock.discord.modules.RetentionModule;

import sx.blah.discord.handle.IListener;
import sx.blah.discord.handle.impl.events.MessageAcknowledgedEvent;
import sx.blah.discord.handle.obj.IMessage;

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
		IMessage message = event.getAcknowledgedMessage();

		getRetentionModule().handleNewMessage(message);

//		String botName = discord.getClient().getOurUser().getName();
//		if (!botName.equals(discord.getBotName())
//				&& message.getChannel().getID().equals(discord.getMainChannel())) {
//			discord.queue(new DiscordCallable(CallPriority.MEDIUM, true) {
//				@Override
//				public void call() throws DiscordException, HTTP429Exception,
//						MissingPermissionsException {
//					message.edit("**" + botName + "**" + (botName.charAt(0) == '*' ? " " : ": ")
//							+ message.getContent());
//				}
//			});
//		}
	}

	private RetentionModule getRetentionModule() {
		if (retention == null) {
			retention = discord.getModule(RetentionModule.class);
		}
		return retention;
	}

}
