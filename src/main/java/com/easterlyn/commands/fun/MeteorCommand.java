package com.easterlyn.commands.fun;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.micromodules.Meteorite;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.PlayerUtils;
import com.google.common.collect.ImmutableList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * EasterlynCommand for summoning a meteor.
 *
 * @author Jikoo
 */
public class MeteorCommand extends EasterlynCommand {

	private final String[] primaryArgs;

	public MeteorCommand(Easterlyn plugin) {
		super(plugin, "meteor");
		this.setDescription("Summon a meteor with parameters.");
		this.setPermissionLevel(UserRank.HEAD_MOD);
		this.setUsage("/meteor [p:player] [r:radius] [e:explode] [m:material] [b:bore]");
		primaryArgs = new String[] {"p:", "r:", "e:", "b:", "m:"};
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		Location target = null;
		int radius = -1;
		String material = null;
		boolean blockDamage = false;
		int bore = -1;
		for (String s : args) {
			if (s.length() < 3 || s.charAt(1) != ':') {
				continue;
			}
			char identifier = Character.toLowerCase(s.charAt(0));
			String argument = s.substring(2);

			// Target a player's location
			if (identifier == 'p') {
				Player pTarget = PlayerUtils.matchOnlinePlayer(sender, args[0]);
				if (pTarget != null) {
					target = pTarget.getLocation();
				}
				continue;
			}

			// Set meteor radius
			if (identifier == 'r') {
				try {
					radius = Integer.parseInt(argument);
				} catch (NumberFormatException e) {
					sender.sendMessage(Language.getColor("bad") + "Invalid radius specified, defaulting to 3");
				}
				continue;
			}

			// Set meteor explosion block damage
			if (identifier == 'e') {
				blockDamage = Boolean.valueOf(argument);
				continue;
			}

			// Set meteor material
			if (identifier == 'm') {
				material = argument.toUpperCase();
				continue;
			}

			// set meteor to bore mode (default behavior: bore if not highest block)
			if (identifier == 'b') {
				bore = argument.equalsIgnoreCase("true") ? 1 : 0;
			}
		}
		if (target == null) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				Block block = p.getTargetBlock((HashSet<Material>) null, 128);
				if (block != null) {
					target = block.getLocation();
				}
				if (target == null) {
					sender.sendMessage(Language.getColor("bad") + "Block on crosshair too far away! Aim closer or specify target!");
					return false;
				}
			} else {
				sender.sendMessage("Non-players must specify a player target!");
				return false;
			}
		}
		if (radius > 10) {
			radius = 10;
			sender.sendMessage(Language.getColor("emphasis.bad") + "Very large meteors cause quite a bit of lag. Keep it down.");
		}
		new Meteorite((Easterlyn) getPlugin(), target, material, radius, blockDamage, bore).dropMeteorite();
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission())) {
			return ImmutableList.of();
		}
		String current = args[args.length - 1];
		ArrayList<String> matches = new ArrayList<>();
		if (current.length() < 2) {
			current = current.toLowerCase();
			for (String argument : primaryArgs) {
				if (argument.startsWith(current)) {
					matches.add(argument);
				}
			}
			return matches;
		}
		String argCompleting = current.substring(0, 2).toLowerCase();
		current = current.substring(2);
		if (argCompleting.equals("p:")) {
			for (String player : super.tabComplete(sender, alias, new String[] {current})) {
				matches.add("p:" + player);
			}
			return matches;
		}
		if (argCompleting.equals("r:")) {
			matches.add("r:#");
			return matches;
		}
		if (argCompleting.equals("e:") || argCompleting.equals("b:")) {
			String arg = "true";
			if (arg.startsWith(current)) {
				matches.add(argCompleting + arg);
			}
			arg = "false";
			if (arg.startsWith(current)) {
				matches.add(argCompleting + arg);
			}
			return matches;
		}
		if (argCompleting.equals("m:")) {
			current = current.toUpperCase();
			for (Material material : Material.values()) {
				if (material.name().startsWith(current)) {
					matches.add(argCompleting + material.name());
				}
			}
			return matches;
		}
		return matches;
	}
}
