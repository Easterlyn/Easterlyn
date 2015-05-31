package co.sblock.commands.utility;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;

/**
 * Yeah, screw the new mechanics.
 * 
 * @author Jikoo
 */
public class RenderMapCommand extends SblockCommand {

	public RenderMapCommand() {
		super("rendermap");
		setPermissionLevel("helper");
		setUsage("/rendermap [x] [z] [world]");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player player = (Player) sender;
		ItemStack mapItem = player.getItemInHand();
		if (mapItem.getType() != Material.EMPTY_MAP) {
			sender.sendMessage(Color.BAD + "You must be holding a blank map in your hand.");
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
				sender.sendMessage(Color.BAD + "Invalid coordinates! Ex. /rendermap 0 0");
				return true;
			}
			if (args.length > 2) {
				World world = Bukkit.getWorld(args[2]);
				if (world == null) {
					sender.sendMessage(Color.BAD + "Invalid world! Ex. /rendermap 0 0 Earth_the_end");
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
		player.setItemInHand(new ItemStack(Material.MAP, 1, view.getId()));
		// future: render whole map
		//((org.bukkit.craftbukkit.v1_8_R1.map.CraftMapView) view).render((org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer) player);
		//player.sendMap(view);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
