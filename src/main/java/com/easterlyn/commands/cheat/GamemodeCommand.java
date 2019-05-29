package com.easterlyn.commands.cheat;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommandAlias;
import com.easterlyn.users.UserRank;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * EasterlynCommandAlias for easy gamemode changing.
 * 
 * @author Jikoo
 */
public class GamemodeCommand extends EasterlynCommandAlias {

	public GamemodeCommand(Easterlyn plugin) {
		super(plugin, "gm", "minecraft:gamemode");
		// We don't have additional obvious spectator aliases because of our /spectate.
		this.setAliases("0", "surv", "survival", "gms", "1", "creative", "gmc", "2", "adventure",
				"gma", "3", "gmt");
		this.setPermissionLevel(UserRank.MOD);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 1 && !(sender instanceof Player)) {
			return false;
		}
		String[] newArgs = new String[2];
		switch (label) {
		case "0":
		case "surv":
		case "survival":
		case "gms":
			newArgs[0] = "survival";
			break;
		case "1":
		case "creative":
		case "gmc":
			newArgs[0] = "creative";
			break;
		case "2":
		case "adventure":
		case "gma":
			newArgs[0] = "adventure";
			break;
		case "3":
		case "gmt":
			newArgs[0] = "spectator";
			break;
		default:
			newArgs[0] = null;
			break;
		}

		if (newArgs[0] == null) {
			// No alias in use, gamemode is not pre-set.
			if (args.length < 1 || !(sender instanceof Player) && args.length < 2) {
				return false;
			}
			newArgs[0] = args[0];
			// If sender isn't a player, args already must be 2 or more in length.
			newArgs[1] = args.length > 1 ? args[1] : sender.getName();
		} else {
			// Gamemode is pre-set by alias.
			// If sender isn't a player, args already must be 1 or more in length.
			newArgs[1] = args.length < 1 ? sender.getName() : args[0];
		}
		getCommand().execute(sender, label, newArgs);
		return true;
	}

	@NotNull
	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args)
			throws IllegalArgumentException {
		return getCommand().tabComplete(sender, alias, args);
	}

}
