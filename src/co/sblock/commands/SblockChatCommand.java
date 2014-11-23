package co.sblock.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.CanonNicks;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.chat.channel.NickChannel;
import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.Log;

/**
 * SblockCommand for most manipulation of chat features.
 * 
 * @author Jikoo
 */
public class SblockChatCommand extends SblockCommand {

	private final String[] primaryArgs;
	private final String[] nickArgs;
	private final String[] channelTypes;

	public SblockChatCommand() {
		super("sc");
		this.setDescription("SblockChat's main command");
		this.setUsage("/sc");
		primaryArgs = new String[] {"c", "l", "listen", "leave", "list", "listall", "new", "nick", "suppress"};
		nickArgs = new String[] {"list", "set", "remove"};
		channelTypes = new String[] {"NORMAL", "NICK", "RP"};
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		User user = UserManager.getUser(((Player) sender).getUniqueId());
		if (args == null || args.length == 0) {
			sender.sendMessage(ChatMsgs.helpDefault());
			return true;
		}

		args[0] = args[0].toLowerCase();
		if (args[0].equals("c")) {
			return scC(user, args);
		} else if (args[0].equals("l") || args[0].equals("listen")) {
			return scL(user, args);
		} else if (args[0].equals("leave")) {
			return scLeave(user, args);
		} else if (args[0].equals("list")) {
			return scList(user, args);
		} else if (args[0].equals("listall")) {
			return scListAll(user, args);
		} else if (args[0].equals("new")) {
			return scNew(user, args);
		} else if (args[0].equals("nick")) {
			return scNick(user, args);
		} else if (args[0].equals("suppress")) {
			user.setSuppressing(!user.isSuppressing());
			user.sendMessage(ChatColor.GREEN + "Suppression toggled!");
			return true;
		} else if (args[0].equals("channel")) {
			return scChannel(user, args);
		} else if (args[0].equals("global")) {
			return scGlobal(user, args);
		} else {
			sender.sendMessage(ChatMsgs.helpDefault());
		}
		return true;
	}

	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || args.length == 0) {
			return ImmutableList.of();
		}
		args[0] = args[0].toLowerCase();
		ArrayList<String> matches = new ArrayList<>();
		User user = UserManager.getUser(((Player) sender).getUniqueId());
		if (args.length == 1) {
			for (String subcommand : primaryArgs) {
				if (subcommand.startsWith(args[0])) {
					matches.add(subcommand);
				}
			}
			String string = "channel";
			if (user.getCurrent().isModerator(user) && string.startsWith(args[0])) {
				matches.add(string);
			}
			return matches;
		}
		if (args[0].equals("c") || args[0].equals("l") || args[0].equals("listen")) {
			if (args.length == 2) {
				for (String channel : ChannelManager.getChannelManager().getChannelList().keySet()) {
					if (StringUtil.startsWithIgnoreCase(channel, args[1])) {
						matches.add(channel);
					}
				}
				return matches;
			}
			return ImmutableList.of();
		}
		if (args[0].equals("leave")) {
			if (args.length == 2) {
				for (String channel : user.getListening()) {
					if (StringUtil.startsWithIgnoreCase(channel, args[1])) {
						matches.add(channel);
					}
				}
				return matches;
			}
			return ImmutableList.of();
		}
		if (args[0].equals("nick")) {
			args[1] = args[1].toLowerCase();
			if (args.length == 2) {
				for (String subcommand : nickArgs) {
					if (subcommand.startsWith(args[1])) {
						matches.add(subcommand);
					}
				}
				return matches;
			}
			if (args.length == 3 && args[1].equals("set") && user.getCurrent().getType() == ChannelType.RP) {
				args[2] = args[2].toUpperCase();
				for (CanonNicks nick : CanonNicks.values()) {
					if (nick != CanonNicks.SERKITFEATURE && nick.name().startsWith(args[2])) {
						matches.add(nick.name());
					}
				}
				return matches;
			}
			return ImmutableList.of();
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
				for (String type : channelTypes) {
					if (type.startsWith(args[3])) {
						matches.add(type);
					}
				}
				return matches;
			}
			return ImmutableList.of();
		}
		if (args[0].equals("channel")) {
			if (!user.getCurrent().isModerator(user)) {
				matches.add("info");
				return matches;
			}
			// TODO finish channel
		}
		// TODO global? Maybe.
		return ImmutableList.of();
	}

	private boolean scC(User user, String[] args) {
		if (args.length == 1) {
			user.sendMessage(ChatMsgs.helpSCC());
			return true;
		}
		Channel c = ChannelManager.getChannelManager().getChannel(args[1]);
		if (c == null) {
			user.sendMessage(ChatMsgs.errorInvalidChannel(args[1]));
			return true;
		}
		if (c.getType() == ChannelType.REGION && !user.isListening(c)) {
			user.sendMessage(ChatMsgs.errorRegionChannelJoin());
			return true;
		}
		user.setCurrent(c);
		return true;
	}

	private boolean scL(User user, String[] args) {
		if (args.length == 1) {
			user.sendMessage(ChatMsgs.helpSCL());
			return true;
		}
		Channel c = ChannelManager.getChannelManager().getChannel(args[1]);
		if (c == null) {
			user.sendMessage(ChatMsgs.errorInvalidChannel(args[1]));
			return true;
		}
		if (c.getType() == ChannelType.REGION) {
			user.sendMessage(ChatMsgs.errorRegionChannelJoin());
			return true;
		}
		user.addListening(c);
		return true;
	}

	private boolean scLeave(User user, String[] args) {
		if (args.length == 1) {
			user.sendMessage(ChatMsgs.helpSCLeave());
			return true;
		}
		Channel c = ChannelManager.getChannelManager().getChannel(args[1]);
		if (c == null) {
			user.sendMessage(ChatMsgs.errorInvalidChannel(args[1]));
			user.removeListening(args[1]);
			return true;
		}
		if (c.getType() == ChannelType.REGION) {
			user.sendMessage(ChatMsgs.errorRegionChannelLeave());
			return true;
		}
		user.removeListening(args[1]);
		return true;
		
	}

	private boolean scList(User user, String[] args) {
		StringBuilder sb = new StringBuilder().append(ChatColor.YELLOW).append("Currently pestering: ");
		for (String s : user.getListening()) {
			sb.append(s).append(' ');
		}
		user.sendMessage(sb.toString());
		return true;
	}

	private boolean scListAll(User user, String[] args) {
		StringBuilder sb = new StringBuilder();
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
	}

	private boolean scNew(User user, String[] args) {
		if (args.length != 4) {
			user.sendMessage(ChatMsgs.helpSCNew());
			return true;
		}
		if (ChannelManager.getChannelManager().isValidChannel(args[1])) {
			user.sendMessage(ChatMsgs.errorChannelExists());
		}
		for (char c : args[1].substring(1).toCharArray()) {
			if (!Character.isAlphabetic(c)) {
				user.sendMessage(ChatMsgs.errorChannelName());
				return true;
			}
		}
		if (args[1].length() > 16) {
			user.sendMessage(ChatMsgs.errorChannelName());
		} else if (args[1].charAt(0) != '#' && !user.getPlayer().hasPermission("group.denizen")) {
			user.sendMessage(ChatMsgs.errorChannelName());
		} else if (ChannelType.getType(args[3]) == null) {
			user.sendMessage(ChatMsgs.errorInvalidType(args[3]));
		} else if (AccessLevel.getAccessLevel(args[2]) == null) {
			user.sendMessage(ChatMsgs.errorInvalidAccess(args[2]));
		} else {
			ChannelManager.getChannelManager().createNewChannel(args[1],
					AccessLevel.getAccessLevel(args[2]), user.getUUID(), ChannelType.getType(args[3]));
			Channel c = ChannelManager.getChannelManager().getChannel(args[1]);
			user.sendMessage(ChatMsgs.onChannelCreation(c));
			user.setCurrent(c);
		}
		return true;
	}

	private boolean scNick(User user, String[] args) {
		Channel c = user.getCurrent();
		if (c == null) {
			user.sendMessage(ChatMsgs.errorNoCurrent());
			return true;
		}
		if (args.length == 1) {
			user.sendMessage(ChatMsgs.helpSCNick());
			return true;
		}
		if (!(c instanceof NickChannel)) {
			user.sendMessage(ChatMsgs.unsupportedOperation(c.getName()));
			return true;
		}
		if (args[1].equalsIgnoreCase("list")) {
			if (c.getType() == ChannelType.NICK) {
				user.sendMessage(ChatColor.YELLOW + "You can use any nick you want in a nick channel.");
				return true;
			}
			StringBuilder sb = new StringBuilder(ChatColor.YELLOW.toString()).append("Nicks: ");
			for (CanonNicks n : CanonNicks.values()) {
				if (n != CanonNicks.SERKITFEATURE) {
					sb.append(ChatColor.AQUA).append(n.getName());
					sb.append(ChatColor.YELLOW).append(", ");
				}
			}
			user.sendMessage(sb.substring(0, sb.length() - 4).toString());
			return true;
		}
		if (args.length == 2) {
			user.sendMessage(ChatMsgs.helpSCNick());
			return true;
		}
		if (args[1].equalsIgnoreCase("set")) {
			c.setNick(user, StringUtils.join(args, ' ', 2, args.length));
			return true;
		} else if (args[1].equalsIgnoreCase("remove")) {
			c.removeNick(user, true);
			return true;
		} else {
			user.sendMessage(ChatMsgs.helpSCNick());
			return true;
		}
	}

	private boolean scGlobal(User user, String[] args) {
		if (!user.getPlayer().hasPermission("group.denizen")) {
			return false;
		}
		if (args.length == 4 && args[1].equalsIgnoreCase("setnick")) {
			scGlobalSetNick(user, args);
			return true;
		} else if (args.length >= 3) {
			if (args[1].equalsIgnoreCase("mute")) {
				scGlobalMute(user, args);
				return true;
			} else if (args[1].equalsIgnoreCase("unmute")) {
				scGlobalUnmute(user, args);
				return true;
			} else if (args[1].equalsIgnoreCase("rmnick")) {
				scGlobalRmNick(user, args);
				return true;
			} else if (args[1].equalsIgnoreCase("clearnicks")) {
				for (User u : UserManager.getUsers()) {
					if (!u.getPlayer().getDisplayName().equals(u.getPlayerName())) {
						u.getPlayer().setDisplayName(u.getPlayerName());
					}
				}
			}
		}
		user.sendMessage(ChatMsgs.helpGlobalMod());
		return true;
	}

	private void scGlobalSetNick(User user, String[] args) {
		Player p = Bukkit.getPlayer(args[2]);
		if (p == null) {
			user.sendMessage(ChatMsgs.errorInvalidUser(args[2]));
			return;
		}
		p.setDisplayName(args[3]);
		String msg = ChatMsgs.onUserSetGlobalNick(args[2], args[3]);
		for (User u : UserManager.getUsers()) {
			u.sendMessage(msg);
		}
		Log.anonymousInfo(msg);
	}

	private void scGlobalRmNick(User user, String[] args) {
		Player p = Bukkit.getPlayer(args[2]);
		if (p == null) {
			user.sendMessage(ChatMsgs.errorInvalidUser(args[2]));
			return;
		}
		String msg = ChatMsgs.onUserRmGlobalNick(args[2], p.getDisplayName());
		for (User u : UserManager.getUsers()) {
			u.sendMessage(msg);
		}
		Log.anonymousInfo(msg);
		p.setDisplayName(p.getName());
	}

	private void scGlobalMute(User user, String[] args) {
		Player p = Bukkit.getPlayer(args[2]);
		if (p == null) {
			user.sendMessage(ChatMsgs.errorInvalidUser(args[2]));
			return;
		}
		User victim = UserManager.getUser(p.getUniqueId());
		victim.setMute(true);
		String msg = ChatMsgs.onUserMute(args[2]);
		for (User u : UserManager.getUsers()) {
			u.sendMessage(msg);
		}
		Log.anonymousInfo(msg);
	}

	private void scGlobalUnmute(User user, String[] args) {
		Player p = Bukkit.getPlayer(args[2]);
		if (p == null) {
			user.sendMessage(ChatMsgs.errorInvalidUser(args[2]));
			return;
		}
		User victim = UserManager.getUser(p.getUniqueId());
		victim.setMute(false);;
		String msg = ChatMsgs.onUserUnmute(args[2]);
		for (User u : UserManager.getUsers()) {
			u.sendMessage(msg);
		}
		Log.anonymousInfo(msg);
	}

	private boolean scChannel(User user, String[] args) {
		Channel c = user.getCurrent();
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
				User u = UserManager.getUser(userID);
				if (u.getCurrent().equals(c)) {
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
