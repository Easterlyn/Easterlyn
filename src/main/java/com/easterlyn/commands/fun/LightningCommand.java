package com.easterlyn.commands.fun;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.StringUtil;

/**
 * EasterlynCommand for causing a lightning strike.
 * 
 * @author Jikoo
 */
public class LightningCommand extends EasterlynCommand {

	public LightningCommand(Easterlyn plugin) {
		super(plugin, "lightning");
		this.setAliases("shock", "smite", "thor");
		this.setDescription("Strike lightning at coordinates, cursor, or a player.");
		this.setPermissionLevel(UserRank.ADMIN);
		this.setUsage("/lightning [<player>|[world] <x> <y> <z>]");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				BlockIterator iterator = new BlockIterator(player, 64);
				Block block = null;
				while (iterator.hasNext()) {
					block = iterator.next();
					if (block.getType() != Material.AIR) {
						break;
					}
				}
				if (block == null || block.getType() == Material.AIR) {
					sender.sendMessage(Language.getColor("bad") + "No target block within 64 blocks!");
					return true;
				}
				block.getWorld().strikeLightning(block.getLocation());
				return true;
			}
			// Console help
			sender.sendMessage("/lightning <player>|<world> <x> <y> <z>");
			return true;
		}
		if (args.length == 1) {
			List<Player> matches = Bukkit.matchPlayer(args[0]);
			if (matches.size() == 0) {
				sender.sendMessage(getLang().getValue("core.error.invalidUser").replace("{PLAYER}", args[0]));
				return false;
			}
			Player target = matches.get(0);
			target.getWorld().strikeLightning(target.getLocation());
		}
		if (args.length > 2) {
			World world = Bukkit.getWorld(args[0]);
			if (world == null) {
				if (sender instanceof Entity) {
					world = ((Entity) sender).getWorld();
				} else if (sender instanceof BlockCommandSender) {
					world = ((BlockCommandSender) sender).getBlock().getWorld();
				} else {
					return false;
				}
				if (world == null) {
					return false;
				}
			}
			try {
				Location strike = new Location(world, Double.parseDouble(args[args.length - 3]),
						Double.parseDouble(args[args.length - 2]),
						Double.parseDouble(args[args.length - 1]));
				world.strikeLightning(strike);
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(getPermission())) {
			return ImmutableList.of();
		}
		if (args.length == 1) {
			// Super method should always return an ArrayList of player name matches as we already checked permissions
			List<String> completions = super.tabComplete(sender, alias, args);
			for (World world : Bukkit.getWorlds()) {
				if (StringUtil.startsWithIgnoreCase(world.getName(), args[0])) {
					completions.add(world.getName());
				}
			}
			return completions;
		}
		return ImmutableList.of();
	}

}
