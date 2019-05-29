package com.easterlyn.commands.teleportation;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommandAlias;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * EasterlynCommandAlias for teleporting a player to oneself.
 * 
 * @author Jikoo
 */
public class TeleportHereCommand extends EasterlynCommandAlias {

	public TeleportHereCommand(Easterlyn plugin) {
		super(plugin, "tphere", "minecraft:tp");
		this.setPermissionLevel(UserRank.MOD);
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
		newArgs[1] = sender.getName();
		newArgs[0] = args[0];
		return getCommand().execute(sender, label, newArgs);
	}

	@NotNull
	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || args.length != 1) {
			return ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}

}
