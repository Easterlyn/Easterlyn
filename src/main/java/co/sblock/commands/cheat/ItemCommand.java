package co.sblock.commands.cheat;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommandAlias;

/**
 * SblockCommandAlias for using a /give command targeting oneself.
 * 
 * @author Jikoo
 */
public class ItemCommand extends SblockCommandAlias {

	public ItemCommand(Sblock plugin) {
		super(plugin, "item", "minecraft:give");
		this.setAliases("i");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		String[] newArgs = new String[args.length + 1];
		newArgs[0] = "@p";
		System.arraycopy(args, 0, newArgs, 1, args.length);
		return getCommand().execute(sender, label, newArgs);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player)) {
			return ImmutableList.of();
		}
		String[] newArgs = new String[args.length + 1];
		newArgs[0] = "@p";
		System.arraycopy(args, 0, newArgs, 1, args.length);
		return getCommand().tabComplete(sender, alias, newArgs);
	}

}
