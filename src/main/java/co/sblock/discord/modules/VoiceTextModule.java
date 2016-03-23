package co.sblock.discord.modules;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import co.sblock.discord.Discord;
import co.sblock.discord.abstraction.DiscordCallable;
import co.sblock.discord.abstraction.DiscordModule;

import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.HTTP429Exception;

/**
 * 
 * 
 * @author Jikoo
 */
public class VoiceTextModule extends DiscordModule {

	private final BiMap<IChannel, IChannel> channels;

	public VoiceTextModule(Discord discord) {
		super(discord);
		channels = HashBiMap.create();
	}

	@Override
	public void doSetup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doHeartbeat() { }

	public void handleChannelDeletion(IChannel channel) {
		if (channels.containsKey(channel)) {
			// Voice channel has been deleted, delete corresponding text channel
			IChannel textForVoice = channels.remove(channel);
			this.getDiscord().queue(new DiscordCallable() {
				@Override
				public void call() throws DiscordException, HTTP429Exception, MissingPermissionsException {
					textForVoice.delete();
				}
			});
			return;
		}
		if (channels.containsValue(channel)) {
			// Text channel has been deleted, re-create it for corresponding voice channel
			handleChannelCreation(channels.inverse().remove(channel));
		}
	}

	public void handleChannelCreation(IChannel channel) {
		if (!(channel instanceof IVoiceChannel)) {
			return;
		}
	}

}
