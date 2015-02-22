package co.sblock.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.channel.Channel;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

/**
 * SblockCommand for most manipulation of chat features.
 * 
 * @author Jikoo
 */
public class SblockChatCommand extends SblockCommand {

	private final String[] primaryArgs;
	private final String[] channelMod;
	private final String[] channelOwner;

	public SblockChatCommand() {
		super("sc");
		this.setDescription("SblockChat's main command");
		this.setUsage("/sc");
		setAliases("chat");
		primaryArgs = new String[] {"list", "listall", "new", "nick", "suppress"};
		channelMod = new String[] {"info", "getlisteners", "kick", "ban", "approve", "deapprove"};
		channelOwner = new String[] {"mod", "unban", "disband"};
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args == null || args.length == 0) {
			sender.sendMessage(ChatMsgs.helpDefault());
			return true;
		}

		OfflineUser user = Users.getGuaranteedUser(((Player) sender).getUniqueId());
		sender.sendMessage(ChatColor.RED + "/sc is being phased out! Please check /chat for the new commands!");

		args[0] = args[0].toLowerCase();
		if (args[0].equals("c")) {
			args[0] = "join";
			Bukkit.dispatchCommand(sender, StringUtils.join(args, ' '));
			return true;
		} else if (args[0].equals("l") || args[0].equals("listen")) {
			args[0] = "listen";
			Bukkit.dispatchCommand(sender, StringUtils.join(args, ' '));
			return true;
		} else if (args[0].equals("leave")) {
			args[0] = "leave";
			Bukkit.dispatchCommand(sender, StringUtils.join(args, ' '));
			return true;
		} else if (args[0].equals("list") || args[0].equals("listall") || args[0].equals("new")) {
			args[0] = "channel";
			Bukkit.dispatchCommand(sender, StringUtils.join(args, ' '));
			return true;
		} else if (args[0].equals("nick")) {
			Bukkit.dispatchCommand(sender, StringUtils.join(args, ' '));
			return true;
		} else if (args[0].equals("suppress")) {
			Bukkit.dispatchCommand(sender, "suppress");
			return true;
		} else if (args[0].equals("channel")) {
			return scChannel(user, args);
		} else {
			sender.sendMessage(ChatMsgs.helpDefault());
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || args.length == 0) {
			return ImmutableList.of();
		}
		args[0] = args[0].toLowerCase();
		ArrayList<String> matches = new ArrayList<>();
		OfflineUser user = Users.getGuaranteedUser(((Player) sender).getUniqueId());
		if (args.length == 1) {
			for (String subcommand : primaryArgs) {
				if (subcommand.startsWith(args[0])) {
					matches.add(subcommand);
				}
			}
			String string = "channel";
			if (user.getCurrentChannel() != null && user.getCurrentChannel().isModerator(user) && string.startsWith(args[0])) {
				matches.add(string);
			}
			return matches;
		}
		if (args[0].equals("channel")) {
			if (args.length > 4) {
				return ImmutableList.of();
			}
			args[1] = args[1].toLowerCase();
			if (args.length == 2) {
				for (String argument : channelMod) {
					if (argument.startsWith(args[1])) {
						matches.add(argument);
					}
				}
				if (!user.getCurrentChannel().isOwner(user)) {
					return matches;
				}
				for (String argument : channelOwner) {
					if (argument.startsWith(args[1])) {
						matches.add(argument);
					}
				}
				return matches;
			}
			if (args.length == 3) {
				for (int i = 2; i < channelMod.length; i++) {
					if (channelMod[i].equals(args[1])) {
						return super.tabComplete(sender, alias, args);
					}
				}
				if (!user.getCurrentChannel().isOwner(user)) {
					return ImmutableList.of();
				}
				if (args[1].equals("mod")) {
					String argument = "add";
					if (argument.startsWith(args[2])) {
						matches.add(argument);
					}
					argument = "remove";
					if (argument.startsWith(args[2])) {
						matches.add(argument);
					}
					return matches;
				}
				if (args[1].equals("unban")) {
					return super.tabComplete(sender, alias, args);
				}
				return ImmutableList.of();
			}
			args[2] = args[2].toLowerCase();
			if (args.length == 4 && args[1].equals("mod")
					&& (args[2].equals("add") || args[2].equals("remove"))
					&& user.getCurrentChannel().isOwner(user)) {
				return super.tabComplete(sender, alias, args);
			}
			return ImmutableList.of();
		}
		return ImmutableList.of();
	}

	private boolean scChannel(OfflineUser user, String[] args) {
		Channel c = user.getCurrentChannel();
		if (args.length == 2 && args[1].equalsIgnoreCase("info")) {
			user.sendMessage(c.toString());
			return true;
		}
		if (!c.isModerator(user)) {
			user.sendMessage(ChatMsgs.onChannelCommandFail(c.getName()));
			return true;
		}
		if (args.length == 1) {
			user.sendMessage(ChatMsgs.helpChannelMod());
			if (c.isOwner(user)) {
				user.sendMessage(ChatMsgs.helpChannelOwner());
			}
			return true;
		} else if (args.length >= 2 && args[1].equalsIgnoreCase("getlisteners")) {
			StringBuilder sb = new StringBuilder().append(ChatColor.YELLOW);
			sb.append("Channel members: ");
			for (UUID userID : c.getListening()) {
				OfflineUser u = Users.getGuaranteedUser(userID);
				if (c.equals(u.getCurrentChannel())) {
					sb.append(ChatColor.GREEN);
				} else {
					sb.append(ChatColor.YELLOW);
				}
				sb.append(u.getPlayerName()).append(' ');
			}
			user.sendMessage(sb.toString());
			return true;
		} else if (args.length >= 3) {
			if (args[1].equalsIgnoreCase("kick")) {
				c.kickUser(user, Bukkit.getPlayer(args[2]).getUniqueId());
				return true;
			} else if (args[1].equalsIgnoreCase("ban")) {
				c.banUser(user, Bukkit.getPlayer(args[2]).getUniqueId());
				return true;
			} else if (args[1].equalsIgnoreCase("approve")) {
				c.approveUser(user, Bukkit.getPlayer(args[2]).getUniqueId());
				return true;
			} else if (args[1].equalsIgnoreCase("deapprove")) {
				c.disapproveUser(user, Bukkit.getPlayer(args[2]).getUniqueId());
				return true;
			}
		}
		if (c.isOwner(user)) {
			if (args.length >= 4 && args[1].equalsIgnoreCase("mod")) {
				if (args[2].equalsIgnoreCase("add")) {
					c.addMod(user, Bukkit.getPlayer(args[3]).getUniqueId());
					return true;
				} else if (args[2].equalsIgnoreCase("remove")) {
					c.removeMod(user, Bukkit.getPlayer(args[3]).getUniqueId());
					return true;
				} else {
					user.sendMessage(ChatMsgs.helpChannelMod());
					if (c.isOwner(user)) {
						user.sendMessage(ChatMsgs.helpChannelOwner());
					}
					return true;
				}
			} else if (args.length >= 3 && args[1].equalsIgnoreCase("unban")) {
				ChannelManager.getChannelManager().getChannel(c.getName())
						.unbanUser(user, Bukkit.getPlayer(args[2]).getUniqueId());
				return true;
			} else if (args.length >= 2 && args[1].equalsIgnoreCase("disband")) {
				c.disband(user);
				return true;
			}
		}
		user.sendMessage(ChatMsgs.helpChannelMod());
		if (c.isOwner(user)) {
			user.sendMessage(ChatMsgs.helpChannelOwner());
		}
		return true;
	}
}
