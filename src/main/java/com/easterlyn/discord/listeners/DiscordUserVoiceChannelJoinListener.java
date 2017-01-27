package com.easterlyn.discord.listeners;

import com.easterlyn.discord.Discord;
import com.easterlyn.discord.modules.VoiceTextModule;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.UserVoiceChannelJoinEvent;

/**
 * Listener for UserVoiceChannelJoinEvents.
 * 
 * @author Jikoo
 */
public class DiscordUserVoiceChannelJoinListener implements IListener<UserVoiceChannelJoinEvent> {

	private final Discord discord;

	private VoiceTextModule voiceText;

	public DiscordUserVoiceChannelJoinListener(Discord discord) {
		this.discord = discord;
	}

	@Override
	public void handle(UserVoiceChannelJoinEvent event) {
		getVoiceTextModule().handleUserJoin(event.getChannel(), event.getUser());
	}

	private VoiceTextModule getVoiceTextModule() {
		if (voiceText == null) {
			voiceText = discord.getModule(VoiceTextModule.class);
		}

		return voiceText;
	}

}
