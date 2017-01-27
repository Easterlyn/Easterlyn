package com.easterlyn.commands.cheat;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.SblockCommand;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;

/**
 * Yeah, screw the new mechanics.
 * 
 * @author Jikoo
 */
public class CenterMapCommand extends SblockCommand {

	public CenterMapCommand(Easterlyn plugin) {
		super(plugin, "centermap");
		setPermissionLevel(UserRank.HELPER);
		setUsage("/centermap [x] [z] [world]");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		Player player = (Player) sender;
		ItemStack mapItem = player.getInventory().getItemInMainHand();
		if (mapItem.getType() != Material.EMPTY_MAP) {
			sender.sendMessage(Language.getColor("bad") + "You must be holding a blank map in your main hand.");
			return true;
		}
		MapView view = Bukkit.createMap(player.getWorld());
		view.setScale(Scale.FARTHEST);
		int x, z;
		if (args.length > 1) {
			try {
				x = Integer.valueOf(args[0]);
				z = Integer.valueOf(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(Language.getColor("bad") + "Invalid coordinates! Ex. /centermap 0 0");
				return true;
			}
			if (args.length > 2) {
				World world = Bukkit.getWorld(args[2]);
				if (world == null) {
					sender.sendMessage(Language.getColor("bad") + "Invalid world! Ex. /centermap 0 0 Earth_the_end");
					return true;
				}
				view.setWorld(world);
			}
		} else {
			x = player.getLocation().getBlockX();
			z = player.getLocation().getBlockZ();
		}
		view.setCenterX(x);
		view.setCenterZ(z);
		player.getInventory().setItemInMainHand(new ItemStack(Material.MAP, 1, view.getId()));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
