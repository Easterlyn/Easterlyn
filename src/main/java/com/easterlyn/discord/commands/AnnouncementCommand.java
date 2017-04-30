package com.easterlyn.discord.commands;

import java.util.EnumSet;

import com.easterlyn.discord.Discord;
import com.easterlyn.discord.abstraction.DiscordCommand;
import com.easterlyn.discord.queue.CallType;
import com.easterlyn.discord.queue.DiscordCallable;
import com.easterlyn.utilities.Wrapper;

import org.apache.commons.lang3.StringUtils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Simple DiscordCommand for pinning an announcement.
 * 
 * @author Jikoo
 */
public class AnnouncementCommand extends DiscordCommand {

	public AnnouncementCommand(Discord discord) {
		super(discord, "announce", "/announce [arguments] - post and pin an announcement", EnumSet.of(Permissions.MANAGE_MESSAGES));
	}

	@Override
	protected boolean onCommand(IUser sender, IChannel channel, String[] args) {
		if (args.length < 1) {
			return false;
		}
		Wrapper<IMessage> message = new Wrapper<>();
		this.getDiscord().queue(new DiscordCallable(channel, CallType.MESSAGE_SEND) {
			@Override
			public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
				message.set(channel.sendMessage(StringUtils.join(args, ' ')));
			}
		}.withChainedCall(new DiscordCallable(channel, CallType.MESSAGE_EDIT) {
			@Override
			public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
				// Refresh message just in case
				IMessage posted = channel.getMessageByID(message.get().getLongID());
				if (posted != null && !posted.isDeleted()) {
					channel.pin(posted);
				}
			}
		}));
		return true;
	}

}
