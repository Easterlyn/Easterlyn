package com.easterlyn.commands.chat;

import java.util.ArrayList;
import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.ChannelManager;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.channel.RegionChannel;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.Users;

import com.google.common.collect.ImmutableList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

/**
 * Command for leaving a chat channel.
 * 
 * @author Jikoo
 */
public class ChatLeaveCommand extends EasterlynCommand {

	private final Users users;
	private final ChannelManager manager;

	public ChatLeaveCommand(Easterlyn plugin) {
		super(plugin, "leave");
		setAliases("quit");
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
		if (manager.getChannel(args[0]) instanceof RegionChannel) {
			sender.sendMessage(getLang().getValue("chat.error.globalQuit"));
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
