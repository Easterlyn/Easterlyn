package co.sblock.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

/**
 * Command for checking or manipulating channel data.
 * 
 * @author Jikoo
 */
public class ChatChannelCommand extends SblockCommand {

	private final String[] defaultArgs = new String[] {"getlisteners", "info", "list", "listall", "new"};

	public ChatChannelCommand() {
		super("channel");
		setDescription("Check or manipulate channel data.");
		setUsage(ChatColor.DARK_AQUA + "Channel information/manipulation\n"
				+ ChatColor.AQUA + "/channel getlisteners"
				+ ChatColor.YELLOW + ": List people in the channel."
				+ ChatColor.AQUA + "/channel info"
				+ ChatColor.YELLOW + ": Shows channel type, creator, etc."
				+ ChatColor.AQUA + "/channel list"
				+ ChatColor.YELLOW + ": List channels you're in."
				+ ChatColor.AQUA + "/channel listall"
				+ ChatColor.YELLOW + ": List all channels.");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args.length == 0) {
			sender.sendMessage(this.getUsage());
			return true;
		}

		OfflineUser user = Users.getGuaranteedUser(((Player) sender).getUniqueId());
		Channel channel = user.getCurrentChannel();
		StringBuilder sb;
		args[0] = args[0].toLowerCase();

		switch (args[0]) {
		case "getlisteners":
			if (channel == null) {
				user.sendMessage(ChatMsgs.errorNoCurrent());
				return true;
			}
			sb = new StringBuilder().append(ChatColor.YELLOW);
			sb.append("Channel members: ");
			for (UUID userID : channel.getListening()) {
				OfflineUser u = Users.getGuaranteedUser(userID);
				if (channel.equals(u.getCurrentChannel())) {
					sb.append(ChatColor.GREEN);
				} else {
					sb.append(ChatColor.YELLOW);
				}
				sb.append(u.getPlayerName()).append(' ');
			}
			user.sendMessage(sb.toString());
			return true;
		case "info":
			if (channel == null) {
				user.sendMessage(ChatMsgs.errorNoCurrent());
				return true;
			}
			user.sendMessage(channel.toString());
			return true;
		case "list":
			sb = new StringBuilder().append(ChatColor.YELLOW).append("Currently pestering: ");
			for (String s : user.getListening()) {
				sb.append(s).append(' ');
			}
			user.sendMessage(sb.toString());
			return true;
		case "listall":
			sb = new StringBuilder();
			sb.append(ChatColor.YELLOW).append("All channels: ");
			for (Channel c : ChannelManager.getChannelManager().getChannelList().values()) {
				ChatColor cc;
				if (user.isListening(c)) {
					cc = ChatColor.YELLOW;
				} else if (c.getAccess() == AccessLevel.PUBLIC) {
					cc = ChatColor.GREEN;
				} else {
					cc = ChatColor.RED;
				}
				sb.append(cc).append(c.getName()).append(' ');
			}
			user.sendMessage(sb.toString());
			return true;
		case "new":
			if (args.length != 4) {
				user.sendMessage(ChatColor.AQUA + "/channel new <name> <access> <type>" + ChatColor.YELLOW
						+ ": Create a new channel.\nAccess must be either PUBLIC or PRIVATE\n"
						+ "Type must be NORMAL, NICK, or RP");
				return true;
			}
			if (ChannelManager.getChannelManager().isValidChannel(args[1])) {
				user.sendMessage(ChatColor.RED + "A channel by that name already exists!");
			}
			for (char c : args[1].substring(1).toCharArray()) {
				if (!Character.isLetterOrDigit(c)) {
					user.sendMessage(ChatColor.RED + "Channel names must start with '#' and cannot exceed 16 characters!");
					return true;
				}
			}
			if (args[1].length() > 16 || args[1].charAt(0) != '#' && !user.getPlayer().hasPermission("sblock.denizen")) {
				user.sendMessage(ChatColor.RED + "Channel names must start with '#' and cannot exceed 16 characters!");
			} else if (ChannelType.getType(args[3]) == null) {
				user.sendMessage(ChatColor.GOLD + args[3] + ChatColor.RED
						+ " is not a valid channel type!\nValid types: NORMAL, RP, NICK.");
			} else if (AccessLevel.getAccessLevel(args[2]) == null) {
				user.sendMessage(ChatColor.GOLD + args[2] + ChatColor.RED
						+ " is not a valid access level!\nValid levels: PUBLIC, PRIVATE");
			} else {
				ChannelManager.getChannelManager().createNewChannel(args[1],
						AccessLevel.getAccessLevel(args[2]), user.getUUID(), ChannelType.getType(args[3]));
				Channel c = ChannelManager.getChannelManager().getChannel(args[1]);
				user.setCurrentChannel(c);
			}
			return true;
		}
		sender.sendMessage(this.getUsage());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || args.length > 4) {
			return ImmutableList.of();
		}
		args[0] = args[0].toLowerCase();
		ArrayList<String> matches = new ArrayList<>();
		OfflineUser user = Users.getGuaranteedUser(((Player) sender).getUniqueId());
		if (args.length == 1) {
			for (String subcommand : defaultArgs) {
				if (subcommand.startsWith(args[0])) {
					matches.add(subcommand);
				}
			}
			if (user.getCurrentChannel() == null || !user.getCurrentChannel().isModerator(user)) {
				return matches;
			}
			return matches;
		}
		if (args[0].equals("new")) {
			if (args.length == 2) {
				matches.add("#<channelname>");
				return matches;
			}
			if (args.length == 3) {
				args[2] = args[2].toUpperCase();
				for (AccessLevel access : AccessLevel.values()) {
					if (access.name().startsWith(args[2])) {
						matches.add(access.name());
					}
				}
				return matches;
			}
			if (args.length == 4) {
				args[3] = args[3].toUpperCase();
				for (String type : new String[] {"NORMAL", "NICK", "RP"}) {
					if (type.startsWith(args[3])) {
						matches.add(type);
					}
				}
				return matches;
			}
			return ImmutableList.of();
		}
		Channel current = user.getCurrentChannel();
		if (current == null || !current.isModerator(user)) {
			return ImmutableList.of();
		}

		// TODO TODO TODO
		return ImmutableList.of();
	}
}
