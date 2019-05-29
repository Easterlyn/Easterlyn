package com.easterlyn.commands.cheat;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommandAlias;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * EasterlynCommandAlias for using a /give command targeting oneself.
 * 
 * @author Jikoo
 */
public class ItemCommand extends EasterlynCommandAlias {

	public ItemCommand(Easterlyn plugin) {
		super(plugin, "item", "minecraft:give");
		this.setAliases("i");
		this.setPermissionLevel(UserRank.MOD);
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

	@NotNull
	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args)
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
