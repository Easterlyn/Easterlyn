package co.sblock.discord.commands;

import java.util.EnumSet;

import co.sblock.discord.Discord;

import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

/**
 * A base for all Discord-specific commands.
 * 
 * @author Jikoo
 */
public abstract class DiscordCommand {

	private final Discord discord;
	private final String command;
	private final String usage;
	private final EnumSet<Permissions> permissions;

	public DiscordCommand(Discord discord, String command, String usage, EnumSet<Permissions> permissions) {
		this.discord = discord;
		this.command = command;
		this.usage = usage;
		this.permissions = permissions;;
	}

	protected Discord getDiscord() {
		return this.discord;
	}

	public String getName() {
		return command;
	}

	public void execute(IUser sender, IChannel channel, String[] args) {
		if (!hasRequiredPermissions(sender, channel)) {
			discord.postMessage(Discord.BOT_NAME, "<@" + sender.getID()
					+ ">, you do not have access to this command.", channel.getID());
			return;
		}

		if (!onCommand(sender, channel, args)) {
			discord.postMessage(Discord.BOT_NAME, usage, channel.getID());
		}
	}

	private boolean hasRequiredPermissions(IUser sender, IChannel channel) {
		if (permissions == null) {
			return true;
		}
		try {
			DiscordUtils.checkPermissions(discord.getAPI(), channel, permissions);
		} catch (MissingPermissionsException e) {
			return false;
		}
		return true;
	}

	protected abstract boolean onCommand(IUser sender, IChannel channel, String[] args);

}
