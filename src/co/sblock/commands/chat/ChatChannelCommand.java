package co.sblock.commands.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.Color;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.chat.channel.NormalChannel;
import co.sblock.chat.channel.RegionChannel;
import co.sblock.commands.SblockAsynchronousCommand;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

import net.md_5.bungee.api.ChatColor;

/**
 * Command for checking or manipulating channel data.
 * 
 * @author Jikoo
 */
public class ChatChannelCommand extends SblockAsynchronousCommand {

	private final String[] defaultArgs = new String[] {"info", "list", "listeners", "listening", "new"};
	private final String[] modArgs = new String[] {"approve", "ban", "deapprove", "kick"};
	private final String modHelp = Color.GOOD_EMPHASIS + "Channel moderation commands:\n"
			+ Color.COMMAND + "/channel kick <user>"
			+ Color.GOOD + ": Kick a user from the channel\n"
			+ Color.COMMAND + "/channel ban <user>"
			+ Color.GOOD + ": Ban a user from the channel\n"
			+ Color.COMMAND + "/channel (de)approve <user>"
			+ Color.GOOD + ": Manage allowed users.";
	private final String[] ownerArgs = new String[] {"mod", "unban", "disband"};
	private final String ownerHelp = Color.GOOD_EMPHASIS + "Channel owner commands:\n"
			+ Color.COMMAND + "/channel mod <add|remove> <user>"
			+ Color.GOOD + ": Add/remove a channel mod\n"
			+ Color.COMMAND + "/channel unban <user>"
			+ Color.GOOD + ": (Un)bans a user from the channel\n"
			+ Color.COMMAND + "/channel disband"
			+ Color.GOOD + ": Delete the channel!";

	public ChatChannelCommand() {
		super("channel");
		setDescription("Check or manipulate channel data.");
		setUsage(Color.GOOD_EMPHASIS + "Channel information/manipulation\n"
				+ Color.COMMAND + "/channel listeners"
				+ Color.GOOD + ": List people in the channel.\n"
				+ Color.COMMAND + "/channel info"
				+ Color.GOOD + ": Shows channel type, creator, etc.\n"
				+ Color.COMMAND + "/channel listening"
				+ Color.GOOD + ": List channels you're in.\n"
				+ Color.COMMAND + "/channel list"
				+ Color.GOOD + ": List all channels.\n"
				+ Color.COMMAND + "/channel new <name> <access> <type>"
				+ Color.GOOD + ": Create a new channel.");
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
		case "listeners":
			if (channel == null) {
				user.sendMessage(ChatMsgs.errorCurrentChannelNull());
				return true;
			}
			sb = new StringBuilder().append(Color.GOOD);
			sb.append("Channel members: ");
			for (UUID userID : channel.getListening()) {
				OfflineUser u = Users.getGuaranteedUser(userID);
				if (channel.equals(u.getCurrentChannel())) {
					sb.append(Color.GOOD_PLAYER);
				} else {
					sb.append(Color.GOOD);
				}
				sb.append(u.getPlayerName()).append(' ');
			}
			user.sendMessage(sb.toString());
			return true;
		case "info":
			if (channel == null) {
				user.sendMessage(ChatMsgs.errorCurrentChannelNull());
				return true;
			}
			user.sendMessage(channel.toString());
			return true;
		case "listening":
			sb = new StringBuilder().append(Color.GOOD).append("Currently pestering: ");
			for (String s : user.getListening()) {
				sb.append(s).append(' ');
			}
			user.sendMessage(sb.toString());
			return true;
		case "list":
			sb = new StringBuilder();
			sb.append(Color.GOOD).append("All channels: ");
			for (Channel c : ChannelManager.getChannelManager().getChannelList().values()) {
				ChatColor cc;
				if (user.isListening(c)) {
					cc = ChatColor.GREEN;
				} else if (c.getAccess() == AccessLevel.PUBLIC) {
					cc = ChatColor.YELLOW;
				} else {
					continue;
				}
				sb.append(cc).append(c.getName()).append(' ');
			}
			user.sendMessage(sb.toString());
			return true;
		case "new":
			if (args.length != 4) {
				user.sendMessage(Color.COMMAND + "/channel new <name> <access> <type>" + Color.GOOD
						+ ": Create a new channel.\nAccess must be either PUBLIC or PRIVATE\n"
						+ "Type must be NORMAL, NICK, or RP");
				return true;
			}
			if (ChannelManager.getChannelManager().getChannel(args[1]) != null) {
				user.sendMessage(Color.BAD + "A channel by that name already exists!");
				return true;
			}
			for (char c : args[1].substring(1).toCharArray()) {
				if (c < '0' || c > '9' && c < 'A' || c > 'Z' && c < 'a' || c > 'z') {
					user.sendMessage(Color.BAD + "Channel names must start with # and can only contain A-Z, a-z, or 0-9!");
					return true;
				}
			}
			if (args[1].length() > 16 || args[1].charAt(0) != '#' && !user.getPlayer().hasPermission("sblock.denizen")) {
				user.sendMessage(Color.BAD + "Channel names must start with '#' and cannot exceed 16 characters!");
			} else if (ChannelType.getType(args[3]) == null) {
				user.sendMessage(Color.BAD_EMPHASIS + args[3] + Color.BAD
						+ " is not a valid channel type!\nValid types: NORMAL, RP, NICK.");
			} else if (AccessLevel.getAccessLevel(args[2]) == null) {
				user.sendMessage(Color.BAD_EMPHASIS + args[2] + Color.BAD
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
			return false;
		}

		if (channel instanceof RegionChannel) {
			sender.sendMessage(Color.BAD + "Region channels do not support kicks, bans, or approval. Moderators are permissions-based.");
			return false;
		}

		NormalChannel normal = (NormalChannel) channel;

		UUID target;
		switch (args[0]) {
		case "approve":
		case "invite":
			if (args.length == 1) {
				sender.sendMessage(Color.COMMAND + "/channel approve <user>" + Color.GOOD + ": Approve a user for this channel.");
				return true;
			}
			target = getUniqueId(args[1]);
			if (target == null) {
				sender.sendMessage(Color.BAD + "Unknown player. Check your spelling!");
				return true;
			}
			normal.approveUser(user, target);
			return true;
		case "ban":
			if (args.length == 1) {
				sender.sendMessage(Color.COMMAND + "/channel ban <user>" + Color.GOOD + ": Ban a user from the channel");
				return true;
			}
			target = getUniqueId(args[1]);
			if (target == null) {
				sender.sendMessage(Color.BAD + "Unknown player. Check your spelling!");
				return true;
			}
			normal.banUser(user, target);
			return true;
		case "deapprove":
			if (args.length == 1) {
				sender.sendMessage(Color.COMMAND + "/channel deapprove <user>" + Color.GOOD + ": De-approve a user from this channel.");
				return true;
			}
			target = getUniqueId(args[1]);
			if (target == null) {
				sender.sendMessage(Color.BAD + "Unknown player. Check your spelling!");
				return true;
			}
			normal.disapproveUser(user, target);
			return true;
		case "kick":
			if (args.length == 1) {
				sender.sendMessage(Color.COMMAND + "/channel kick <user>" + Color.GOOD + ": Kick a user from the channel");
				return true;
			}
			target = getUniqueId(args[1]);
			if (target == null) {
				sender.sendMessage(Color.BAD + "Unknown player. Check your spelling!");
				return true;
			}
			normal.kickUser(user, target);
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
				sender.sendMessage(Color.COMMAND + "/channel mod <add|remove> <user>" + Color.GOOD + ": Add or remove a channel mod");
				return true;
			}
			target = getUniqueId(args[2]);
			if (target == null) {
				sender.sendMessage(Color.BAD + "Unknown player. Check your spelling!");
				return true;
			}
			if (args[1].equalsIgnoreCase("add")) {
				normal.addMod(user, target);
				return true;
			} else if (args[1].equalsIgnoreCase("remove")) {
				normal.removeMod(user, target);
				return true;
			} else {
				sender.sendMessage(Color.COMMAND + "/channel mod <add|remove> <user>" + Color.GOOD + ": Add or remove a channel mod");
				return true;
			}
		case "unban":
			if (args.length < 2) {
				sender.sendMessage(Color.COMMAND + "/channel unban <user>" + Color.GOOD + ": Unban a user from the channel");
				return true;
			}
			target = getUniqueId(args[1]);
			if (target == null) {
				sender.sendMessage(Color.BAD + "Unknown player. Check your spelling!");
				return true;
			}
			normal.unbanUser(user, target);
			return true;
		case "disband":
			normal.disband(user);
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
