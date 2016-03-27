package co.sblock.commands.chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.Chat;
import co.sblock.chat.Language;
import co.sblock.chat.channel.Channel;
import co.sblock.commands.SblockCommand;
import co.sblock.users.User;
import co.sblock.users.Users;

/**
 * Command for bypassing channel allowances to silently force a join.
 * 
 * @author Jikoo
 */
public class ChannelBypassFocusCommand extends SblockCommand {

	private final Users users;
	private final ChannelManager manager;

	public ChannelBypassFocusCommand(Sblock plugin) {
		super(plugin, "totalfocus");
		this.users = plugin.getModule(Users.class);
		manager = plugin.getModule(Chat.class).getChannelManager();
		this.setPermissionMessage("Try /join <channel>");
		this.setPermissionLevel("felt");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			return false;
		}
		Channel channel = manager.getChannel(args[0]);
		if (channel == null) {
			sender.sendMessage(getLang().getValue("chat.error.invalidChannel").replace("{CHANNEL}", args[0]));
			return true;
		}
		Player player = Bukkit.getPlayer(args[1]);
		if (player == null) {
			sender.sendMessage(getLang().getValue("core.error.invalidUser").replace("{PLAYER}", args[1]));
			return true;
		}
		User user = users.getUser(player.getUniqueId());
		user.getListening().add(channel.getName());
		user.currentChannel = channel.getName();
		channel.getListening().add(player.getUniqueId());
		sender.sendMessage(Language.getColor("good") + "Jacked in!");
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
			for (String channel : manager.getChannelList().keySet()) {
				if (StringUtil.startsWithIgnoreCase(channel, args[0])) {
					matches.add(channel);
				}
			}
			return matches;
		}
	}

}
