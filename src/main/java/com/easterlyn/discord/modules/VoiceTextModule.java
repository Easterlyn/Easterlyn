package com.easterlyn.discord.modules;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import com.easterlyn.discord.Discord;
import com.easterlyn.discord.abstraction.CallPriority;
import com.easterlyn.discord.abstraction.DiscordCallable;
import com.easterlyn.discord.abstraction.DiscordModule;
import com.easterlyn.utilities.TextUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * DiscordModule for managing a text channel per voice channel.
 * 
 * @author Jikoo
 */
public class VoiceTextModule extends DiscordModule {

	private final BiMap<IVoiceChannel, IChannel> channels;

	public VoiceTextModule(Discord discord) {
		super(discord);
		channels = Maps.synchronizedBiMap(HashBiMap.create());
	}

	@Override
	public void doSetup() {
		this.getDiscord().getClient().getVoiceChannels().forEach(channel -> {
			handleChannelCreation(channel);
		});

	}

	@Override
	public void doHeartbeat() { }

	public void handleChannelDeletion(IChannel channel) {
		if (channels.containsKey(channel)) {
			// Voice channel has been deleted, delete corresponding text channel
			IChannel textForVoice = channels.remove(channel);
			this.getDiscord().queue(new DiscordCallable() {
				@Override
				public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
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

	public void handleChannelCreation(IVoiceChannel voice) {
		if (this.channels.containsValue(voice) || voice.equals(voice.getGuild().getAFKChannel())) {
			return;
		}

		String textChannelName = getTextChannelName(voice.getName());
		List<IChannel> channels = voice.getGuild().getChannelsByName(textChannelName);
		IChannel text = null;

		for (IChannel channelMatch : channels) {
			if (!(channelMatch instanceof IVoiceChannel)) {
				text = channelMatch;
				break;
			}
		}

		if (text == null) {
			createTextChannel(voice);
		} else {
			this.channels.put(voice, text);
			getDiscord().getModule(RetentionModule.class).setRetention(text, 600000L);
			addAllMembers(voice, text);
		}
	}

	private void createTextChannel(IVoiceChannel voice) {
		this.getDiscord().queue(new DiscordCallable() {
			@Override
			public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
				IChannel text = voice.getGuild().createChannel(getTextChannelName(voice.getName()));
				channels.put(voice, text);
				getDiscord().queue(new DiscordCallable() {
					@Override
					public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
						text.overrideRolePermissions(text.getGuild().getEveryoneRole(), EnumSet.noneOf(Permissions.class), EnumSet.of(Permissions.READ_MESSAGES));
					}
				});
				getDiscord().queue(new DiscordCallable() {
					@Override
					public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
						text.changeTopic("Text channel for voice channel " + voice.getName());
					}
				});
				getDiscord().getModule(RetentionModule.class).setRetention(text, 600000L);
				addAllMembers(voice, text);
			}
		});
	}

	private String getTextChannelName(String voiceChannelName) {
		return "vc-" + TextUtils.stripNonAlphanumerics(voiceChannelName.replace(' ', '_')).toLowerCase();
	}

	private void addAllMembers(IVoiceChannel voice, IChannel text) {
		List<IUser> connected = voice.getConnectedUsers();
		List<IUser> permitted = text.getUsersHere();
		List<IUser> toPermit = connected.stream().filter(user -> !permitted.contains(user))
				.collect(Collectors.toCollection(ArrayList::new));
		List<IUser> toRemove = permitted.stream().filter(user -> !connected.contains(user))
				.collect(Collectors.toCollection(ArrayList::new));

		toPermit.forEach(user -> addPermissions(text, user));
		toRemove.forEach(user -> removePermissions(text, user));
	}

	public void handleUserJoin(IVoiceChannel voice, IUser user) {
		if (channels.containsKey(voice)) {
			addPermissions(channels.get(voice), user);
		}
	}

	private void addPermissions(IChannel text, IUser user) {
		if (!isUserEditable(user, text.getGuild())) {
			return;
		}
		getDiscord().queue(new DiscordCallable(CallPriority.LOW) {
			@Override
			public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
				text.overrideUserPermissions(user, EnumSet.of(Permissions.READ_MESSAGES), EnumSet.noneOf(Permissions.class));
			}
		});
	}

	public void handleUserLeave(IVoiceChannel voice, IUser user) {
		if (channels.containsKey(voice)) {
			removePermissions(channels.get(voice), user);
		}
	}

	private void removePermissions(IChannel text, IUser user) {
		if (!isUserEditable(user, text.getGuild())) {
			return;
		}
		getDiscord().queue(new DiscordCallable(CallPriority.LOW) {
			@Override
			public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
				text.removePermissionsOverride(user);
			}
		});
	}

	private boolean isUserEditable(IUser user, IGuild guild) {
		if (guild.getOwner().equals(user)) {
			return false;
		}
		IUser us = this.getDiscord().getClient().getOurUser();
		return !user.equals(us) && !DiscordUtils.isUserHigher(guild, us, user.getRolesForGuild(guild));
	}

}
