package com.easterlyn.commands.cheat;

import java.util.ArrayList;
import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

/**
 * EasterlynCommand for toggling or setting flight status.
 * 
 * @author Jikoo
 */
public class FlyCommand extends EasterlynCommand {

	// TODO convert to lang
	public FlyCommand(Easterlyn plugin) {
		super(plugin, "fly");
		this.setDescription("Toggle flight for yourself or another player.");
		this.setPermissionLevel(UserRank.HELPER);
		this.setUsage("/fly [player] [true|false]");
		this.addExtraPermission("other", UserRank.FELT);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player) && (!sender.hasPermission("easterlyn.command.fly.other")
				|| args.length < 1)) {
			return false;
		}

		// No arguments, simple toggle. Already has to be a Player.
		if (args.length == 0) {
			fly((Player) sender, null);
			sender.sendMessage(Language.getColor("good") + "Flight toggled!");
			return true;
		}

		// Set flight toggle based on last argument
		String lastArg = args[args.length - 1].toLowerCase();
		Boolean fly = null;
		if (lastArg.equals("true")) {
			fly = true;
		} else if (lastArg.equals("false")) {
			fly = false;
		}

		// Fetch target player
		Player player = null;
		if (!(sender instanceof Player)) {
			// Not a player, must have at least 1 argument, which will be interpreted as a player name
			// This will still be hit by /fly true and such, but that's hardly a problem.
			player = matchPlayer(args[0]);
		} else if (!sender.hasPermission("easterlyn.command.fly.other")) {
			// No permission to specify others and must be a Player
			player = (Player) sender;
		} else if (args.length == 1 && fly == null) {
			// Only 1 argument and flight toggle is not set, so it must be a player name
			player = matchPlayer(args[0]);
		}
		if (player == null) {
			return false;
		}

		sender.sendMessage(Language.getColor("good") + "Flight " + (fly == null ? "toggled" : "set " + fly)
				+ (sender.equals(player) ? "!" : " for " + player.getName() + "!"));
		fly(player, fly);
		return true;
	}

	private Player matchPlayer(String arg) {
		List<Player> players = Bukkit.matchPlayer(arg);
		if (players.isEmpty()) {
			return null;
		}
		return players.get(0);
	}

	private void fly(Player player, Boolean fly) {
		if (fly == null) {
			fly = !player.getAllowFlight();
		}
		player.setAllowFlight(fly);
		player.setFlying(fly);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission()) || args.length > 2
				|| (!sender.hasPermission("easterlyn.command.fly.other") && args.length > 1)) {
			return ImmutableList.of();
		}
		List<String> matches = new ArrayList<>();
		if (StringUtil.startsWithIgnoreCase("true", args[0])) {
			matches.add("true");
		}
		if (StringUtil.startsWithIgnoreCase("false", args[0])) {
			matches.add("false");
		}
		if (args.length == 1 && sender.hasPermission("easterlyn.command.fly.other")) {
			matches.addAll(super.tabComplete(sender, alias, args));
		}
		return matches;
	}

}
