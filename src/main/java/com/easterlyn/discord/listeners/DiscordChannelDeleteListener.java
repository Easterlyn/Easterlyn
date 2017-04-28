package com.easterlyn.discord.listeners;

import com.easterlyn.discord.Discord;
import com.easterlyn.discord.modules.VoiceTextModule;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.ChannelDeleteEvent;

/**
 * Listener for ChannelDeleteEvents.
 * 
 * @author Jikoo
 */
public class DiscordChannelDeleteListener implements IListener<ChannelDeleteEvent> {

	private final Discord discord;

	private VoiceTextModule voiceText;

	public DiscordChannelDeleteListener(Discord discord) {
		this.discord = discord;
	}

	@Override
	public void handle(ChannelDeleteEvent event) {
		getVoiceTextModule().handleChannelDeletion(event.getChannel());
	}

	private VoiceTextModule getVoiceTextModule() {
		if (voiceText == null) {
			voiceText = discord.getModule(VoiceTextModule.class);
		}

		return voiceText;
	}

}
