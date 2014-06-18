package co.sblock.machines;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.machines.utilities.Icon;
import co.sblock.machines.utilities.MachineType;
import co.sblock.module.CommandDenial;
import co.sblock.module.CommandDescription;
import co.sblock.module.CommandListener;
import co.sblock.module.CommandPermission;
import co.sblock.module.CommandUsage;
import co.sblock.module.SblockCommand;

/**
 * @author Jikoo
 */
public class MachineCommand implements CommandListener {

	/**
	 * Command for getting a Machine ItemStack.
	 * <p>
	 * Admin only.
	 * 
	 * @param sender the CommandSender
	 * @param args the String to interpret into MachineType
	 * 
	 * @return true
	 */
	@CommandDenial
	@CommandDescription("Machinations")
	@CommandPermission("group.denizen")
	@CommandUsage("/sm get|icon <type>")
	@SblockCommand
	public boolean sm(CommandSender sender, String[] args) {
		if (args == null || args.length < 2) {
			return false;
		}
		if (args[0].equalsIgnoreCase("get")) {
			try {
				((Player) sender).getInventory().addItem(
						MachineType.getType(args[1]).getUniqueDrop());
				sender.sendMessage("Machine get!");
			} catch (Exception e) {
				SblockMachines.getMachines().getLogger().fine("Invalid machine: " + args[1]);
				StringBuilder sb = new StringBuilder("Valid types: ");
				for (MachineType m : MachineType.values()) {
					sb.append(m.name()).append(" (").append(m.getAbbreviation()).append(") ");
				}
				sender.sendMessage(sb.toString());
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("icon")) {
			try {
				((Player) sender).getInventory().addItem(Icon.valueOf(args[1]).getInstaller());
				sender.sendMessage("Installer get!");
			} catch (Exception e) {
				SblockMachines.getMachines().getLogger().fine("Invalid machine: " + args[1]);
				StringBuilder sb = new StringBuilder("Valid types: ");
				for (Icon i : Icon.values()) {
					if (i.getInstaller() != null) {
						sb.append(i.name()).append(' ');
					}
				}
				sender.sendMessage(sb.toString());
			}
		}
		return true;
	}

}
