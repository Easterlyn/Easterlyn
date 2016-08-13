package co.sblock.commands.fun;

import java.util.ArrayList;
import java.util.List;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;
import co.sblock.users.User;
import co.sblock.users.UserAspect;
import co.sblock.users.Users;

import com.google.common.collect.ImmutableList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

/**
 * Donator perk command, classpect reselection on the fly.
 * 
 * @author Jikoo
 */
public class AspectCommand extends SblockCommand {


	private final Users users;

	public AspectCommand(Sblock plugin) {
		super(plugin, "aspect");
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

		UserAspect userAspect = UserAspect.getAspect(ChatColor.translateAlternateColorCodes('&', args[0]));
		if (userAspect == UserAspect.BREATH && !args[0].equalsIgnoreCase("breath")
				|| userAspect.getDisplayName().length() < 2
				|| userAspect.getDisplayName().contains(String.valueOf(ChatColor.COLOR_CHAR))
				|| userAspect.getColor() == null) {
			sender.sendMessage(getLang().getValue("command.aspect.failure"));
			return true;
		}

		User user = users.getUser(((Player) sender).getUniqueId());
		user.setUserAspect(userAspect);
		sender.sendMessage(getLang().getValue("command.aspect.success")
				.replace("{ASPECT}", userAspect.getColor() + userAspect.getDisplayName()));
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
		for (UserAspect userAspect : UserAspect.values()) {
			if (StringUtil.startsWithIgnoreCase(userAspect.getDisplayName(), args[0])) {
				matches.add(userAspect.getDisplayName());
			}
		}
		return matches;
	}

}
