package co.sblock.commands.entry;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

import co.sblock.commands.SblockCommand;
import co.sblock.machines.Machines;
import co.sblock.machines.type.computer.Program;
import co.sblock.machines.type.computer.Programs;

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
		this.setPermissionLevel("denizen");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args == null || args.length < 2) {
			return false;
		}
		if (args[0].equalsIgnoreCase("get")) {
			try {
				((Player) sender).getInventory().addItem(Machines.getMachineByName(args[1]).getUniqueDrop());
				sender.sendMessage("Machine get!");
			} catch (Exception e) {
				StringBuilder sb = new StringBuilder("Valid types: ");
				for (String name : Machines.getMachinesByName().keySet()) {
					sb.append(name).append(' ');
				}
				sender.sendMessage(sb.toString());
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("icon")) {
			try {
				((Player) sender).getInventory().addItem(Programs.getProgramByName(args[1]).getInstaller());
				sender.sendMessage("Installer get!");
			} catch (Exception e) {
				StringBuilder sb = new StringBuilder("Valid types: ");
				for (Program program : Programs.getPrograms()) {
					if (program.getInstaller() != null) {
						sb.append(program.getName()).append(' ');
					}
				}
				sender.sendMessage(sb.toString());
			}
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || !sender.hasPermission(this.getPermission())
				|| args.length < 1 || args.length > 2) {
			return ImmutableList.of();
		}
		ArrayList<String> matches = new ArrayList<>();
		args[0] = args[0].toLowerCase();
		if (args.length == 1) {
			String argument = "get";
			if (argument.startsWith(args[0])) {
				matches.add(argument);
			}
			argument = "icon";
			if (argument.startsWith(args[0])) {
				matches.add(argument);
			}
			return matches;
		}
		args[1] = args[1].toUpperCase();
		if (args[0].equals("get")) {
			for (String type : Machines.getMachinesByName().keySet()) {
				if (StringUtil.startsWithIgnoreCase(type, args[1])) {
					matches.add(type);
				}
			}
			return matches;
		}
		if (args[0].equals("icon")) {
			for (Program program : Programs.getPrograms()) {
				if (program.getInstaller() != null && StringUtil.startsWithIgnoreCase(program.getName(), args[1])) {
					matches.add(program.getName());
				}
			}
			return matches;
		}
		return matches;
	}
}