package co.sblock.commands;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.utilities.inventory.InventoryUtils;

/**
 * SblockCommand for removing NBT tags from an item. Attempts to preserve standard meta.
 * 
 * @author Jikoo
 */
public class NBTClearCommand extends SblockCommand {

	public NBTClearCommand() {
		super("clearnbt");
		this.setAliases(Arrays.asList("cleannbt"));
		this.setDescription("Clear some NBT tags off an item. Preserves most meta.");
		this.setUsage("Run /cleannbt while holding an item");
		this.setPermission("group.felt");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player player = (Player) sender;
		player.setItemInHand(InventoryUtils.cleanNBT(player.getItemInHand()));
		player.sendMessage(ChatColor.GREEN + "NBT cleared!");
		return true;
	}
}
