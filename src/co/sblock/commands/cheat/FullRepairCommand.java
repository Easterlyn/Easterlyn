package co.sblock.commands.cheat;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for repairing an item
 * 
 * @author Jikoo
 */
public class FullRepairCommand extends SblockCommand {

	public FullRepairCommand() {
		super("fullrepair");
		this.setDescription("Fully repairs an item, including wiping the anvil tag.");
		this.setUsage("Run /fullrepair while holding an item.");
		this.setPermissionLevel("denizen");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player player = (Player) sender;
		ItemStack hand = player.getItemInHand();
		if (hand == null) {
			return false;
		}
		if (hand.getType().getMaxDurability() > 0) {
			hand.setDurability((short) 0);
		}
		if (hand.hasItemMeta()) {
			ItemMeta meta = hand.getItemMeta();
			Repairable repairable = (Repairable) meta;
			repairable.setRepairCost(0);
			hand.setItemMeta(meta);
		}
		player.setItemInHand(hand);
		player.sendMessage(Color.GOOD + "Fully repaired!");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
