package co.sblock.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
			sender.sendMessage(ChatColor.RED + "You must be holding a blank map in your hand.");
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
				sender.sendMessage(ChatColor.RED + "Invalid coordinates! Ex. /rendermap 0 0");
				return true;
			}
			if (args.length > 2) {
				World world = Bukkit.getWorld(args[2]);
				if (world == null) {
					sender.sendMessage(ChatColor.RED + "Invalid world! Ex. /rendermap 0 0 Earth_the_end");
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
		// TODO try to render whole map
		//((org.bukkit.craftbukkit.v1_8_R1.map.CraftMapView) view).render((org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer) player);
		//player.sendMap(view);
		return true;
	}
}
