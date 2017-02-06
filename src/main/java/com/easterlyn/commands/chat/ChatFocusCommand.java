package com.easterlyn.commands.chat;

import java.util.ArrayList;
import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.ChannelManager;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.RegionChannel;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;

import com.google.common.collect.ImmutableList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

/**
 * Command for joining or focusing on a chat channel.
 * 
 * @author Jikoo
 */
public class ChatFocusCommand extends EasterlynCommand {

	private final Users users;
	private final ChannelManager manager;

	public ChatFocusCommand(Easterlyn plugin) {
		super(plugin, "focus");
		setAliases("join", "current");
		this.users = plugin.getModule(Users.class);
		this.manager = plugin.getModule(Chat.class).getChannelManager();
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
		User user = users.getUser(((Player) sender).getUniqueId());
		Channel channel = manager.getChannel(args[0]);
		if (channel == null) {
			user.sendMessage(getLang().getValue("chat.error.invalidChannel").replace("{CHANNEL}", args[0]));
			return true;
		}
		if (channel instanceof RegionChannel && !user.isListening(channel)) {
			user.sendMessage(getLang().getValue("chat.error.globalJoin"));
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
