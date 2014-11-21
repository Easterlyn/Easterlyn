package co.sblock.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.chat.ChatMsgs;
import co.sblock.users.User;
import co.sblock.users.UserManager;

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
		this.setPermission("group.felt");
	}

	public boolean execute(CommandSender sender, String label, String[] args) {
		if (args == null || args.length < 2) {
			return false;
		}
		Player p = Bukkit.getPlayer(args[1]);
		if (p == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(args[1]));
			return true;
		}
		User u = UserManager.getUser(p.getUniqueId());
		if (!u.getAspect().name().equalsIgnoreCase(args[0])) {
			return true;
		}
		sender.getServer().dispatchCommand(sender, "warp " + args[0] + " " + args[1]);
		return true;
	}
}
