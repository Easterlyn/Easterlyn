package com.easterlyn.commands.admin;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.micromodules.Cooldowns;
import com.easterlyn.users.UserRank;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * EasterlynCommand for resetting TPA timer. Intended for official business use only.
 *
 * @author Jikoo
 */
public class CooldownResetCommand extends EasterlynCommand {

	private final Cooldowns cooldowns;

	public CooldownResetCommand(Easterlyn plugin) {
		super(plugin, "tpreset");
		this.setPermissionLevel(UserRank.STAFF);
		this.cooldowns = plugin.getModule(Cooldowns.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		Player player = (Player) sender;
		if (args.length > 1) {
			for (String cooldown : args) {
				cooldowns.clearCooldown(player, cooldown);
			}
		} else {
			cooldowns.clearCooldown(player, "back");
			cooldowns.clearCooldown(player, "deathpoint");
			cooldowns.clearCooldown(player, "teleportRequest");
		}
		sender.sendMessage(getLang().getValue("command.tpreset.success"));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		if (args.length > 0) {
			return this.completeArgument(args[args.length - 1], "back", "deathpoint", "teleportRequest");
		}
		return com.google.common.collect.ImmutableList.of();
	}

}
