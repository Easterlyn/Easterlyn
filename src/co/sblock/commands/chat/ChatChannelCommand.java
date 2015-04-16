package co.sblock.commands.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.commands.SblockAsynchronousCommand;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

/**
 * Command for checking or manipulating channel data.
 * 
 * @author Jikoo
 */
public class ChatChannelCommand extends SblockAsynchronousCommand {

	private final String[] defaultArgs = new String[] {"getlisteners", "info", "list", "listall", "new"};
	private final String[] modArgs = new String[] {"approve", "ban", "deapprove", "kick"};
	private final String modHelp = ChatColor.DARK_AQUA + "Channel moderation commands:\n"
			+ ChatColor.AQUA + "/channel kick <user>"
			+ ChatColor.YELLOW + ": Kick a user from the channel\n"
			+ ChatColor.AQUA + "/channel ban <user>"
			+ ChatColor.YELLOW + ": Ban a user from the channel\n"
			+ ChatColor.AQUA + "/channel (de)approve <user>"
			+ ChatColor.YELLOW + ": Manage allowed users.";
	private final String[] ownerArgs = new String[] {"mod", "unban", "disband"};
	private final String ownerHelp = ChatColor.DARK_AQUA + "Channel owner commands:\n"
			+ ChatColor.AQUA + "/channel mod <add|remove> <user>"
			+ ChatColor.YELLOW + ": Add or remove a channel mod\n"
			+ ChatColor.AQUA + "/channel unban <user>"
			+ ChatColor.YELLOW + ": (Un)bans a user from the channel\n"
			+ ChatColor.AQUA + "/channel disband"
			+ ChatColor.YELLOW + ": Delete the channel!";

	public ChatChannelCommand() {
		super("channel");
		setDescription("Check or manipulate channel data.");
		setUsage(ChatColor.DARK_AQUA + "Channel information/manipulation\n"
				+ ChatColor.AQUA + "/channel getlisteners"
				+ ChatColor.YELLOW + ": List people in the channel.\n"
				+ ChatColor.AQUA + "/channel info"
				+ ChatColor.YELLOW + ": Shows channel type, creator, etc.\n"
				+ ChatColor.AQUA + "/channel list"
				+ ChatColor.YELLOW + ": List channels you're in.\n"
				+ ChatColor.AQUA + "/channel listall"
				+ ChatColor.YELLOW + ": List all channels.\n"
				+ ChatColor.AQUA + "/channel new <name> <access> <type>"
				+ ChatColor.YELLOW + ": Create a new channel.");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}

		OfflineUser user = Users.getGuaranteedUser(((Player) sender).getUniqueId());
		Channel channel = user.getCurrentChannel();

		if (args.length == 0) {
			sender.sendMessage(this.getUsage());
			if (channel == null || !channel.isModerator(user)) {
				return true;
			}
			sender.sendMessage(modHelp);
			if (channel.isOwner(user)) {
				sender.sendMessage(ownerHelp);
			}
			return true;
		}

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
			if (ChannelManager.getChannelManager().getChannel(args[1]) != null) {
				user.sendMessage(ChatColor.RED + "A channel by that name already exists!");
				return true;
			}
			for (char c : args[1].substring(1).toCharArray()) {
				if (!Character.isLetterOrDigit(c)) {
					user.sendMessage(ChatColor.RED + "Channel names must be alphanumeric!");
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
				channel = ChannelManager.getChannelManager().getChannel(args[1]);
				user.setCurrentChannel(channel);
			}
			return true;
		}

		if (!channel.isModerator(user)) {
			sender.sendMessage(this.getUsage());
			return true;
		}

		switch (args[0]) {
		case "approve":
			if (args.length == 1) {
				sender.sendMessage(ChatColor.AQUA + "/channel approve <user>" + ChatColor.YELLOW + ": Approve a user for this channel.");
				return true;
			}
			channel.approveUser(user, getUniqueId(args[1]));
			return true;
		case "ban":
			if (args.length == 1) {
				sender.sendMessage(ChatColor.AQUA + "/channel ban <user>" + ChatColor.YELLOW + ": Ban a user from the channel");
				return true;
			}
			channel.banUser(user, getUniqueId(args[1]));
			return true;
		case "deapprove":
			if (args.length == 1) {
				sender.sendMessage(ChatColor.AQUA + "/channel deapprove <user>" + ChatColor.YELLOW + ": De-approve a user from this channel.");
				return true;
			}
			channel.disapproveUser(user, getUniqueId(args[1]));
			return true;
		case "kick":
			if (args.length == 1) {
				sender.sendMessage(ChatColor.AQUA + "/channel kick <user>" + ChatColor.YELLOW + ": Kick a user from the channel");
				return true;
			}
			channel.kickUser(user, getUniqueId(args[1]));
			return true;
		}

		if (!channel.isOwner(user)) {
			sender.sendMessage(this.getUsage());
			sender.sendMessage(this.modHelp);
			return true;
		}

		switch (args[0]) {
		case "mod":
			if (args.length < 3) {
				sender.sendMessage(ChatColor.AQUA + "/channel mod <add|remove> <user>" + ChatColor.YELLOW + ": Add or remove a channel mod");
				return true;
			}
			if (args[1].equalsIgnoreCase("add")) {
				channel.addMod(user, getUniqueId(args[2]));
				return true;
			} else if (args[1].equalsIgnoreCase("remove")) {
				channel.removeMod(user, getUniqueId(args[2]));
				return true;
			} else {
				sender.sendMessage(ChatColor.AQUA + "/channel mod <add|remove> <user>" + ChatColor.YELLOW + ": Add or remove a channel mod");
				return true;
			}
		case "unban":
			if (args.length < 2) {
				sender.sendMessage(ChatColor.AQUA + "/channel unban <user>" + ChatColor.YELLOW + ": Unban a user from the channel");
				return true;
			}
			channel.unbanUser(user, getUniqueId(args[1]));
			return true;
		case "disband":
			channel.disband(user);
			return true;
		}

		sender.sendMessage(this.getUsage());
		sender.sendMessage(this.modHelp);
			sender.sendMessage(this.ownerHelp);
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
			for (String subcommand : modArgs) {
				if (subcommand.startsWith(args[0])) {
					matches.add(subcommand);
				}
			}
			if (!user.getCurrentChannel().isOwner(user)) {
				return matches;
			}
			for (String subcommand : ownerArgs) {
				if (subcommand.startsWith(args[0])) {
					matches.add(subcommand);
				}
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

		if (args[0].equals("kick") || args[0].equals("ban") || args[0].equals("approve") || args[0].equals("deapprove")) {
			if (args.length > 2) {
				return ImmutableList.of();
			}
			return super.tabComplete(sender, alias, args);
		}

		if (!current.isOwner(user)) {
			return ImmutableList.of();
		}

		if (args[0].equals("mod")) {
			if (args.length == 2) {
				if (StringUtil.startsWithIgnoreCase(args[1], "add")) {
					matches.add("add");
				}
				if (StringUtil.startsWithIgnoreCase(args[1], "remove")) {
					matches.add("remove");
				}
				return matches;
			}
			if (args.length == 3) {
				return super.tabComplete(sender, alias, args);
			}
			return ImmutableList.of();
		}

		if (args.length == 2 && args[0].equals("unban")) {
			return super.tabComplete(sender, alias, args);
		}

		return ImmutableList.of();
	}
}
