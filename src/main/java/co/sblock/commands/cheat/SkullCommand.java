package co.sblock.commands.cheat;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommandAlias;

/**
 * SblockCommandAlias for spawning or changing a skull to a particular owner.
 * 
 * @author Jikoo
 */
public class SkullCommand extends SblockCommandAlias {

	public SkullCommand(Sblock plugin) {
		// TODO this uses our own /lore, there may need to be a safer way to do this to ensure it's loaded first.
		super(plugin, "skull", "lore");
		this.setAliases("head");
		this.setUsage("/skull <player>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args.length == 0) {
			return false;
		}

		String[] newArgs = new String[2];
		newArgs[0] = "owner";
		newArgs[1] = args[0];
		Player player = (Player) sender;
		ItemStack hand = player.getItemInHand();
		if (hand == null || hand.getType() != Material.SKULL_ITEM) {
			player.setItemInHand(new ItemStack(Material.SKULL_ITEM, 1, (short) 3));
			if (hand != null) {
				player.getLocation().getWorld().dropItem(player.getLocation(), hand).setPickupDelay(0);
			}
		}
		getCommand().execute(sender, label, newArgs);
		// This being a SblockCommand, it will handle its own permissions and usage when execute fails.
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (args.length == 1) {
			return super.tabComplete(sender, alias, args);
		}
		return com.google.common.collect.ImmutableList.of();
	}

}
