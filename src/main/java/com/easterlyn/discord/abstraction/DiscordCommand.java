package com.easterlyn.discord.abstraction;

import com.easterlyn.discord.Discord;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.PermissionUtils;

import java.util.EnumSet;

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
		this.permissions = permissions;
	}

	protected Discord getDiscord() {
		return this.discord;
	}

	public String getName() {
		return this.command;
	}

	public void execute(IUser sender, IChannel channel, String original, String[] args) {
		if (permissions != null && !PermissionUtils.hasPermissions(channel, sender, permissions)) {
			discord.postMessage(null, sender.mention()
					+ ", you do not have access to this command.", channel.getLongID());
			String log = String.format("%s[%s] was denied access to command in #%s[%s]: %s",
					sender.getName(), sender.getLongID(), channel.getName(), channel.getLongID(), original);
			discord.log(log);
			discord.getLogger().info(log);
			return;
		}

		if (!onCommand(sender, channel, args)) {
			discord.postMessage(null, usage, channel.getLongID());
		}
		String log = String.format("Command in #%s[%s] from %s[%s]: %s",
				channel.getName(), channel.getLongID(), sender.getName(), sender.getLongID(), original);
		discord.log(log);
		discord.getLogger().info(log);
	}

	protected abstract boolean onCommand(IUser sender, IChannel channel, String[] args);

}
