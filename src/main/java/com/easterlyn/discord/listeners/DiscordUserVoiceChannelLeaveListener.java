package com.easterlyn.discord.listeners;

import com.easterlyn.discord.Discord;
import com.easterlyn.discord.modules.VoiceTextModule;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;

/**
 * Listener for UserVoiceChannelLeaveEvents.
 * 
 * @author Jikoo
 */
public class DiscordUserVoiceChannelLeaveListener implements IListener<UserVoiceChannelLeaveEvent> {

	private final Discord discord;

	private VoiceTextModule voiceText;

	public DiscordUserVoiceChannelLeaveListener(Discord discord) {
		this.discord = discord;
	}

	@Override
	public void handle(UserVoiceChannelLeaveEvent event) {
		getVoiceTextModule().handleUserLeave(event.getVoiceChannel(), event.getUser());
	}

	private VoiceTextModule getVoiceTextModule() {
		if (voiceText == null) {
			voiceText = discord.getModule(VoiceTextModule.class);
		}

		return voiceText;
	}

}
