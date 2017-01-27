package com.easterlyn.discord.listeners;

import com.easterlyn.discord.Discord;
import com.easterlyn.discord.modules.VoiceTextModule;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.VoiceChannelCreateEvent;

/**
 * Listener for VoiceChannelCreateEvents.
 * 
 * @author Jikoo
 */
public class DiscordVoiceChannelCreateListener implements IListener<VoiceChannelCreateEvent> {

	private final Discord discord;

	private VoiceTextModule voiceText;

	public DiscordVoiceChannelCreateListener(Discord discord) {
		this.discord = discord;
	}

	@Override
	public void handle(VoiceChannelCreateEvent event) {
		getVoiceTextModule().handleChannelCreation(event.getChannel());
	}

	private VoiceTextModule getVoiceTextModule() {
		if (voiceText == null) {
			voiceText = discord.getModule(VoiceTextModule.class);
		}

		return voiceText;
	}

}
