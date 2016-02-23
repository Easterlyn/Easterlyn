package co.sblock.discord.commands;

import java.util.HashSet;
import java.util.List;

import co.sblock.discord.Discord;
import co.sblock.discord.abstraction.DiscordCommand;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

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
	protected boolean onCommand(IUser sender, IChannel channel, String[] args) {
		IGuild guild;
		if (args.length > 0) {
			guild = getDiscord().getClient().getGuildByID(args[0]);
			if (guild == null) {
				for (IGuild server : getDiscord().getClient().getGuilds()) {
					if (server.getName().equalsIgnoreCase(args[0])) {
						guild = server;
						break;
					}
				}
			}
		} else {
			guild = channel instanceof IPrivateChannel ? null : channel.getGuild();
		}
		if (guild == null) {
			StringBuilder builder = new StringBuilder("Invalid server! Valid servers: ");
			for (IGuild server : getDiscord().getClient().getGuilds()) {
				builder.append(server.getID()).append(" (").append(server.getName()).append("), ");
			}
			builder.delete(builder.length() - 2, builder.length());
			getDiscord().postMessage(getName(), builder.toString(), channel.getID());
			return true;
		}
		StringBuilder builder = new StringBuilder("Server ").append(guild.getID())
				.append(" (").append(guild.getName()).append(")\nGroups:\n");
		List<IChannel> channels = guild.getChannels();
		if (channels.size() > 0) {
			for (IChannel chan : channels) {
				builder.append(chan.getID()).append(" (").append(chan.getName()).append("), ");
			}
			builder.delete(builder.length() - 2, builder.length());
		} else {
			builder.append("none");
		}
		HashSet<IRole> visibleRoles = new HashSet<>();
		for (IUser user : guild.getUsers()) {
			visibleRoles.addAll(user.getRolesForGuild(guild.getID()));
		}
		builder.append("\nRoles:\n");
		if (visibleRoles.size() > 0) {
			for (IRole role : visibleRoles) {
				builder.append(role.getID()).append(" (").append(role.getName()).append("), ");
			}
			builder.delete(builder.length() - 2, builder.length());
		} else {
			builder.append("none");
		}
		List<IUser> users = guild.getUsers();
		builder.append("\nClients:\n");
		if (users.size() > 0) {
			for (IUser user : users) {
				builder.append(user.getID()).append(" (").append(user.getName()).append("), ");
			}
			builder.delete(builder.length() - 2, builder.length());
		} else {
			builder.append("none");
		}
		getDiscord().postMessage(getName(), builder.toString(), channel.getID());
		return true;
	}

}
