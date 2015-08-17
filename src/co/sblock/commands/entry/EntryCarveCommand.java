package co.sblock.commands.entry;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.sblock.commands.SblockCommand;
import co.sblock.micromodules.Captcha;
import co.sblock.micromodules.CruxiteDowel;
import co.sblock.utilities.InventoryUtils;

/**
 * Temporary command - Currently, furnaces cannot be opened, but I still want to test the Entry process.
 * 
 * @author Jikoo
 */
public class EntryCarveCommand extends SblockCommand {

	public EntryCarveCommand() {
		super("carve");
		this.setUsage("/carve with a punchcard in hand");
		this.setPermissionLevel("denizen");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (!Captcha.isPunch(player.getItemInHand())) {
				return false;
			}
			ItemStack dowel = CruxiteDowel.carve(player.getItemInHand());
			player.setItemInHand(InventoryUtils.decrement(player.getItemInHand(), 1));
			player.getInventory().addItem(dowel);
			return true;
		}
		return false;
	}

}
