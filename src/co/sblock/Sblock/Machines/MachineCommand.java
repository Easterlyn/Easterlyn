package co.sblock.Sblock.Machines;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;
import co.sblock.Sblock.Machines.Type.Icon;
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
	 * @param sender the CommandSender
	 * @param args the String to interpret into MachineType
	 * 
	 * @return true
	 */
	@SblockCommand(description = "Machine get", usage = "/sm get|icon <type>", permission = "group.denizen")
	public boolean sm(CommandSender sender, String[] args) {
		if (args == null || args.length < 2) {
			return false;
		}
		if (args[0].equalsIgnoreCase("get")) {
			try {
				((Player) sender).getInventory().addItem(MachineType.getType(args[1]).getUniqueDrop());
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
