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
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * Command handler for all Chat-related commands.
 * 
 * @author Dublek, Jikoo
 */
public class ChatModuleCommandListener implements CommandListener {

	@SblockCommand
	public boolean spawn(CommandSender sender) {
		((Player) sender).performCommand("mvs");
		return true;
	}

	@SblockCommand
	public boolean color(CommandSender sender) {
		sender.sendMessage(ColorDef.listColors());
		return true;
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean lel(CommandSender sender, String text) {
		if (!(sender instanceof Player) || sender.hasPermission("group.horrorterror")) {
			String lelOut = new String();
			for (int i = 0; i < text.length();) {
				for (int j = 0; j < ColorDef.RAINBOW.length; j++) {
					if (i >= text.length())
						break;
					lelOut = lelOut + ColorDef.RAINBOW[j] + ChatColor.MAGIC
							+ text.charAt(i);
					i++;
				}
			}
			Bukkit.broadcastMessage(lelOut);
		} else {
			sender.sendMessage(ChatColor.BLACK + "Lul.");
		}
		return true;
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean le(CommandSender sender, String text) {
		if (!(sender instanceof Player) || sender.hasPermission("group.horrorterror")) {
			String leOut = new String();
			text = text.toUpperCase();
			for (int i = 0; i < text.length();) {
				for (int j = 0; j < ColorDef.RAINBOW.length; j++) {
					if (i >= text.length())
						break;
					String next = text.substring(i, i + 1);
					if (next.equals("O")) {
						leOut = leOut + ColorDef.RAINBOW[j] + ChatColor.MAGIC + next;
					} else {
						leOut = leOut + ColorDef.RAINBOW[j] + next;
					}
					i++;
				}
			}
			Bukkit.broadcastMessage(leOut);
		} else {
			sender.sendMessage(ChatColor.BLACK
					+ "Le no. Le command \"le\" is reserved for fancier people than you.");
		}
		return true;
	}

	@SblockCommand(consoleFriendly = true)
	public boolean whois(CommandSender sender, String target) {
		if (!(sender instanceof Player) || sender.hasPermission("group.denizen")) {
			ChatUser u = ChatUserManager.getUserManager().getUser(target);
			if (u == null) {
				sender.sendMessage(ChatMsgs.errorInvalidUser(target)
						+ "\n/whois <player>\n" + "<name>, <class> of <aspect>\n"
						+ "<mPlanet>, <dPlanet>, <towerNum>, <sleepState>\n"
						+ "<isMute>, <currentChannel>, <listeningChannels>\n"
						+ "<ip>, <previousLocation>\n" + "<timePlayed>, <lastLogin>");
				return true;
			}
			sender.sendMessage(u.toString());
			return true;
		} else {
			sender.sendMessage(ChatMsgs.darkMysteries());
			return true;
		}
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean o(CommandSender sender, String text) {
		if (!(sender instanceof Player) || sender.hasPermission("group.horrorterror")) {
			Sblogger.info("o", text);
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				p.sendMessage(ChatColor.BOLD + "[o] " + text);
			}
			return true;
		} else {
			sender.sendMessage(ChatColor.BOLD + "[o] "
					+ "You try to be the white text guy, but fail to be the white text guy. "
					+ "No one can be the white text guy except for the white text guy.");
			return true;
		}
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean sban(CommandSender sender, String args) {
		if (!(sender instanceof Player) || sender.hasPermission("group.horrorterror")) {
			String target = args.replaceAll(" .*", "");
			args = args.substring(args.indexOf(" "), args.length());
			if (args.length() == 0) {
				args = "no reason whatsoever!";
			}
			if (!Bukkit.getOfflinePlayer(target).hasPlayedBefore()) {
				sender.sendMessage("Unknown user, check your spelling.");
				return true;
			}
			SblockUser victim = UserManager.getUserManager().getUser(target);
			if (victim != null) {
				for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
					u.sendMessage(ChatColor.DARK_RED + victim.getPlayerName()
							+ " has been superbanned for " + args);
				}
				DBManager.getDBM().addBan(victim, args);
				Bukkit.banIP(victim.getUserIP());
				victim.getPlayer().kickPlayer(args);
			}
			Bukkit.getOfflinePlayer(target).setBanned(true);
			DBManager.getDBM().deleteUser(target);
			Bukkit.dispatchCommand(sender, "lwc admin purge " + target);
			return true;
		} else {
			sender.sendMessage(ChatMsgs.darkMysteries());
			return true;
		}
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean unsban(CommandSender sender, String target) {
		if (!(sender instanceof Player) || sender.hasPermission("group.horrorterror")) {
			DBManager.getDBM().removeBan(target);
			if (Bukkit.getOfflinePlayer(target).hasPlayedBefore()) {
				for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
					u.sendMessage(ChatColor.GREEN + target + " has been unbanned.");
				}
				Sblogger.info("Sban", target + " has been unbanned!");
			} else {
				sender.sendMessage(ChatColor.GREEN + "Not globally announcing unban: " + target
						+ " has not played before or is an IP.");
			}
			return true;
		} else {
			sender.sendMessage(ChatMsgs.darkMysteries());
			return true;
		}
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean sc(CommandSender sender, String arguments) {
		if (!(sender instanceof Player)) { // future console-friendly stuff
			sender.sendMessage(ChatColor.DARK_RED + "No commands programmed yet! :D");
			return true;
		} else { // ingame commands
			ChatUser user = ChatUserManager.getUserManager().getUser(sender.getName());
			if (arguments == null) {
				sender.sendMessage(ChatMsgs.helpDefault());
				return true;
			}
			String[] args = arguments.split(" ");
			if (args.length == 0) {
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
			sb.append(s).append(" ");
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
			sb.append(cc).append(c.getName()).append(" ");
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
			user.sendMessage(ChatMsgs.darkMysteries());
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
		Sblogger.infoNoLogName(msg);
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
		Sblogger.infoNoLogName(msg);
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
		Sblogger.infoNoLogName(msg);
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
		Sblogger.infoNoLogName(msg);
	}

	private boolean scChannel(ChatUser user, String[] args) {
		Channel c = user.getCurrent();
		if (args.length == 2 && args[1].equalsIgnoreCase("info")) {
			user.sendMessage(c.toString());
			return true;
		}
		if (!c.isChannelMod(user)) {
			user.sendMessage(ChatMsgs.darkMysteries());
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
					sb.append(ChatColor.GREEN).append(u.getPlayerName()).append(" ");
				} else {
					sb.append(ChatColor.YELLOW).append(u.getPlayerName()).append(" ");
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
