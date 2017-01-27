package com.easterlyn.commands.cheat;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.SblockCommand;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * SblockCommand for restoring health and saturation.
 * 
 * @author Jikoo
 */
public class HealCommand extends SblockCommand {

	public HealCommand(Easterlyn plugin) {
		super(plugin, "heal");
		this.setAliases("eat", "feed");
		this.setPermissionLevel(UserRank.FELT);
		this.addExtraPermission("other", UserRank.DENIZEN);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player) && (!sender.hasPermission("sblock.command.heal.other")
				|| args.length < 1)) {
			return false;
		}
		if (args.length == 0 || !sender.hasPermission("sblock.command.heal.other")) {
			heal((Player) sender, label);
			sender.sendMessage(getLang().getValue("command.heal.success"));
			return true;
		}
		List<Player> players = Bukkit.matchPlayer(args[0]);
		if (players.size() == 0) {
			return false;
		}
		heal(players.get(0), label);
		sender.sendMessage(getLang().getValue("command.heal.success"));
		return true;
	}

	private void heal(Player player, String label) {
		if (label.equalsIgnoreCase("heal")) {
			player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		}
		player.setFoodLevel(20);
		player.setSaturation(20);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission()) || args.length > 1
				|| !sender.hasPermission("sblock.command.heal.other")) {
			return ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}

}
