package co.sblock.commands.utility;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableList;

import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for printing information about an item.
 * 
 * @author Jikoo
 */
public class ItemInformationCommand extends SblockCommand {

	public ItemInformationCommand() {
		super("iteminfo");
		this.setDescription("Serializes item in hand and prints the result.");
		this.setUsage("/iteminfo");
		this.setPermissionLevel("felt");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		ItemStack hand = ((Player) sender).getItemInHand();
		if (hand == null) {
			sender.sendMessage("NULL");
			return true;
		}
		sender.sendMessage(ChatColor.stripColor(hand.toString()));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || !sender.hasPermission(this.getPermission()) || args.length > 0) {
			return ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}
}
