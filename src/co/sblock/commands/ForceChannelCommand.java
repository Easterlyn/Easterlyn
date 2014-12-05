package co.sblock.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.channel.Channel;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

/**
 * SblockCommand for forcing a User to change current channel.
 * 
 * @author Jikoo
 */
public class ForceChannelCommand extends SblockCommand {

	public ForceChannelCommand() {
		super("forcechannel");
		this.setDescription("Help people find their way.");
		this.setUsage("/forcechannel <channel> <player>");
		this.setPermission("group.felt");
		this.setPermissionMessage("Try /sc c <channel>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			return false;
		}
		Channel c = ChannelManager.getChannelManager().getChannel(args[0]);
		if (c == null) {
			sender.sendMessage(ChatMsgs.errorInvalidChannel(args[0]));
			return true;
		}
		Player p = Bukkit.getPlayer(args[1]);
		if (p == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(args[1]));
			return true;
		}
		OfflineUser user = Users.getGuaranteedUser(p.getUniqueId());
		user.setCurrentChannel(c);
		sender.sendMessage(ChatColor.GREEN + "Channel forced!");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (!sender.hasPermission(this.getPermission()) || args.length == 0 || args.length > 2) {
			return ImmutableList.of();
		}
		if (args.length == 2) {
			return super.tabComplete(sender, alias, args);
		} else {
			ArrayList<String> matches = new ArrayList<>();
			for (String channel : ChannelManager.getChannelManager().getChannelList().keySet()) {
				if (StringUtil.startsWithIgnoreCase(channel, args[0])) {
					matches.add(channel);
				}
			}
			return matches;
		}
	}
}
