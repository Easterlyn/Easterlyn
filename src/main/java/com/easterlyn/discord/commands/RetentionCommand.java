package com.easterlyn.discord.commands;

import com.easterlyn.discord.Discord;
import com.easterlyn.discord.abstraction.DiscordCommand;
import com.easterlyn.discord.modules.RetentionModule;
import com.easterlyn.utilities.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.MissingPermissionsException;

import java.util.EnumSet;

/**
 * DiscordCommand for setting retention duration on a channel.
 *
 * @author Jikoo
 */
public class RetentionCommand extends DiscordCommand {

	public RetentionCommand(Discord discord) {
		super(discord, "retention", "/retention [guild] <duration>", EnumSet.of(Permissions.MANAGE_CHANNEL));
	}

	@Override
	protected boolean onCommand(IUser sender, IChannel channel, String[] args) {
		RetentionModule module;
		try {
			module = getDiscord().getModule(RetentionModule.class);
		} catch (IllegalArgumentException e) {
			getDiscord().postMessage(null, "Retention is not enabled.", channel.getLongID());
			return true;
		}
		if (channel instanceof IPrivateChannel || channel instanceof IVoiceChannel) {
			getDiscord().postMessage(null,
					"You cannot set a retention policy on private messages.", channel.getLongID());
			return true;
		}
		if (args.length < 1) {
			return false;
		}
		boolean guild = false;
		try {
			DiscordUtils.checkPermissions(channel.getModifiedPermissions(sender), EnumSet.of(Permissions.MANAGE_SERVER));
			for (String arg : args) {
				if (arg.equalsIgnoreCase("server") || arg.equalsIgnoreCase("guild")) {
					guild = true;
					break;
				}
			}
		} catch (MissingPermissionsException e) {
			guild = false;
		}
		if (args[0].equalsIgnoreCase("null") || args[0].equalsIgnoreCase("off")) {
			if (guild) {
				module.setRetention(channel.getGuild(), null);
			} else {
				module.setRetention(channel, null);
			}
			getDiscord().postMessage(null, "Channel retention unset.", channel.getLongID());
			return true;
		}
		Pair<String, Long> pair;
		try {
			pair = NumberUtils.parseAndRemoveFirstTime(StringUtils.join(args, ' '));
		} catch (NumberFormatException e) {
			return false;
		}
		long seconds = pair.getRight() / 1000;
		if (guild) {
			module.setRetention(channel.getGuild(), seconds);
		} else {
			module.setRetention(channel, seconds);
		}
		this.getDiscord().postMessage(null,
				(guild ? "Guild" : "Channel") + " retention set to " + seconds + " seconds.",
				channel.getLongID());
		return true;
	}

}
