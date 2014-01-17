package co.sblock.Sblock.Machines;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;
import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.Machines.Type.MachineType;

/**
 * @author Jikoo
 */
public class MachineCommand implements CommandListener {

	/**
	 * Command for getting a Machine ItemStack.
	 * <p>
	 * Admin only.
	 * 
	 * @param sender
	 *            the <code>CommandSender</code>
	 * @param type
	 *            the <code>String</code> to interpret into
	 *            <code>MachineType</code>
	 * @return true
	 */
	@SblockCommand(description = "Machine get", usage = "/sm <type>")
	public boolean sm(CommandSender sender, String[] type) {
		if (!sender.hasPermission("group.horrorterror")) {
			sender.sendMessage(ChatMsgs.permissionDenied());
			return true;
		}
		if (type == null || type.length == 0) {
			return false;
		}
		try {
			((Player) sender).getInventory().addItem(MachineType.getType(type[0]).getUniqueDrop());
		} catch (Exception e) {
			SblockMachines.getMachines().getLogger().fine("Invalid machine: " + type[0]);
		}
		return true;
	}

}
