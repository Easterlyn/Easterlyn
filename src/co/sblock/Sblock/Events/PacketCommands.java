/**
 * 
 */
package co.sblock.Sblock.Events;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;

/**
 * @author Jikoo
 *
 */
public class PacketCommands implements CommandListener {

	@SblockCommand
	public boolean dragon(CommandSender sender) {
		if (sender.hasPermission("group.horrorterror")) {
			EventModule.getEventModule().getListener().dragon(((Player) sender).getLocation());
			sender.sendMessage("Dragon attempt");
		}
		return true;
	}

	@SblockCommand
	public boolean quit(CommandSender sender) {
		EventModule.getEventModule().getListener().forceCloseClient((Player) sender);
		return false;
	}
}
