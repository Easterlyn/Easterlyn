package co.sblock.commands.fun;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;
import co.sblock.users.User;
import co.sblock.users.UserClass;
import co.sblock.users.Users;

/**
 * Donator perk command, classpect reselection on the fly.
 * 
 * @author Jikoo
 */
public class ClassCommand extends SblockCommand {

	private final Users users;

	public ClassCommand(Sblock plugin) {
		super(plugin, "class");
		this.setPermissionLevel("donator");
		this.users = plugin.getModule(Users.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		if (args.length == 0) {
			return false;
		}
		UserClass userClass;
		try {
			userClass = UserClass.valueOf(args[0].toUpperCase());
		} catch (IllegalArgumentException e) {
			sender.sendMessage(getLang().getValue("command.general.invalidParameters").replace("{PARAMETER}", args[0]));
			return true;
		}
		User user = users.getUser(((Player) sender).getUniqueId());
		user.setUserClass(userClass.name());
		sender.sendMessage(getLang().getValue("command.class.success").replace("{CLASS}", userClass.getDisplayName()));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || !sender.hasPermission(this.getPermission())
				|| args.length == 0 || args.length > 1) {
			return ImmutableList.of();
		}
		args[0] = args[0].toUpperCase();
		ArrayList<String> matches = new ArrayList<>();
		for (UserClass userclass : UserClass.values()) {
			if (userclass.name().startsWith(args[0])) {
				matches.add(userclass.name());
			}
		}
		return matches;
	}

}
