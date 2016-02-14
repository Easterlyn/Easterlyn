package co.sblock.discord.commands;

import java.util.Collection;
import java.util.List;

import co.sblock.discord.Discord;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

/**
 * A base for all Discord-specific commands.
 * 
 * @author Jikoo
 */
public abstract class DiscordCommand {

	private final Discord discord;
	private final String command;
	private final String usage;
	private final List<String> roles;

	public DiscordCommand(Discord discord, String command, String usage, List<String> roles) {
		this.discord = discord;
		this.command = command;
		this.usage = usage;
		this.roles = roles;
	}

	protected Discord getDiscord() {
		return this.discord;
	}

	public String getName() {
		return command;
	}

	public void execute(IUser sender, IChannel channel, String[] args) {
		if (!hasRequiredRole(sender, channel)) {
			discord.postMessage(getName(), "<@" + sender.getID()
					+ ">, you do not have access to this command.", channel.getID());
			return;
		}

		if (!onCommand(sender, channel, args)) {
			discord.postMessage(getName(), usage, channel.getID());
		}
	}

	private boolean hasRequiredRole(IUser sender, IChannel channel) {
		if (roles == null || roles.isEmpty()) {
			// No roles required
			return true;
		}
		if (channel instanceof IPrivateChannel) {
			return false;
		}
		Collection<IRole> userRoles = sender.getRolesForGuild(channel.getGuild().getID());
		if (userRoles.isEmpty()) {
			return false;
		}
		for (IRole role : userRoles) {
			if (roles.contains(role.getID())) {
				return true;
			}
		}
		return false;
	}

	protected abstract boolean onCommand(IUser sender, IChannel group, String[] args);

}
