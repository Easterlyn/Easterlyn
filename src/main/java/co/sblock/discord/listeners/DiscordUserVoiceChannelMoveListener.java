package co.sblock.discord.listeners;

import co.sblock.discord.Discord;
import co.sblock.discord.modules.VoiceTextModule;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.UserVoiceChannelMoveEvent;

/**
 * Listener for UserVoiceChannelMoveEvents.
 * 
 * @author Jikoo
 */
public class DiscordUserVoiceChannelMoveListener implements IListener<UserVoiceChannelMoveEvent> {

	private final Discord discord;

	private VoiceTextModule voiceText;

	public DiscordUserVoiceChannelMoveListener(Discord discord) {
		this.discord = discord;
	}

	@Override
	public void handle(UserVoiceChannelMoveEvent event) {
		getVoiceTextModule().handleUserLeave(event.getOldChannel(), event.getUser());
		getVoiceTextModule().handleUserJoin(event.getNewChannel(), event.getUser());
	}

	private VoiceTextModule getVoiceTextModule() {
		if (voiceText == null) {
			voiceText = discord.getModule(VoiceTextModule.class);
		}

		return voiceText;
	}

}
