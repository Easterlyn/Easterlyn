package co.sblock.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.machines.SblockMachines;
import co.sblock.machines.utilities.Icon;
import co.sblock.machines.utilities.MachineType;

/**
 * SblockCommand for getting machine key objects and icons.
 * 
 * @author Jikoo
 */
public class MachineCommand extends SblockCommand {

	public MachineCommand() {
		super("sm");
		this.setDescription("Machinations.");
		this.setUsage("/sm get|icon <type>");
		this.setPermission("group.denizen");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
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

	// TODO tab completion
}
