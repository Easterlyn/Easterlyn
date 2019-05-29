package com.easterlyn.commands.utility;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.micromodules.Spectators;
import com.easterlyn.users.User;
import com.easterlyn.users.UserRank;
import com.easterlyn.users.Users;

import com.google.common.collect.ImmutableList;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * EasterlynCommand for toggling spectate mode.
 * 
 * @author Jikoo
 */
public class SpectateCommand extends EasterlynCommand {

	private final Spectators spectators;
	private final Users users;

	public SpectateCommand(Easterlyn plugin) {
		super(plugin, "spectate");
		this.setAliases("spec", "spectator");
		this.setPermissionLevel(UserRank.MEMBER);
		this.spectators = plugin.getModule(Spectators.class);
		this.users = plugin.getModule(Users.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		if (!spectators.isEnabled()) {
			sender.sendMessage(getLang().getValue("core.error.moduleDisabled").replace("{MODULE}", "Spectate"));
			return true;
		}
		Player player = (Player) sender;
		User user = users.getUser(player.getUniqueId());
		if (args.length > 0) {
			args[0] = args[0].toLowerCase();
			if (args[0].equals("on") || args[0].equals("allow") || args[0].equals("true")) {
				user.setSpectatable(true);
				player.sendMessage(getLang().getValue("command.spectate.allow"));
				return true;
			}
			if (args[0].equals("off") || args[0].equals("deny") || args[0].equals("false")) {
				user.setSpectatable(false);
				player.sendMessage(getLang().getValue("command.spectate.deny"));
				return true;
			}
			sender.sendMessage(this.getUsage());
			return true;
		}
		if (spectators.isSpectator(player.getUniqueId())) {
			player.sendMessage(getLang().getValue("spectators.return.standard"));
			spectators.removeSpectator(player, false);
		} else {
			if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.SPECTATOR) {
				player.sendMessage(getLang().getValue("command.spectate.gamemode"));
				return true;
			}
			player.sendMessage(getLang().getValue("spectators.initiate"));
			spectators.addSpectator(player);
		}
		return true;
	}

	@NotNull
	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args)
			throws IllegalArgumentException {
		// CHAT: tab-complete allow/deny
		return ImmutableList.of();
	}
}
