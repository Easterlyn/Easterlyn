package co.sblock.commands.chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.Chat;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.Color;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.RegionChannel;
import co.sblock.commands.SblockCommand;
import co.sblock.users.User;
import co.sblock.users.Users;

/**
 * Command for joining or focusing on a chat channel.
 * 
 * @author Jikoo
 */
public class ChatFocusCommand extends SblockCommand {

	private final Users users;
	private final ChannelManager manager;

	public ChatFocusCommand(Sblock plugin) {
		super(plugin, "focus");
		this.users = plugin.getModule(Users.class);
		this.manager = plugin.getModule(Chat.class).getChannelManager();
		setDescription("Join or focus on a chat channel.");
		setUsage(Color.COMMAND + "/join <channel>"
				+ Color.GOOD + ": Join or focus on <channel>.");
		setAliases("join", "current");
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
		User user = users.getUser(((Player) sender).getUniqueId());
		Channel channel = manager.getChannel(args[0]);
		if (channel == null) {
			user.sendMessage(ChatMsgs.errorInvalidChannel(args[0]));
			return true;
		}
		if (channel instanceof RegionChannel && !user.isListening(channel)) {
			user.sendMessage(ChatMsgs.errorRegionChannelJoin());
			return true;
		}
		channel.updateLastAccess();
		user.setCurrentChannel(channel);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || args.length > 1) {
			return ImmutableList.of();
		}
		if (args.length ==  1) {
			ArrayList<String> matches = new ArrayList<>();
			for (String channel : manager.getChannelList().keySet()) {
				if (StringUtil.startsWithIgnoreCase(channel, args[0])) {
					matches.add(channel);
				}
			}
			return matches;
		}
		return super.tabComplete(sender, alias, args);
	}
}
