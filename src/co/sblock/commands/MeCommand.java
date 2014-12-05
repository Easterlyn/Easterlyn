package co.sblock.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

/**
 * SblockCommand for performing an emote.
 * 
 * @author Jikoo
 */
public class MeCommand extends SblockCommand {

	public MeCommand() {
		super("me");
		this.setDescription("#>does an action");
		this.setUsage("YOU FOOKIN WOT M8? /me (@channel) <message> Channel optional, defaults current.");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args.length == 0) {
			return false;
		}
		((Player) sender).chat("#>" + StringUtils.join(args, ' '));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player)) {
			return ImmutableList.of("NoConsoleSupport");
		}
 		if (args.length != 1 || !args[0].isEmpty() && args[0].charAt(0) != '@') {
			return super.tabComplete(sender, alias, args);
		}
		OfflineUser user = Users.getGuaranteedUser(((Player) sender).getUniqueId());
		ArrayList<String> matches = new ArrayList<>();
		String toMatch = args[0].substring(1);
		for (String s : user.getListening()) {
			if (StringUtil.startsWithIgnoreCase(s, toMatch)) {
				matches.add('@' + s);
			}
		}
		return matches;
	}
}
