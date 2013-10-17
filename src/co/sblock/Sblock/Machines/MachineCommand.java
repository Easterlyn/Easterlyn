/**
 * 
 */
package co.sblock.Sblock.Machines;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;
import co.sblock.Sblock.Machines.Type.MachineType;

/**
 * @author Jikoo
 *
 */
public class MachineCommand implements CommandListener {

	@SblockCommand(consoleFriendly = false, mergeLast = false)
	public boolean sm(CommandSender sender, String type) {
		((Player) sender).getInventory().addItem(MachineType.getType(type).getUniqueDrop());
		return true;
	}

}
