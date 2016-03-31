package co.sblock.commands.info;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for getting a player's ping.
 * 
 * @author Jikoo
 */
public class PingCommand extends SblockCommand {

	public PingCommand(Sblock plugin) {
		super(plugin, "ping");
		this.setDescription("Get your ping.");
		this.setUsage("/ping <player>");
		this.addExtraPermission("other", "helper");
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
			sender.sendMessage(getLang().getValue("core.error.invalidUser").replace("{PLAYER}", args[0]));
			return true;
		}
		sender.sendMessage(Language.getColor("player.good") + target.getName()
				+ Language.getColor("good") + "'s ping is "
				+ ((org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer) target).getHandle().ping + "ms!");
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
