package co.sblock.discord.commands;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import co.sblock.discord.Discord;
import co.sblock.discord.abstraction.DiscordCommand;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * A command for checking a user's ingame name, if registered.
 * 
 * @author Jikoo
 */
public class IngameNameCommand extends DiscordCommand {

	public IngameNameCommand(Discord discord) {
		super(discord, "ign", "/ign [name]", null);
	}

	@Override
	protected boolean onCommand(IUser sender, IChannel group, String[] args) {
		IUser target;
		if (args.length > 0) {
			String name = StringUtils.join(args, ' ').replaceAll("<@(\\d+)>", "$1");
			target = getDiscord().getClient().getUserByID(name);
			if (target == null && !(group instanceof IPrivateChannel)) {
				for (IUser user : group.getGuild().getUsers()) {
					if (user.getName().equalsIgnoreCase(name)) {
						target = user;
						break;
					}
				}
			}
			if (target == null) {
				getDiscord().postMessage(getName(), "Unknown user!", group.getID());
				return true;
			}
		} else {
			target = sender;
		}
		UUID uuid = getDiscord().getUUIDOf(target);
		OfflinePlayer player = uuid == null ? null : Bukkit.getOfflinePlayer(uuid);
		if (player == null) {
			getDiscord().postMessage(getName(), target.getName() + " has not linked their Minecraft account!", group.getID());
		} else {
			getDiscord().postMessage(getName(), target.getName() + "'s IGN is " + player.getName() + ".", group.getID());
		}
		return true;
	}

}
