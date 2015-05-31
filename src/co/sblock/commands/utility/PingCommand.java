package co.sblock.commands.utility;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for getting a player's ping.
 * 
 * @author Jikoo
 */
public class PingCommand extends SblockCommand {

	public PingCommand() {
		super("ping");
		this.setDescription("Get your ping.");
		this.setUsage("/ping <player>");
		Permission permission;
		try {
			permission = new Permission("sblock.command.ping.other", PermissionDefault.OP);
			Bukkit.getPluginManager().addPermission(permission);
		} catch (IllegalArgumentException e) {
			permission = Bukkit.getPluginManager().getPermission("sblock.command.ping.other");
			permission.setDefault(PermissionDefault.OP);
		}
		permission.addParent("sblock.command.*", true).recalculatePermissibles();
		permission.addParent("sblock.helper", true).recalculatePermissibles();
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player) && args.length == 0) {
			return false;
		}
		// future: couple samples over a short period
		Player target;
		if (args.length == 0 || !sender.hasPermission("sblock.command.ping.other")) {
			target = (Player) sender;
		} else {
			target = Bukkit.getPlayer(args[0]);
		}
		if (target == null) {
			sender.sendMessage(Color.BAD + "Unknown player " + args[0] + "!");
			return true;
		}
		sender.sendMessage(Color.GOOD_PLAYER + target.getName() + Color.GOOD +"'s ping is " +
			((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) target).getHandle().ping + "ms!");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (args.length != 1 || !sender.hasPermission("sblock.command.ping.other")) {
			return ImmutableList.of();
		} else {
			return super.tabComplete(sender, alias, args);
		}
	}
}
