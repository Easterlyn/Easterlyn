package co.sblock.discord.commands;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import co.sblock.discord.Discord;

import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;
import me.itsghost.jdiscord.talkable.User;

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
	protected boolean onCommand(GroupUser sender, Group group, String[] args) {
		User target;
		if (args.length > 0) {
			String name = getDiscord().sanitize(StringUtils.join(args, ' '));
			target = getDiscord().getAPI().getUserByUsername(name);
			if (target == null) {
				target = getDiscord().getAPI().getUserById(name);
			}
			if (target == null) {
				getDiscord().postMessage(getName(), "Unknown user!", group.getId());
				return true;
			}
		} else {
			target = sender.getUser();
		}
		UUID uuid = getDiscord().getUUIDOf(target);
		OfflinePlayer player = uuid == null ? null : Bukkit.getOfflinePlayer(uuid);
		if (player == null) {
			getDiscord().postMessage(getName(), target.getUsername() + " has not linked their Minecraft account!", group.getId());
		} else {
			getDiscord().postMessage(getName(), target.getUsername() + "'s IGN is " + player.getName() + ".", group.getId());
		}
		// TODO Auto-generated method stub
		return false;
	}

}
