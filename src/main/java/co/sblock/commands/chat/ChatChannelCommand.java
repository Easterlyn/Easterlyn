package co.sblock.commands.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.Chat;
import co.sblock.chat.Language;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.chat.channel.NormalChannel;
import co.sblock.chat.channel.RegionChannel;
import co.sblock.commands.SblockAsynchronousCommand;
import co.sblock.users.User;
import co.sblock.users.Users;

import com.google.common.collect.ImmutableList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.md_5.bungee.api.ChatColor;

/**
 * Command for checking or manipulating channel data.
 * 
 * @author Jikoo
 */
public class ChatChannelCommand extends SblockAsynchronousCommand {

	private final Users users;
	private final ChannelManager manager;
	private final String[] defaultArgs = new String[] {"info", "list", "listeners", "listening", "new"};
	private final String[] modArgs = new String[] {"approve", "ban", "deapprove", "kick"};
	// TODO may want to move this to lang
	private final String modHelp = Language.getColor("emphasis.neutral") + "Channel moderation commands:\n"
			+ Language.getColor("command") + "/channel kick <user>"
			+ Language.getColor("neutral") + ": Kick a user from the channel\n"
			+ Language.getColor("command") + "/channel ban <user>"
			+ Language.getColor("neutral") + ": Ban a user from the channel\n"
			+ Language.getColor("command") + "/channel (de)approve <user>"
			+ Language.getColor("neutral") + ": Manage allowed users.";
	private final String[] ownerArgs = new String[] {"mod", "unban", "disband"};
	private final String ownerHelp = Language.getColor("emphasis.neutral") + "Channel owner commands:\n"
			+ Language.getColor("command") + "/channel mod <add|remove> <user>"
			+ Language.getColor("neutral") + ": Add/remove a channel mod\n"
			+ Language.getColor("command") + "/channel unban <user>"
			+ Language.getColor("neutral") + ": (Un)bans a user from the channel\n"
			+ Language.getColor("command") + "/channel disband"
			+ Language.getColor("neutral") + ": Delete the channel!";

	public ChatChannelCommand(Sblock plugin) {
		super(plugin, "channel");
		this.users = plugin.getModule(Users.class);
		this.manager = plugin.getModule(Chat.class).getChannelManager();
		setDescription("Check or manipulate channel data.");
		setUsage(Language.getColor("emphasis.neutral") + "Channel information/manipulation\n"
				+ Language.getColor("command") + "/channel listeners"
				+ Language.getColor("neutral") + ": List people in the channel.\n"
				+ Language.getColor("command") + "/channel info"
				+ Language.getColor("neutral") + ": Shows channel type, creator, etc.\n"
				+ Language.getColor("command") + "/channel listening"
				+ Language.getColor("neutral") + ": List channels you're in.\n"
				+ Language.getColor("command") + "/channel list"
				+ Language.getColor("neutral") + ": List all channels.\n"
				+ Language.getColor("command") + "/channel new <name> <access> <type>"
				+ Language.getColor("neutral") + ": Create a new channel.");

		this.addExtraPermission("list.private", "helper");
		this.addExtraPermission("new.anyname", "denizen");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}

		User user = users.getUser(((Player) sender).getUniqueId());
		Channel current = user.getCurrentChannel();

		if (args.length == 0) {
			sender.sendMessage(this.getUsage());
			if (current == null || !current.isModerator(user)) {
				return true;
			}
			sender.sendMessage(modHelp);
			if (current.isOwner(user)) {
				sender.sendMessage(ownerHelp);
			}
			return true;
		}

		StringBuilder sb;
		args[0] = args[0].toLowerCase();

		switch (args[0]) {
		case "listeners":
			if (current == null) {
				user.sendMessage(getLang().getValue("chat.error.noCurrentChannel"));
				return true;
			}
			sb = new StringBuilder().append(Language.getColor("neutral"));
			sb.append("Channel members: ");
			for (UUID userID : current.getListening()) {
				User u = users.getUser(userID);
				if (current.equals(u.getCurrentChannel())) {
					sb.append(Language.getColor("player.neutral"));
				} else {
					sb.append(Language.getColor("neutral"));
				}
				sb.append(u.getPlayerName()).append(' ');
			}
			user.sendMessage(sb.toString());
			return true;
		case "info":
			if (current == null) {
				user.sendMessage(getLang().getValue("chat.error.noCurrentChannel"));
				return true;
			}
			user.sendMessage(current.toString());
			return true;
		case "listening":
			sb = new StringBuilder().append(Language.getColor("neutral")).append("Currently pestering: ");
			// CHAT don't show users ignoring
			for (String s : user.getListening()) {
				sb.append(s).append(' ');
			}
			user.sendMessage(sb.toString());
			return true;
		case "list":
			sb = new StringBuilder();
			sb.append(Language.getColor("neutral")).append("All channels: ");
			for (Channel channel : manager.getChannelList().values()) {
				if (channel.isBanned(user)) {
					continue;
				}
				ChatColor color;
				if (channel.equals(current)) {
					color = Language.getColor("emphasis.good");
				} else if (user.isListening(channel)) {
					color = Language.getColor("good");
				} else if (channel.getAccess() == AccessLevel.PUBLIC) {
					color = Language.getColor("neutral");
				} else if (channel.isApproved(user)) {
					color = Language.getColor("emphasis.neutral");
				} else if (sender.hasPermission("sblock.command.channel.list.private")) {
					color = Language.getColor("bad");
				} else {
					continue;
				}
				sb.append(color).append(channel.getName()).append(' ');
			}
			user.sendMessage(sb.toString());
			return true;
		case "new":
			if (args.length != 4) {
				user.sendMessage(Language.getColor("command") + "/channel new <name> <access> <type>" + Language.getColor("neutral")
						+ ": Create a new channel.\nAccess must be either PUBLIC or PRIVATE\n"
						+ "Type must be NORMAL, NICK, or RP\nEx: /channel new #example PUBLIC NICK");
				return true;
			}
			if (manager.getChannel(args[1]) != null) {
				user.sendMessage(Language.getColor("bad") + "A channel by that name already exists!");
				return true;
			}
			if (!user.getPlayer().hasPermission("sblock.command.channel.new.anyname")) {
				for (char c : args[1].substring(1).toCharArray()) {
					if (c < '0' || c > '9' && c < 'A' || c > 'Z' && c < 'a' || c > 'z') {
						user.sendMessage(Language.getColor("bad") + "Channel names must start with # and can only contain A-Z, a-z, or 0-9!");
						return true;
					}
				}
			}
			if (args[1].length() > 16 || args[1].charAt(0) != '#' && !user.getPlayer().hasPermission("sblock.command.channel.new.anyname")) {
				user.sendMessage(Language.getColor("bad") + "Channel names must start with '#' and cannot exceed 16 characters!");
			} else if (ChannelType.getType(args[3]) == null) {
				user.sendMessage(Language.getColor("emphasis.bad") + args[3] + Language.getColor("bad")
						+ " is not a valid channel type!\nValid types: NORMAL, RP, NICK.");
			} else if (AccessLevel.getAccessLevel(args[2]) == null) {
				user.sendMessage(Language.getColor("emphasis.bad") + args[2] + Language.getColor("bad")
						+ " is not a valid access level!\nValid levels: PUBLIC, PRIVATE");
			} else {
				manager.createNewChannel(args[1], AccessLevel.getAccessLevel(args[2]),
						user.getUUID(), ChannelType.getType(args[3]));
				current = manager.getChannel(args[1]);
				user.setCurrentChannel(current);
			}
			return true;
		default:
			break;
		}

		if (current == null) {
			sender.sendMessage(getLang().getValue("chat.error.noCurrentChannel"));
			return true;
		}

		if (!current.isModerator(user)) {
			return false;
		}

		if (current instanceof RegionChannel) {
			sender.sendMessage(Language.getColor("bad") + "Region channels do not support kicks, bans, or approval. Moderators are permissions-based.");
			return false;
		}

		NormalChannel normal = (NormalChannel) current;

		UUID target;
		switch (args[0]) {
		case "approve":
		case "invite":
			if (args.length == 1) {
				sender.sendMessage(Language.getColor("command") + "/channel approve <user>" + Language.getColor("neutral") + ": Approve a user for this channel.");
				return true;
			}
			target = getUniqueId(args[1]);
			if (target == null) {
				sender.sendMessage(Language.getColor("bad") + "Unknown player. Check your spelling!");
				return true;
			}
			normal.approveUser(user, target);
			return true;
		case "ban":
			if (args.length == 1) {
				sender.sendMessage(Language.getColor("command") + "/channel ban <user>" + Language.getColor("neutral") + ": Ban a user from the channel");
				return true;
			}
			target = getUniqueId(args[1]);
			if (target == null) {
				sender.sendMessage(Language.getColor("bad") + "Unknown player. Check your spelling!");
				return true;
			}
			normal.banUser(user, target);
			return true;
		case "deapprove":
			if (args.length == 1) {
				sender.sendMessage(Language.getColor("command") + "/channel deapprove <user>" + Language.getColor("neutral") + ": De-approve a user from this channel.");
				return true;
			}
			target = getUniqueId(args[1]);
			if (target == null) {
				sender.sendMessage(Language.getColor("bad") + "Unknown player. Check your spelling!");
				return true;
			}
			normal.disapproveUser(user, target);
			return true;
		case "kick":
			if (args.length == 1) {
				sender.sendMessage(Language.getColor("command") + "/channel kick <user>" + Language.getColor("neutral") + ": Kick a user from the channel");
				return true;
			}
			target = getUniqueId(args[1]);
			if (target == null) {
				sender.sendMessage(Language.getColor("bad") + "Unknown player. Check your spelling!");
				return true;
			}
			normal.kickUser(user, target);
			return true;
		default:
			break;
		}

		if (!current.isOwner(user)) {
			sender.sendMessage(this.getUsage());
			sender.sendMessage(this.modHelp);
			return true;
		}

		switch (args[0]) {
		case "mod":
			if (args.length < 3) {
				sender.sendMessage(Language.getColor("command") + "/channel mod <add|remove> <user>" + Language.getColor("neutral") + ": Add or remove a channel mod");
				return true;
			}
			target = getUniqueId(args[2]);
			if (target == null) {
				sender.sendMessage(Language.getColor("bad") + "Unknown player. Check your spelling!");
				return true;
			}
			if (args[1].equalsIgnoreCase("add")) {
				normal.addMod(user, target);
				return true;
			} else if (args[1].equalsIgnoreCase("remove")) {
				normal.removeMod(user, target);
				return true;
			} else {
				sender.sendMessage(Language.getColor("command") + "/channel mod <add|remove> <user>" + Language.getColor("neutral") + ": Add or remove a channel mod");
				return true;
			}
		case "unban":
			if (args.length < 2) {
				sender.sendMessage(Language.getColor("command") + "/channel unban <user>" + Language.getColor("neutral") + ": Unban a user from the channel");
				return true;
			}
			target = getUniqueId(args[1]);
			if (target == null) {
				sender.sendMessage(Language.getColor("bad") + "Unknown player. Check your spelling!");
				return true;
			}
			normal.unbanUser(user, target);
			return true;
		case "disband":
			normal.disband(user);
			return true;
		default:
			break;
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
		User user = users.getUser(((Player) sender).getUniqueId());
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
