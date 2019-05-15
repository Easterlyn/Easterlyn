package com.easterlyn.commands.admin;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;
import com.easterlyn.machines.type.computer.Program;
import com.easterlyn.machines.type.computer.Programs;
import com.easterlyn.machines.utilities.Direction;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.TextUtils;
import com.easterlyn.utilities.tuple.Pair;
import com.google.common.collect.ImmutableList;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * EasterlynCommand for getting machine key objects and icons.
 *
 * @author Jikoo
 */
public class MachineCommand extends EasterlynCommand {

	private final Machines machines;

	public MachineCommand(Easterlyn plugin) {
		super(plugin, "sm");
		this.setDescription("Machinations.");
		this.setPermissionLevel(UserRank.ADMIN);
		this.setUsage("/sm get|icon <type>");
		this.machines = plugin.getModule(Machines.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		if (args == null || args.length < 2) {
			return false;
		}
		Player player = (Player) sender;
		if (args[0].equalsIgnoreCase("get")) {
			try {
				player.getInventory().addItem(machines.getMachineByName(TextUtils.join(args, ' ', 1, args.length)).getUniqueDrop());
				sender.sendMessage("Machine get!");
			} catch (Exception e) {
				StringBuilder sb = new StringBuilder("Valid types: ");
				for (String name : machines.getMachinesByName().keySet()) {
					sb.append(name).append(' ');
				}
				sender.sendMessage(sb.toString());
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("make")) {
			Machine machine = machines.getMachineByName(TextUtils.join(args, ' ', 1, args.length));
			Pair<Machine, ConfigurationSection> machinePair = machines.addMachine(player.getLocation(),
					machine.getName(), player.getUniqueId(),
					Direction.getFacingDirection(player));
			if (machinePair == null) {
				StringBuilder sb = new StringBuilder("Valid types: ");
				for (String name : machines.getMachinesByName().keySet()) {
					sb.append(name).append(' ');
				}
				sender.sendMessage(sb.toString());
			} else if (machinePair.getLeft().assemble(player, machinePair.getRight())) {
				sender.sendMessage("OK!");
			} else {
				sender.sendMessage("NOT OK!");
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
			for (String argument : new String[] {"get", "icon", "make"}) {
				if (argument.startsWith(args[0])) {
					matches.add(argument);
				}
			}
			return matches;
		}
		args[1] = args[1].toUpperCase();
		if (args[0].equals("get") || args[0].equals("make")) {
			for (String type : machines.getMachinesByName().keySet()) {
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
