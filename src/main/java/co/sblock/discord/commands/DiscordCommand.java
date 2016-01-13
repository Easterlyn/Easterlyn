package co.sblock.discord.commands;

import java.util.Collection;
import java.util.List;

import co.sblock.discord.Discord;

import me.itsghost.jdiscord.Role;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;

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

	public void execute(GroupUser sender, Group group, String[] args) {
		if (!hasRequiredRole(sender.getRoles())) {
			discord.postMessage(getName(), "<@" + sender.getUser().getId()
					+ ">, you do not have access to this command.", group.getId());
			return;
		}

		if (!onCommand(sender, group, args)) {
			discord.postMessage(getName(), usage, group.getId());
		}
	}

	private boolean hasRequiredRole(Collection<Role> userRoles) {
		if (roles == null || roles.isEmpty()) {
			// No roles required
			return true;
		}
		if (userRoles.isEmpty()) {
			return false;
		}
		for (Role role : userRoles) {
			if (roles.contains(role.getId())) {
				return true;
			}
		}
		return false;
	}

	protected abstract boolean onCommand(GroupUser sender, Group group, String[] args);

}
