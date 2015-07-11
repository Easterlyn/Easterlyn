package co.sblock.commands.utility;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;

/**
 * Command for setting the amount of the ItemStack in hand.
 * 
 * @author Jikoo
 */
public class MoreCommand extends SblockCommand {

	public MoreCommand() {
		super("more");
		this.setUsage("/more [optional amount]");
		this.setPermissionLevel("felt");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player player = (Player) sender;
		ItemStack stack = player.getItemInHand();
		if (stack == null || stack.getType() == Material.AIR) {
			return false;
		}
		int amount;
		if (args.length > 0) {
			try {
				amount = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				amount = 64;
			}

			amount += stack.getAmount();

			if (amount > 64) {
				amount = 64;
			}
		} else {
			amount = 64;
		}
		player.getItemInHand().setAmount(amount);
		player.sendMessage(Color.GOOD + "Stack in hand set to " + Color.GOOD_EMPHASIS + amount);
		// TODO check if this needs inv update/setItemInHand
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
