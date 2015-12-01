package co.sblock.discord.commands;

import java.util.HashSet;
import java.util.List;

import co.sblock.discord.Discord;

import me.itsghost.jdiscord.Role;
import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;

/**
 * A command for printing out information about Discord.
 * 
 * @author Jikoo
 */
public class DiscordInfoCommand extends DiscordCommand {

	public DiscordInfoCommand(Discord discord) {
		super(discord, "dc-info", "/dc-info [serverid]", null);
	}

	@Override
	protected boolean onCommand(GroupUser sender, Group group, String[] args) {
		Server server;
		if (args.length > 0) {
			server = getDiscord().getAPI().getServerById(args[0]);
			if (server == null) {
				for (Server srv : getDiscord().getAPI().getAvailableServers()) {
					if (srv.getName().equalsIgnoreCase(args[0])) {
						server = srv;
						break;
					}
				}
			}
		} else {
			server = group.getServer();
		}
		if (server == null) {
			StringBuilder builder = new StringBuilder("Invalid server! Valid servers: ");
			for (Server srv : getDiscord().getAPI().getAvailableServers()) {
				builder.append(srv.getId()).append(" (").append(srv.getName()).append("), ");
			}
			builder.delete(builder.length() - 2, builder.length());
			getDiscord().postMessage(getName(), builder.toString(), group.getId());
			return true;
		}
		StringBuilder builder = new StringBuilder("Server ").append(server.getId())
				.append(" (").append(server.getName()).append(")\nGroups:\n");
		List<Group> groups = server.getGroups();
		if (groups.size() > 0) {
			for (Group grp : groups) {
				builder.append(grp.getId()).append(" (").append(grp.getName()).append("), ");
			}
			builder.delete(builder.length() - 2, builder.length());
		} else {
			builder.append("none");
		}
		HashSet<Role> visibleRoles = new HashSet<>();
		for (GroupUser user : server.getConnectedClients()) {
			visibleRoles.addAll(user.getRoles());
		}
		builder.append("\nRoles:\n");
		if (visibleRoles.size() > 0) {
			for (Role role : visibleRoles) {
				builder.append(role.getId()).append(" (").append(role.getName()).append("), ");
			}
			builder.delete(builder.length() - 2, builder.length());
		} else {
			builder.append("none");
		}
		getDiscord().postMessage(getName(), builder.toString(), group.getId());
		return true;
	}

}
