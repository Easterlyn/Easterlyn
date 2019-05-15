package com.easterlyn.discord.commands;

import com.easterlyn.discord.Discord;
import com.easterlyn.discord.abstraction.DiscordCommand;
import com.easterlyn.discord.queue.CallType;
import com.easterlyn.discord.queue.DiscordCallable;
import com.easterlyn.utilities.TextUtils;
import com.easterlyn.utilities.tuple.Wrapper;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.EnumSet;

/**
 * Create a simple poll quickly.
 *
 * @author Jikoo
 */
public class PollCommand extends DiscordCommand {

	public PollCommand(Discord discord) {
		super(discord, "poll", "/poll [poll text]", EnumSet.of(Permissions.SEND_MESSAGES, Permissions.ADD_REACTIONS));
	}

	@Override
	protected boolean onCommand(IUser sender, IChannel channel, String[] args) {
		Wrapper<IMessage> message = new Wrapper<>();
		this.getDiscord().queue(new DiscordCallable(channel, CallType.MESSAGE_SEND) {
			@Override
			public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
				String senderName = channel.isPrivate() ? sender.getName() : sender.getDisplayName(channel.getGuild());
				message.set(channel.sendMessage(String.format("**Poll by %s:** %s", senderName, TextUtils.join(args, ' '))));
			}
		}.withChainedCall(new DiscordCallable(channel, CallType.EMOJI_EDIT) {
			@Override
			public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
				// Refresh message just in case
				IMessage posted = channel.getMessageByID(message.get().getLongID());
				if (posted != null && !posted.isDeleted()) {
					posted.addReaction(ReactionEmoji.of(":thumbsup:"));
				}
			}
		}).withChainedCall(new DiscordCallable(channel, CallType.EMOJI_EDIT) {
			@Override
			public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
				// Refresh message just in case
				IMessage posted = channel.getMessageByID(message.get().getLongID());
				if (posted != null && !posted.isDeleted()) {
					posted.addReaction(ReactionEmoji.of(":thumbsdown:"));
				}
			}
		}).withChainedCall(new DiscordCallable(channel, CallType.EMOJI_EDIT) {
			@Override
			public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
				// Refresh message just in case
				IMessage posted = channel.getMessageByID(message.get().getLongID());
				if (posted != null && !posted.isDeleted()) {
					posted.addReaction(ReactionEmoji.of(":shrug:"));
				}
			}
		}));
		return true;
	}

}
