package co.sblock.commands.teleportation;

import java.util.ArrayList;
import java.util.List;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;
import co.sblock.users.User;
import co.sblock.users.UserAspect;
import co.sblock.users.Users;

import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

/**
 * SblockCommand for warping a User only if their aspect matches the target warp.
 * 
 * @author Jikoo
 */
public class AspectWarpCommand extends SblockCommand {

	private final Users users;

	public AspectWarpCommand(Sblock plugin) {
		super(plugin, "aspectwarp");
		this.users = plugin.getModule(Users.class);
		this.setDescription("Warps player if aspect matches warp name.");
		this.setUsage("/aspectwarp <warp> <player>");
		this.setPermissionLevel("felt");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args == null || args.length < 2) {
			return false;
		}
		Player player = Bukkit.getPlayer(args[1]);
		if (player == null) {
			sender.sendMessage(getLang().getValue("core.error.invalidUser").replace("{PLAYER}", args[1]));
			return true;
		}
		User user = users.getUser(player.getUniqueId());
		if (!user.getUserAspect().getDisplayName().equalsIgnoreCase(args[0])) {
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
				if (StringUtil.startsWithIgnoreCase(aspect.getDisplayName(), args[0])) {
					matches.add(aspect.getDisplayName());
				}
			}
			return matches;
		} else {
			return ImmutableList.of();
		}
	}
}
