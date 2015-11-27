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
import co.sblock.chat.channel.RegionChannel;
import co.sblock.commands.SblockCommand;
import co.sblock.users.Users;

/**
 * Command for leaving a chat channel.
 * 
 * @author Jikoo
 */
public class ChatLeaveCommand extends SblockCommand {

	private final Users users;
	private final ChannelManager manager;

	public ChatLeaveCommand(Sblock plugin) {
		super(plugin, "leave");
		this.users = plugin.getModule(Users.class);
		this.manager = plugin.getModule(Chat.class).getChannelManager();
		setDescription("Leave a chat channel.");
		setUsage(Color.COMMAND + "/leave <channel>"
				+ Color.GOOD + ": Stop listening to <channel>.");
		setAliases("quit");
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
		if (manager.getChannel(args[0]) instanceof RegionChannel) {
			sender.sendMessage(ChatMsgs.errorRegionChannelLeave());
			return true;
		}
		users.getUser(((Player) sender).getUniqueId()).removeListening(args[0]);
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
