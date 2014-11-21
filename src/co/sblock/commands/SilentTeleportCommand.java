package co.sblock.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * SblockCommand for silently teleporting a player.
 * 
 * @author Jikoo
 */
public class SilentTeleportCommand extends SblockCommand {

	public SilentTeleportCommand() {
		super("silenttp");
		this.setDescription("Teleports a player with no confirmation to either party involved. Intended for commandsigns.");
		this.setUsage("/silenttp <player> <x> <y> <z> [pitch] [yaw]");
		this.setPermission("group.horrorterror");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
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
			pTarget.teleport(tpdest);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	// TODO tab complete
}
