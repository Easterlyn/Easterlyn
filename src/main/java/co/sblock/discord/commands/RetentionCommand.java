package co.sblock.discord.commands;

import java.util.EnumSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import co.sblock.discord.Discord;
import co.sblock.utilities.NumberUtils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;
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
		if (channel instanceof IPrivateChannel) {
			getDiscord().postMessage(Discord.BOT_NAME,
					"You cannot set a retention policy on private messages.", channel.getID());
			return true;
		}
		if (args.length < 1) {
			return false;
		}
		Pair<String, Long> pair;
		try {
			pair = NumberUtils.parseAndRemoveFirstTime(StringUtils.join(args, ' '));
		} catch (NumberFormatException e) {
			return false;
		}
		getDiscord().getDataStore().set("retention." + channel.getGuild().getID() + '.' + channel.getID(),
				pair.getRight() / 1000);
		getDiscord().postMessage(Discord.BOT_NAME,
				"Channel retention set to " + (pair.getRight() / 1000) + " seconds.",
				channel.getID());
		return true;
	}

}
