package com.easterlyn.commands.teleportation;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.player.PlayerUtils;

import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 * EasterlynCommand for silently teleporting a player.
 *
 * @author Jikoo
 */
public class SilentTeleportCommand extends EasterlynCommand {

	public SilentTeleportCommand(Easterlyn plugin) {
		super(plugin, "silenttp");
		this.setDescription("Teleports a player with no confirmation to either party involved. Intended for commandsigns.");
		this.setUsage("/silenttp <player> <x> <y> <z> [pitch] [yaw] [world]");
		this.setPermissionLevel(UserRank.ADMIN);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args == null || args.length < 4) {
			return false;
		}
		Player pTarget = PlayerUtils.matchOnlinePlayer(sender, args[0]);
		if (pTarget == null) {
			// silently eat player get failure in case CommandSign messes up, have seen it happen.
			return true;
		}
		try {
			Location tpdest = new Location(pTarget.getWorld(), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
			if (args.length >= 6) {
				tpdest.setPitch(Float.valueOf(args[4]));
				tpdest.setYaw(Float.valueOf(args[5]));
			}
			if (args.length >= 7) {
				World wTarget = Bukkit.getWorld(args[6]);
				if (wTarget == null) {
					return true;
				}
				tpdest.setWorld(wTarget);
			}
			pTarget.teleport(tpdest, TeleportCause.COMMAND);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (args.length < 2) {
			return super.tabComplete(sender, alias, args);
		}
		if (args.length < 7) {
			return ImmutableList.of("0");
		}
		return ImmutableList.of();
	}
}
