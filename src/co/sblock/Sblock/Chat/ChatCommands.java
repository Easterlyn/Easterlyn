package co.sblock.Sblock.Chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;
import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Chat.Channel.ChannelType;
import co.sblock.Sblock.Chat.Channel.NickChannel;
import co.sblock.Sblock.Chat.Channel.RPChannel;
import co.sblock.Sblock.Database.DBManager;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;
import co.sblock.Sblock.Utilities.Broadcast;
import co.sblock.Sblock.Utilities.Log;

/**
 * Command handler for all Chat-related commands.
 * 
 * @author Dublek, Jikoo
 */
public class ChatCommands implements CommandListener {

	private final char SPACE = '\u0020';

	@SblockCommand
	public boolean spawn(CommandSender sender, String[] args) {
		((Player) sender).performCommand("mvs");
		return true;
	}

	@SblockCommand
	public boolean color(CommandSender sender, String[] args) {
		sender.sendMessage(ColorDef.listColors());
		return true;
	}

	@SblockCommand(consoleFriendly = true)
	public boolean lel(CommandSender sender, String[] text) {
		if (sender instanceof Player && !sender.hasPermission("group.horrorterror") || text == null) {
			sender.sendMessage(ChatColor.BLACK + "Lul.");
			return true;
		}
		StringBuilder lelOut = new StringBuilder();
		for (String s : text) {
			for (int i = 0; i < s.length();) {
				for (int j = 0; j < ColorDef.RAINBOW.length; j++) {
					if (i >= s.length())
						break;
					lelOut.append(ColorDef.RAINBOW[j]).append(ChatColor.MAGIC).append(s.charAt(i));
					i++;
				}
			}
			lelOut.append(SPACE);
		}
		Broadcast.general(lelOut.substring(0, lelOut.length() - 1 > 0 ? lelOut.length() - 1 : 0));
		return true;
	}

	@SblockCommand(consoleFriendly = true)
	public boolean le(CommandSender sender, String[] text) {
		if (sender instanceof Player && !sender.hasPermission("group.horrorterror")) {
			sender.sendMessage(ChatColor.BLACK + "Le no. Le /le is reserved for le fancy people.");
		}
		StringBuilder lelOut = new StringBuilder();
		for (String s : text) {
			s = s.toUpperCase();
			for (int i = 0; i < s.length();) {
				for (int j = 0; j < ColorDef.RAINBOW.length; j++) {
					if (i >= s.length())
						break;
					lelOut.append(ColorDef.RAINBOW[j]).append(s.charAt(i));
					i++;
				}
			}
			lelOut.append(SPACE);
		}
		Broadcast.general(lelOut.substring(0, lelOut.length() - 1 > 0 ? lelOut.length() - 1 : 0));
		return true;
	}

	@SblockCommand(consoleFriendly = true)
	public boolean whois(CommandSender sender, String[] target) {
		if (target == null || target.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please specify a user to look up.");
		}
		if (sender instanceof Player && !sender.hasPermission("group.denizen")) {
			((Player) sender).performCommand("profile " + target[0]);
			return true;
		}
		ChatUser u = ChatUserManager.getUserManager().getUser(target[0]);
		if (u == null) {
			DBManager.getDBM().startOfflineLookup(sender, target[0]);
			return true;
		}
		sender.sendMessage(u.toString());
		return true;
	}

	@SblockCommand(consoleFriendly = true)
	public boolean o(CommandSender sender, String text[]) {
		if (sender instanceof Player && !sender.hasPermission("group.horrorterror")) {
			sender.sendMessage(ChatColor.BOLD + "[o] "
					+ "You try to be the white text guy, but fail to be the white text guy. "
					+ "No one can be the white text guy except for the white text guy.");
			return true;
		}
		if (text == null || text.length == 0) {
			sender.sendMessage(ChatColor.BOLD + "[o] If you're going to speak for me, please proceed.");
		}
		StringBuilder o = new StringBuilder(ChatColor.BOLD.toString()).append("[o] ");
		for (String s : text) {
			o.append(s).append(SPACE);
		}
		Broadcast.general(o.substring(0, o.length() - 1 > 0 ? o.length() - 1 : 0));
		return true;
	}

	@SblockCommand(consoleFriendly = true)
	public boolean sban(CommandSender sender, String[] args) {
		if (sender instanceof Player && !sender.hasPermission("group.horrorterror")) {
			sender.sendMessage(ChatMsgs.permissionDenied());
			return true;
		}
		if (args == null || args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Specify a player.");
			return true;
		}
		String target = args[0];
		StringBuilder reason = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			reason.append(args[i]).append(SPACE);
		}
		if (args.length == 1) {
			reason.append("Git wrekt m8.");
		}
		if (!Bukkit.getOfflinePlayer(target).hasPlayedBefore()) {
			sender.sendMessage("Unknown user, check your spelling.");
			return true;
		}
		SblockUser victim = UserManager.getUserManager().getUser(target);
		if (victim != null) {
			for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
				u.sendMessage(ChatColor.DARK_RED + victim.getPlayerName()
						+ " has been superbanned: " + args);
			}
			DBManager.getDBM().addBan(victim, reason.toString());
			Bukkit.banIP(victim.getUserIP());
			victim.getPlayer().kickPlayer(reason.toString());
		}
		Bukkit.getOfflinePlayer(target).setBanned(true);
		DBManager.getDBM().deleteUser(target);
		Bukkit.dispatchCommand(sender, "lwc admin purge " + target);
		return true;
	}

	@SblockCommand(consoleFriendly = true)
	public boolean unsban(CommandSender sender, String[] target) {
		if (sender instanceof Player && !sender.hasPermission("group.horrorterror")) {
			sender.sendMessage(ChatMsgs.permissionDenied());
			return true;
		}
		if (target == null || target.length == 0) {
			sender.sendMessage(ChatColor.RED + "Specify a player.");
			return true;
		}
			DBManager.getDBM().removeBan(target[0]);
			if (Bukkit.getOfflinePlayer(target[0]).hasPlayedBefore()) {
				Bukkit.getOfflinePlayer(target[0]).setBanned(false); //
				Bukkit.broadcastMessage(ChatColor.RED + "[Lil Hal] " + target[0] + " has been unbanned.");
			} else {
				sender.sendMessage(ChatColor.GREEN + "Not globally announcing unban: " + target[0]
						+ " has not played before or is an IP.");
			}
			return true;
	}

	@SblockCommand(consoleFriendly = true)
	public boolean sc(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) { // future console-friendly stuff
			sender.sendMessage(ChatColor.DARK_RED + "No commands programmed yet! :D");
			return true;
		} else { // ingame commands
			ChatUser user = ChatUserManager.getUserManager().getUser(sender.getName());
			if (args == null || args.length == 0) {
				sender.sendMessage(ChatMsgs.helpDefault());
			} else if (args[0].equalsIgnoreCase("c")) {
				return scC(user, args);
			} else if (args[0].equalsIgnoreCase("l")) {
				return scL(user, args);
			} else if (args[0].equalsIgnoreCase("leave")) {
				return scLeave(user, args);
			} else if (args[0].equalsIgnoreCase("list")) {
				return scList(user, args);
			} else if (args[0].equalsIgnoreCase("listall")) {
				return scListAll(user, args);
			} else if (args[0].equalsIgnoreCase("new")) {
				return scNew(user, args);
			} else if(args[0].equalsIgnoreCase("nick")) {
				return scNick(user, args);
			} else if (args[0].equalsIgnoreCase("channel")) {
				return scChannel(user, args);
			} else if (args[0].equalsIgnoreCase("global")) {
				return scGlobal(user, args);
			} else {
				sender.sendMessage(ChatMsgs.helpDefault());
			}
		}
		return true;
	}

	private boolean scC(ChatUser user, String[] args) {
		if (args.length == 1) {
			user.sendMessage(ChatMsgs.helpSCC());
			return true;
		}
		try {
			if (ChatModule.getChatModule().getChannelManager().getChannel(args[1]).getType().equals(ChannelType.REGION)) {
				user.sendMessage(ChatMsgs.errorRegionChannelJoin());
				return true;
			}
			user.setCurrent(ChatModule.getChatModule().getChannelManager().getChannel(args[1]));
		} catch (NullPointerException e) {
			user.sendMessage(ChatMsgs.errorInvalidChannel(args[1]));
		}
		return true;
	}

	private boolean scL(ChatUser user, String[] args) {
		if (args.length == 1) {
			user.sendMessage(ChatMsgs.helpSCL());
			return true;
		}
		try {
			if (ChatModule.getChatModule().getChannelManager().getChannel(args[1]).getType().equals(ChannelType.REGION)) {
				user.sendMessage(ChatMsgs.errorRegionChannelJoin());
				return true;
			}
			user.addListening(ChatModule.getChatModule().getChannelManager().getChannel(args[1]));
		} catch (NullPointerException e) {
			user.sendMessage(ChatMsgs.errorInvalidChannel(args[1]));
		}
		return true;
	}

	private boolean scLeave(ChatUser user, String[] args) {
		if (args.length == 1) {
			user.sendMessage(ChatMsgs.helpSCLeave());
			return true;
		}
		try {
			if (ChatModule.getChatModule().getChannelManager().getChannel(args[1]).getType().equals(ChannelType.REGION)) {
				user.sendMessage(ChatMsgs.errorRegionChannelLeave());
				return true;
			}
			user.removeListening(args[1]);
		} catch (NullPointerException e) {
			user.sendMessage(ChatMsgs.errorInvalidChannel(args[1]));
			user.removeListening(args[1]);
		}
		return true;
		
	}

	private boolean scList(ChatUser user, String[] args) {
		StringBuilder sb = new StringBuilder().append(ChatColor.YELLOW).append("Currently pestering: ");
		for (String s : user.getListening()) {
			sb.append(s).append(SPACE);
		}
		user.sendMessage(sb.toString());
		return true;
	}

	private boolean scListAll(ChatUser user, String[] args) {
		StringBuilder sb = new StringBuilder();
		sb.append(ChatColor.YELLOW).append("All channels: ");
		for (Channel c : ChannelManager.getChannelList().values()) {
			ChatColor cc;
			if (user.isListening(c)) {
				cc = ChatColor.YELLOW;
			} else if (c.getAccess().equals(AccessLevel.PUBLIC)) {
				cc = ChatColor.GREEN;
			} else {
				cc = ChatColor.RED;
			}
			sb.append(cc).append(c.getName()).append(SPACE);
		}
		user.sendMessage(sb.toString());
		return true;
	}

	private boolean scNew(ChatUser user, String[] args) {
		if (args.length != 4) {
			user.sendMessage(ChatMsgs.helpSCNew());
			return true;
		}
		if (args[1].length() > 16) {
			user.sendMessage(ChatMsgs.errorChannelNameTooLong());
		} else if (ChannelType.getType(args[3]) == null) {
			user.sendMessage(ChatMsgs.errorInvalidType(args[3]));
		} else if (AccessLevel.getAccess(args[2]) == null) {
			user.sendMessage(ChatMsgs.errorInvalidAccess(args[2]));
		} else {
			ChatModule.getChatModule().getChannelManager()
					.createNewChannel(args[1], AccessLevel.getAccess(args[2]),
							user.getPlayerName(), ChannelType.getType(args[3]));
			Channel c = ChatModule.getChatModule().getChannelManager().getChannel(args[1]);
			user.sendMessage(ChatMsgs.onChannelCreation(c));
		}
		return true;
	}

	private boolean scNick(ChatUser user, String[] args) {
		Channel c = user.getCurrent();
		if (args.length == 1 || args.length > 3) {
			user.sendMessage(ChatMsgs.helpSCNick());
			return true;
		} else if (c instanceof NickChannel || c instanceof RPChannel) {
			if (args[1].equalsIgnoreCase("set") && args.length == 3) {
				c.setNick(user, args[2]);
				return true;
			} else if (args[1].equalsIgnoreCase("remove")) {
				c.removeNick(user);
				return true;
			} else {
				user.sendMessage(ChatMsgs.helpSCNick());
				return true;
			}
		} else {
			user.sendMessage(ChatMsgs.errorNickUnsupported());
			return true;
		}
	}

	private boolean scGlobal(ChatUser user, String[] args) {
		if (!user.getPlayer().hasPermission("group.denizen")) {
			user.sendMessage(ChatMsgs.permissionDenied());
			return true;
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
				for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
					if (!u.getGlobalNick().equals(u.getPlayerName())) {
						u.setGlobalNick(u.getPlayerName());
					}
				}
			}
		}
		user.sendMessage(ChatMsgs.helpGlobalMod());
		return true;
	}

	private void scGlobalSetNick(ChatUser user, String[] args) {
		ChatUser victim = ChatUserManager.getUserManager().getUser(args[2]);
		if (victim == null) {
			user.sendMessage(ChatMsgs.errorInvalidUser(args[2]));
			return;
		}
		victim.setGlobalNick(args[3]);
		String msg = ChatMsgs.onUserSetGlobalNick(args[2], args[3]);
		for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
			u.sendMessage(msg);
		}
		Log.infoNoName(msg);
	}

	private void scGlobalRmNick(ChatUser user, String[] args) {
		ChatUser victim = ChatUserManager.getUserManager().getUser(args[2]);
		if (victim == null) {
			user.sendMessage(ChatMsgs.errorInvalidUser(args[2]));
			return;
		}
		String msg = ChatMsgs.onUserRmGlobalNick(args[2], user.getGlobalNick());
		for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
			u.sendMessage(msg);
		}
		Log.infoNoName(msg);
		victim.setGlobalNick(victim.getPlayerName());
	}

	private void scGlobalMute(ChatUser user, String[] args) {
		ChatUser victim = ChatUserManager.getUserManager().getUser(args[2]);
		if (victim == null) {
			user.sendMessage(ChatMsgs.errorInvalidUser(args[2]));
			return;
		}
		victim.setMute(true);
		String msg = ChatMsgs.onUserMute(args[2]);
		for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
			u.sendMessage(msg);
		}
		Log.infoNoName(msg);
	}

	private void scGlobalUnmute(ChatUser user, String[] args) {
		ChatUser victim = ChatUserManager.getUserManager().getUser(args[2]);
		if (victim == null) {
			user.sendMessage(ChatMsgs.errorInvalidUser(args[2]));
			return;
		}
		victim.setMute(true);
		String msg = ChatMsgs.onUserUnmute(args[2]);
		for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
			u.sendMessage(msg);
		}
		Log.infoNoName(msg);
	}

	private boolean scChannel(ChatUser user, String[] args) {
		Channel c = user.getCurrent();
		if (args.length == 2 && args[1].equalsIgnoreCase("info")) {
			user.sendMessage(c.toString());
			return true;
		}
		if (!c.isChannelMod(user)) {
			user.sendMessage(ChatMsgs.permissionDenied());
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
			for (String s : c.getListening()) {
				ChatUser u = ChatUserManager.getUserManager().getUser(s);
				if (u.getCurrent().equals(c)) {
					sb.append(ChatColor.GREEN).append(u.getPlayerName()).append(SPACE);
				} else {
					sb.append(ChatColor.YELLOW).append(u.getPlayerName()).append(SPACE);
				}
			}
			user.sendMessage(sb.toString());
			return true;
		} else if (args.length >= 3) {
			if (args[1].equalsIgnoreCase("kick")) {
				c.kickUser(ChatUserManager.getUserManager().getUser(args[2]), user);
				return true;
			} else if (args[1].equalsIgnoreCase("ban")) {
				c.banUser(args[2], user);
				return true;
			}
		}
		if (c.isOwner(user)) {
			if (args.length >= 4 && args[1].equalsIgnoreCase("mod")) {
				if (args[2].equalsIgnoreCase("add")) {
					c.addMod(user, args[3]);
					return true;
				} else if (args[2].equalsIgnoreCase("remove")) {
					c.removeMod(user, args[3]);
					return true;
				} else {
					user.sendMessage(ChatMsgs.helpChannelMod());
					if (c.isOwner(user)) {
						user.sendMessage(ChatMsgs.helpChannelOwner());
					}
					return true;
				}
			} else if (args.length >= 3 && args[1].equalsIgnoreCase("unban")) {
				ChatModule.getChatModule().getChannelManager().getChannel(c.getName())
						.unbanUser(args[2], user);
				return true;
			} else if (args.length >= 2 && args[1].equalsIgnoreCase("disband")) {
				c.disband(user);
				return true;
			} else {
				user.sendMessage(ChatMsgs.helpChannelMod());
				if (c.isOwner(user)) {
					user.sendMessage(ChatMsgs.helpChannelOwner());
				}
				return true;
			}
		}
		return false;
	}
}
