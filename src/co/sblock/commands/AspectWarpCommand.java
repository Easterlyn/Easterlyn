package co.sblock.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ChatMsgs;
import co.sblock.users.OfflineUser;
import co.sblock.users.UserAspect;
import co.sblock.users.Users;

/**
 * SblockCommand for warping a User only if their aspect matches the target warp.
 * 
 * @author Jikoo
 */
public class AspectWarpCommand extends SblockCommand {

	public AspectWarpCommand() {
		super("aspectwarp");
		this.setDescription("Warps player if aspect matches warp name.");
		this.setUsage("/aspectwarp <warp> <player>");
	}

	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args == null || args.length < 2) {
			return false;
		}
		Player p = Bukkit.getPlayer(args[1]);
		if (p == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(args[1]));
			return true;
		}
		OfflineUser u = Users.getGuaranteedUser(p.getUniqueId());
		if (!u.getUserAspect().name().equalsIgnoreCase(args[0])) {
			return true;
		}
		sender.getServer().dispatchCommand(sender, "warp " + args[0] + " " + args[1]);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission())) {
			return ImmutableList.of();
		}
		if (args.length == 2) {
			return super.tabComplete(sender, alias, args);
		}
		if (args.length == 1) {
			ArrayList<String> matches = new ArrayList<>();
			args[0] = args[0].toUpperCase();
			for (UserAspect aspect : UserAspect.values()) {
				if (aspect.name().startsWith(args[0])) {
					matches.add(aspect.name());
				}
			}
			return matches;
		} else {
			return ImmutableList.of();
		}
	}
}
