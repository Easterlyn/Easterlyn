package co.sblock.commands.cheat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for setting travel speeds.
 * 
 * @author Jikoo
 */
public class SpeedCommand extends SblockCommand {

	public SpeedCommand(Sblock plugin) {
		super(plugin, "speed");
		this.setAliases("flyspeed", "walkspeed");
		this.setDescription("Set flight or walking speed.");
		this.setPermissionLevel("denizen");
		this.setUsage("/speed [player] [fly|walk] <-1 to 1>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 1 || !(sender instanceof Player) && args.length < 2) {
			return false;
		}

		float speed;
		try {
			speed = Float.parseFloat(args[args.length - 1]);
		} catch (NumberFormatException e) {
			return false;
		}

		if (speed > 1 || speed < -1) {
			// Speed must be between -1 and 1
			return false;
		}

		// Label used should override parameters
		Boolean fly = null;
		char labelChar = label.charAt(0);
		if (labelChar == 'f' || labelChar == 'F') {
			fly = true;
		} else if (labelChar == 'w' || labelChar == 'W') {
			fly = false;
		}

		if (args.length == 1) {
			setSpeed((Player) sender, fly, speed);
			sender.sendMessage(Color.GOOD + "Speed set!");
			return true;
		}

		// 2 parameters, first can be fly/walk or a player name.
		// As we don't mandate exact player name, check fly/walk first so players
		// whose names contain those words aren't changed instead.
		if (fly == null && args.length == 2 && sender instanceof Player) {
			if (args[0].equalsIgnoreCase("fly")) {
				setSpeed((Player) sender, true, speed);
				sender.sendMessage(Color.GOOD + "Speed set!");
				return true;
			} else if (args[0].equalsIgnoreCase("walk")) {
				setSpeed((Player) sender, false, speed);
				sender.sendMessage(Color.GOOD + "Speed set!");
				return true;
			}
		}

		List<Player> players = Bukkit.matchPlayer(args[0]);
		if (players.size() == 0) {
			return false;
		}
		Player target = players.get(0);
		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("fly")) {
				setSpeed(target, true, speed);
				sender.sendMessage(Color.GOOD + "Speed set!");
				return true;
			} else if (args[0].equalsIgnoreCase("walk")) {
				setSpeed(target, false, speed);
				sender.sendMessage(Color.GOOD + "Speed set!");
				return true;
			}
			return false;
		}
		setSpeed(target, fly, speed);
		sender.sendMessage(Color.GOOD + "Speed set!");
		return true;
	}

	private void setSpeed(Player player, Boolean fly, float speed) {
		if (fly == null) {
			// If not explicitly set, set walk/fly speed based on if the player is midair.
			Block block = player.getLocation().getBlock();
			fly = block.isEmpty() && block.getRelative(BlockFace.DOWN).isEmpty();
		}
		if (fly) {
			player.setFlySpeed(speed);
		} else {
			player.setWalkSpeed(speed);
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return super.tabComplete(sender, alias, args);
	}

}
