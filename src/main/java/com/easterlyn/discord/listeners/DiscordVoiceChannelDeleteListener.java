package com.easterlyn.discord.listeners;

import com.easterlyn.discord.Discord;
import com.easterlyn.discord.modules.VoiceTextModule;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelDeleteEvent;

/**
 * Listener for VoiceChannelDeleteEvents.
 * 
 * @author Jikoo
 */
public class DiscordVoiceChannelDeleteListener implements IListener<VoiceChannelDeleteEvent> {

	private final Discord discord;

	private VoiceTextModule voiceText;

	public DiscordVoiceChannelDeleteListener(Discord discord) {
		this.discord = discord;
	}

	@Override
	public void handle(VoiceChannelDeleteEvent event) {
		getVoiceTextModule().handleChannelDeletion(event.getVoiceChannel());
	}

	private VoiceTextModule getVoiceTextModule() {
		if (voiceText == null) {
			voiceText = discord.getModule(VoiceTextModule.class);
		}

		return voiceText;
	}

}
