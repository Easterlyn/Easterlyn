package co.sblock.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

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
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player) && args.length == 0) {
			return false;
		}
		// TODO couple samples over a short period
		Player target;
		if (args.length == 0 || !sender.hasPermission("group.helper")) {
			target = (Player) sender;
		} else {
			target = Bukkit.getPlayer(args[0]);
		}
		if (target == null) {
			sender.sendMessage(ChatColor.RED + "Unknown player " + args[0] + "!");
			return true;
		}
		sender.sendMessage(ChatColor.GREEN + target.getName() + ChatColor.YELLOW +"'s ping is " +
			((org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer) target).getHandle().ping + "ms!");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (args.length != 1 || !sender.hasPermission("group.helper")) {
			return ImmutableList.of();
		} else {
			return super.tabComplete(sender, alias, args);
		}
	}
}
