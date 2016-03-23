package co.sblock.commands.utility;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.commands.SblockCommand;
import co.sblock.users.User;
import co.sblock.users.Users;

/**
 * SblockCommand for manipulating commands sent on login.
 * 
 * @author Jikoo
 */
public class LoginCommandsCommand extends SblockCommand {

	private final Users users;

	public LoginCommandsCommand(Sblock plugin) {
		super(plugin, "onlogin");
		this.users = plugin.getModule(Users.class);
		this.setDescription("Manipulate commands executed on login.");
		this.setUsage("/onlogin list\n/onlogin add /command additional arguments\n"
				+ "/onlogin delete [number]");
		Permission permission;
		try {
			permission = new Permission("sblock.command.onlogin.more", PermissionDefault.OP);
			Bukkit.getPluginManager().addPermission(permission);
		} catch (IllegalArgumentException e) {
			permission = Bukkit.getPluginManager().getPermission("sblock.command.onlogin.more");
			permission.setDefault(PermissionDefault.OP);
		}
		permission.addParent("sblock.command.*", true).recalculatePermissibles();
		permission.addParent("sblock.helper", true).recalculatePermissibles();
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args.length == 0) {
			sender.sendMessage(this.getUsage());
			return true;
		}

		Player player = (Player) sender;
		User user = users.getUser(player.getUniqueId());
		args[0] = args[0].toLowerCase();

		if (args[0].equals("list")) {
			List<String> commands = user.getLoginCommands();
			if (commands.isEmpty()) {
				sender.sendMessage(Language.getColor("good") + "No commands registered! Try /onlogin add /command");
				return true;
			}
			for (int i = 0; i < commands.size(); i++) {
				sender.sendMessage(new StringBuilder().append(Language.getColor("good")).append(i + 1)
						.append(": ").append(Language.getColor("emphasis.good")).append(commands.get(i)).toString());
			}
			return true;
		}
		if (args[0].equals("delete")) {
			delete(player, user, args);
			return true;
		}
		if (args[0].equals("add")) {
			add(player, user, args);
			return true;
		}
		sender.sendMessage(this.getUsage());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || !sender.hasPermission(this.getPermission())
				|| args.length == 0) {
			return ImmutableList.of();
		}
		args[0] = args[0].toLowerCase();
		ArrayList<String> matches = new ArrayList<String>();
		if (args.length == 1) {
			if ("add".startsWith(args[0])) {
				matches.add("add");
			}
			if ("delete".startsWith(args[0])) {
				matches.add("delete");
			}
			if (matches.isEmpty() || "list".startsWith(args[0])) {
				matches.add("list");
			}
			return matches;
		}
		if (args.length > 2) {
			return matches;
		}
		if (args[1].equals("delete")) {
			matches.add("1");
		}
		return matches;
	}

	private void delete(Player player, User user, String[] args) {
		try {
			int line = Integer.parseInt(args[1]);
			ArrayList<String> commands = new ArrayList<>(user.getLoginCommands());
			if (commands.size() < line) {
				player.sendMessage(Language.getColor("bad") + "You only have " + commands.size() + " command(s), cannot delete " + line + "!");
				return;
			}
			if (line < 1) {
				player.sendMessage(Language.getColor("bad") + "Index must be between 1 and " + commands.size() + "! " + line + " is invalid.");
				return;
			}
			String removed = commands.remove(line - 1);
			user.setLoginCommands(commands);
			player.sendMessage(Language.getColor("good") + "Deleted \"" + removed + "\"");
			return;
		} catch (NumberFormatException e) {
			player.sendMessage(Language.getColor("bad") + "/onlogin delete <number>");
			return;
		}
	}

	private void add(Player player, User user, String[] args) {
		ArrayList<String> commands = new ArrayList<String>(user.getLoginCommands());
		if (!player.hasPermission("sblock.command.onlogin.more") && commands.size() >= 2) {
			player.sendMessage(Language.getColor("bad") + "You cannot set more than two commands on login.");
			return;
		}
		if (args.length <= 1) {
			player.sendMessage(Language.getColor("bad") + "/onlogin add /command arguments");
			return;
		}
		if (args[0].charAt(0) != '/' || args[0].equalsIgnoreCase("/me")) {
			player.sendMessage(Language.getColor("bad") + "To prevent spam, you may not add chat to your onlogin.");
			return;
		}
		String command = StringUtils.join(args, ' ', 1, args.length);
		commands.add(command);
		user.setLoginCommands(commands);
		player.sendMessage(Language.getColor("good") + "Added \"" + command + "\"");
		return;
	}
}
