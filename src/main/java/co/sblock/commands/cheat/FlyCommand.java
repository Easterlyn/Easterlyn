package co.sblock.commands.cheat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;

/**
 * 
 * 
 * @author Jikoo
 */
public class FlyCommand extends SblockCommand {

	public FlyCommand(Sblock plugin) {
		super(plugin, "fly");
		this.setDescription("Toggle flight for yourself or another player.");
		this.setPermissionLevel("helper");
		this.setUsage("/fly [player] [true|false]");
		Permission permission;
		for (String permName : new String[] {"sblock.command.fly.other", "sblock.command.fly.safe"}) {
			try {
				permission = new Permission(permName, PermissionDefault.OP);
				Bukkit.getPluginManager().addPermission(permission);
			} catch (IllegalArgumentException e) {
				permission = Bukkit.getPluginManager().getPermission(permName);
				permission.setDefault(PermissionDefault.OP);
			}
			permission.addParent("sblock.command.*", true).recalculatePermissibles();
			permission.addParent("sblock.felt", true).recalculatePermissibles();
		}
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player) && (!sender.hasPermission("sblock.command.fly.other")
				|| args.length < 1)) {
			return false;
		}

		// No arguments, simple toggle. Already has to be a Player.
		if (args.length == 0) {
			fly((Player) sender, null);
			sender.sendMessage(Color.GOOD + "Flight toggled!");
			return true;
		}

		String lastArg = args[args.length - 1].toLowerCase();
		// Set flight toggle based on last argument
		Boolean fly = lastArg.equals("true") ? true : lastArg.equals("false") ? false : null;

		// Fetch target player
		Player player = null;
		if (!(sender instanceof Player)) {
			// Not a player, must have at least 1 argument, which will be interpreted as a player name
			// This will still be hit by /fly true and such, but that's hardly a problem.
			player = matchPlayer(args[0]);
		} else if (!sender.hasPermission("sblock.command.fly.other")) {
			// No permission to specify others and must be a Player
			player = (Player) sender;
		} else if (args.length == 1 && fly == null) {
			// Only 1 argument and flight toggle is not set, so it must be a player name
			player = matchPlayer(args[0]);
		}
		if (player == null) {
			return false;
		}

		sender.sendMessage(Color.GOOD + "Flight " + (fly == null ? "toggled" : "set " + fly)
				+ (sender.equals(player) ? "!" : " for " + player.getName() + "!"));
		fly(player, fly);
		return true;
	}

	private Player matchPlayer(String arg) {
		List<Player> players = Bukkit.matchPlayer(arg);
		if (players.isEmpty()) {
			return null;
		}
		return players.get(0);
	}

	private void fly(Player player, Boolean fly) {
		if (fly == null) {
			fly = !player.getAllowFlight();
		}
		player.setAllowFlight(fly);
		player.setFlying(fly);
	}

}
