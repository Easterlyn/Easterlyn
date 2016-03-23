package co.sblock.commands.cheat;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for repairing an item.
 * 
 * @author Jikoo
 */
public class RepairCommand extends SblockCommand {

	public RepairCommand(Sblock plugin) {
		super(plugin, "repair");
		this.setDescription("Fully repairs an item, including wiping the anvil tag.");
		this.setUsage("Run /repair [full] while holding an item in main hand.");
		this.setPermissionLevel("denizen");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player player = (Player) sender;
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand == null || hand.getType() == Material.AIR) {
			return false;
		}
		if (hand.getType().getMaxDurability() > 0) {
			hand.setDurability((short) 0);
		}
		if (args.length > 0 && hand.hasItemMeta() && args[0].equalsIgnoreCase("full")) {
			ItemMeta meta = hand.getItemMeta();
			Repairable repairable = (Repairable) meta;
			repairable.setRepairCost(0);
			hand.setItemMeta(meta);
		}
		player.getInventory().setItemInMainHand(hand);
		player.sendMessage(Language.getColor("good") + "Repaired!");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (!sender.hasPermission(this.getPermission()) || args.length != 1) {
			return ImmutableList.of();
		}
		return ImmutableList.of("full");
	}

}
