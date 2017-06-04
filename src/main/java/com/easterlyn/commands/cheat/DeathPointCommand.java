package com.easterlyn.commands.cheat;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.micromodules.Cooldowns;
import com.easterlyn.users.User;
import com.easterlyn.users.UserRank;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.PlayerUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Cheat harder you disgusting survival-hating cheating cheater.
 * 
 * @author Jikoo
 */
public class DeathPointCommand extends EasterlynCommand {

	private final Cooldowns cooldowns;
	private final Users users;
	private final SimpleDateFormat time = new SimpleDateFormat("m:ss");

	public DeathPointCommand(Easterlyn plugin) {
		super(plugin, "deathpoint");
		this.cooldowns = plugin.getModule(Cooldowns.class);
		this.users = plugin.getModule(Users.class);

		this.setPermissionLevel(UserRank.HELPER);
		this.addExtraPermission("other", UserRank.HEAD_MOD);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player) && args.length < 1) {
			return false;
		}

		User targetUser;
		boolean targetOther = false;
		if (args.length > 0 && sender.hasPermission("easterlyn.command.deathpoint.other")) {
			Player targetPlayer = PlayerUtils.matchPlayer(args[0], false, this.getPlugin());
			if (targetPlayer == null) {
				sender.sendMessage(getLang().getValue("core.error.invalidUser").replace("{PLAYER}", args[0]));
				return true;
			}
			targetOther = true;
			targetUser = users.getUser(targetPlayer.getUniqueId());
		} else {
			targetUser = users.getUser(((Player) sender).getUniqueId());
		}

		if (!targetOther) {
			long remainder = cooldowns.getRemainder((Player) sender, this.getName());
			if (remainder > 0) {
				sender.sendMessage(getLang().getValue("command.general.cooldown")
						.replace("{TIME}", time.format(new Date(remainder))));
				if (sender.hasPermission("easterlyn.command.tpreset")) {
					sender.sendMessage(getLang().getValue("command.deathpoint.reset"));
				}
				return true;
			}
			cooldowns.addCooldown((Player) sender, this.getName(), 36000000L);
		}

		Location death = targetUser.getDeathLocation();

		if (death == null) {
			if (targetOther) {
				sender.sendMessage(getLang().getValue("command.deathpoint.unset.other").replace("{PLAYER}", targetUser.getDisplayName()));
			} else {
				sender.sendMessage(getLang().getValue("command.deathpoint.unset.self"));
			}
			return true;
		}

		targetUser.getPlayer().teleport(death, TeleportCause.COMMAND);
		if (targetOther) {
			sender.sendMessage(getLang().getValue("command.deathpoint.success.other").replace("{PLAYER}", targetUser.getDisplayName()));
		} else {
			sender.sendMessage(getLang().getValue("command.deathpoint.success.self"));
		}
		return true;
	}

}
