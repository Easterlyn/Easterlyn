package co.sblock.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

/**
 * SblockCommand for silently teleporting a player.
 * 
 * @author Jikoo
 */
public class SilentTeleportCommand extends SblockCommand {

	public SilentTeleportCommand() {
		super("silenttp");
		this.setDescription("Teleports a player with no confirmation to either party involved. Intended for commandsigns.");
		this.setUsage("/silenttp <player> <x> <y> <z> [pitch] [yaw] [world]");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args == null || args.length < 4) {
			return false;
		}
		Player pTarget = Bukkit.getPlayer(args[0]);
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
			pTarget.teleport(tpdest);
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
			return ImmutableList.of("#");
		}
		return ImmutableList.of();
	}
}
