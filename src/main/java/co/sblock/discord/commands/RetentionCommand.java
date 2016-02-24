package co.sblock.discord.commands;

import java.util.EnumSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import co.sblock.discord.Discord;
import co.sblock.discord.abstraction.DiscordCommand;
import co.sblock.discord.modules.RetentionModule;
import co.sblock.utilities.NumberUtils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;

/**
 * DiscordCommand for setting retention duration on a channel.
 * 
 * @author Jikoo
 */
public class RetentionCommand extends DiscordCommand {

	public RetentionCommand(Discord discord) {
		super(discord, "retention", "/retention <duration>", EnumSet.of(Permissions.MANAGE_CHANNEL));
	}

	@Override
	protected boolean onCommand(IUser sender, IChannel channel, String[] args) {
		RetentionModule module;
		try {
			module = getDiscord().getModule(RetentionModule.class);
		} catch (IllegalArgumentException e) {
			getDiscord().postMessage(Discord.BOT_NAME, "Retention is not enabled.", channel.getID());
			return true;
		}
		if (channel instanceof IPrivateChannel || channel instanceof IVoiceChannel) {
			getDiscord().postMessage(Discord.BOT_NAME,
					"You cannot set a retention policy on private messages.", channel.getID());
			return true;
		}
		if (args.length < 1) {
			return false;
		}
		if (args[0].equalsIgnoreCase("null") || args[0].equalsIgnoreCase("off")) {
			module.setRetention(channel.getGuild(), channel, null);
			getDiscord().postMessage(Discord.BOT_NAME, "Channel retention unset.", channel.getID());
			return true;
		}
		Pair<String, Long> pair;
		try {
			pair = NumberUtils.parseAndRemoveFirstTime(StringUtils.join(args, ' '));
		} catch (NumberFormatException e) {
			return false;
		}
		module.setRetention(channel.getGuild(), channel, pair.getRight() / 1000);
		getDiscord().postMessage(Discord.BOT_NAME,
				"Channel retention set to " + (pair.getRight() / 1000) + " seconds.",
				channel.getID());
		return true;
	}

}
