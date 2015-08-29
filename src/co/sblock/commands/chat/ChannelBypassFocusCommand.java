package co.sblock.commands.chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.Color;
import co.sblock.chat.channel.Channel;
import co.sblock.commands.SblockCommand;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

/**
 * 
 * 
 * @author Jikoo
 */
public class ChannelBypassFocusCommand extends SblockCommand {

	public ChannelBypassFocusCommand() {
		super("totalfocus");
		this.setDescription("Knock knock. Come on in.");
		this.setUsage("/totalfocus <channel> <player>");
		this.setPermissionMessage("Try /join <channel>");
		this.setPermissionLevel("felt");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			return false;
		}
		Channel channel = ChannelManager.getChannelManager().getChannel(args[0]);
		if (channel == null) {
			sender.sendMessage(ChatMsgs.errorInvalidChannel(args[0]));
			return true;
		}
		Player player = Bukkit.getPlayer(args[1]);
		if (player == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(args[1]));
			return true;
		}
		OfflineUser user = Users.getGuaranteedUser(player.getUniqueId());
		user.getListening().add(channel.getName());
		user.currentChannel = channel.getName();
		channel.getListening().add(player.getUniqueId());
		sender.sendMessage(Color.GOOD + "Jacked in!");
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
