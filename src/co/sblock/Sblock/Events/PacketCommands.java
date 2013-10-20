package co.sblock.Sblock.Events;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;

/**
 * @author Jikoo
 */
public class PacketCommands implements CommandListener {

	/**
	 * Command for faking an Ender Dragon death at the
	 * <code>CommandSender</code>.
	 * 
	 * @param sender
	 *            the <code>CommandSender</code>
	 * @return true
	 */
	@SblockCommand
	public boolean dragon(CommandSender sender) {
		if (sender.hasPermission("group.horrorterror")) {
			EventModule.getEventModule().getListener().dragon(((Player) sender).getLocation());
			sender.sendMessage("Dragon attempt");
		}
		return true;
	}

	/**
	 * Command for crashing own client (forcibly closed, no error log displayed)
	 * accidentally discovered working on /dragon.
	 * 
	 * @param sender
	 *            the <code>CommandSender</code>
	 * @return false
	 */
	@SblockCommand
	public boolean quit(CommandSender sender) {
		EventModule.getEventModule().getListener().forceCloseClient((Player) sender);
		return false;
	}
}
