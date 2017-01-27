package com.easterlyn.commands.admin;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.SblockCommand;
import com.easterlyn.micromodules.FreeCart;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * SblockCommand for spawning a temporary minecart.
 * 
 * @author Jikoo
 */
public class TemporaryCart extends SblockCommand {

	private final FreeCart carts;

	public TemporaryCart(Easterlyn plugin) {
		super(plugin, "tempcart");
		this.setDescription("Spawns a temporary minecart with specified velocity vector at location, then mounts player.");
		this.setUsage("/tempcart <player> <locX> <locY> <locZ> <vecX> <vecZ>");
		this.setPermissionLevel(UserRank.DENIZEN);
		this.carts = plugin.getModule(FreeCart.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!carts.isEnabled()) {
			sender.sendMessage("FreeCart module is disabled!");
			return true;
		}
		if (args == null || args.length < 6) {
			return false;
		}
		Player pTarget = Bukkit.getPlayer(args[0]);
		if (pTarget == null) {
			return true;
		}
		try {
			Location cartDest = new Location(pTarget.getWorld(), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
			Vector cartVector = new Vector(Double.valueOf(args[4]), 0, Double.valueOf(args[5]));
			carts.spawnCart(pTarget, cartDest, cartVector);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (args.length == 1) {
			return super.tabComplete(sender, alias, args);
		}
		if (args.length < 7) {
			return ImmutableList.of("0");
		}
		return ImmutableList.of();
	}
}
