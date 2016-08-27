package co.sblock.commands.teleportation;

import java.util.List;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommandAlias;

import com.google.common.collect.ImmutableList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * SblockCommandAlias for teleporting a player to oneself.
 * 
 * @author Jikoo
 */
public class TeleportHereCommand extends SblockCommandAlias {

	public TeleportHereCommand(Sblock plugin) {
		super(plugin, "tphere", "minecraft:tp");
		this.setPermissionLevel("felt");
		this.setAliases("s");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		if (args.length == 0) {
			return false;
		}
		String[] newArgs = new String[2];
		newArgs[1] = "@p";
		newArgs[0] = args[0];
		return getCommand().execute(sender, label, newArgs);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || args.length != 1) {
			return ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}

}
