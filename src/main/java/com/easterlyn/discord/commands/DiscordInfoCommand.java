package com.easterlyn.discord.commands;

import java.util.List;

import com.easterlyn.discord.Discord;
import com.easterlyn.discord.abstraction.DiscordCommand;

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
		IGuild guild = null;
		if (args.length > 0) {
			try {
				guild = getDiscord().getClient().getGuildByID(Long.parseLong(args[0]));
			} catch (NumberFormatException e) {
				// Not a guild ID
			}
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
				builder.append(server.getLongID()).append(" (").append(server.getName()).append("), ");
			}
			builder.delete(builder.length() - 2, builder.length());
			getDiscord().postMessage(getName(), builder.toString(), channel.getLongID());
			return true;
		}
		StringBuilder builder = new StringBuilder("Available roles in guild ").append(guild.getLongID())
				.append(" (").append(guild.getName()).append("):\n");
		List<IRole> roles = guild.getRoles();
		if (roles.size() > 0) {
			for (IRole role : roles) {
				builder.append(role.getLongID()).append(" (").append(role.getName()).append("), ");
			}
			builder.delete(builder.length() - 2, builder.length());
		} else {
			builder.append("none");
		}
		builder.append("\nFor client and channel IDs, please turn on developer mode and right click.");
		getDiscord().postMessage(getName(), builder.toString(), channel.getLongID());
		return true;
	}

}
