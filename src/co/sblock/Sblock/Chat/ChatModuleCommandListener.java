package co.sblock.Sblock.Chat;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.SblockCommand;
import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Chat.Channel.ChannelType;
import co.sblock.Sblock.Chat.Channel.NickChannel;
import co.sblock.Sblock.Chat.Channel.RPChannel;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * @author Dublek
 * @author Jikoo
 */
public class ChatModuleCommandListener implements CommandListener {

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean lel(CommandSender sender, String text) {
		if (!(sender instanceof Player) || sender.hasPermission("group.denizen")) {
			String lelOut = new String();
			for (int i = 0; i < text.length();) {
				for (int j = 0; j < ColorDef.RAINBOW.length; j++) {
					if (i >= text.length())
						break;
					lelOut = lelOut + ChatColor.valueOf(ColorDef.RAINBOW[j]) + ChatColor.MAGIC
							+ text.charAt(i);
					i++;
				}
			}
			Sblogger.info("LEL", lelOut);
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				p.sendMessage(lelOut);
			}
		} else {
			sender.sendMessage(ChatColor.BLACK + "lul.");
		}
		return true;
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean le(CommandSender sender, String text) {
		if (!(sender instanceof Player) || sender.hasPermission("group.denizen")) {
			String leOut = new String();
			for (int i = 0; i < text.length();) {
				for (int j = 0; j < ColorDef.RAINBOW.length; j++) {
					if (i >= text.length())
						break;
					leOut = leOut + ChatColor.valueOf(ColorDef.RAINBOW[j]) + text.charAt(i);
					i++;
				}
			}
			Sblogger.info("LE", leOut);
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				p.sendMessage(leOut);
			}
		} else {
			sender.sendMessage(ChatColor.BLACK + "Aren't you fancy.");
		}
		return true;
	}

	@SblockCommand(consoleFriendly = true)
	public boolean whois(CommandSender sender, String target) {
		if (target == null) {
			sender.sendMessage("/whois <player>\n" + "<name>, <class> of <aspect>\n"
					+ "<mPlanet>, <dPlanet>, <towerNum>, <sleepState>\n"
					+ "<isMute>, <currentChannel>, <listeningChannels>\n"
					+ "<ip>, <previousLocation>\n" + "<timePlayed>, <lastLogin>");
			return true;
		}
		if (!(sender instanceof Player) || sender.hasPermission("group.denizen")) {
			ChatUser u = ChatUserManager.getUserManager().getUser(target);
			sender.sendMessage(u.toString());
			return true;
		} else {
			sender.sendMessage(ChatColor.BLACK
					+ "There are mysteries into which it behooves one not to delve too deeply...");
			return true;
		}
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean o(CommandSender sender, String text) {
		if (!(sender instanceof Player) || sender.hasPermission("group.horrorterror")
				|| sender.isOp()) {
			Sblogger.infoNoLogName(ChatColor.WHITE + "[o] " + text);
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				p.sendMessage(ChatColor.BOLD + "[o] " + text);
			}
			return true;
		} else {
			sender.sendMessage(ChatColor.BOLD + "[o] "
					+ "You try to be the white text guy, but fail to be the white text guy."
					+ "No one can be the white text guy except for the white text guy.");
			return true;
		}
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean sban(CommandSender sender, String target, String reason) {
		if (!(sender instanceof Player) || sender.isOp()) {
			SblockUser victim = UserManager.getUserManager().getUser(target);
			Bukkit.banIP(victim.getUserIP());
			DatabaseManager.getDatabaseManager().deleteUser(victim);
			Bukkit.dispatchCommand(sender, "lwc admin purge " + target);
			for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
				u.sendMessage(ChatColor.DARK_RED + victim.getPlayerName()
						+ " has been superbanned for " + reason);
			}
			DatabaseManager.getDatabaseManager().addBan(victim, reason);
			victim.getPlayer().setBanned(true);
			victim.getPlayer().kickPlayer(reason);
			return true;
		} else {
			sender.sendMessage(ChatColor.BLACK
					+ "There are mysteries into which it behooves one not to delve too deeply...");
			return true;
		}
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean unsban(CommandSender sender, String target) {
		if (!(sender instanceof Player) || sender.isOp()) {
			if (Bukkit.getOfflinePlayer(target).hasPlayedBefore()) {
				for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
					u.sendMessage(ChatColor.GREEN + target + " has been unbanned.");
				}
				Sblogger.info("Sban", target + " has been unbanned!");
			} else {
				sender.sendMessage(ChatColor.GREEN + "Not globally announcing unban: " + target
						+ " has not played before or is an IP.");
			}
			DatabaseManager.getDatabaseManager().removeBan(target);
			return true;
		} else {
			sender.sendMessage(ChatColor.BLACK
					+ "There are mysteries into which it behooves one not to delve too deeply...");
			return true;
		}
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean sc(CommandSender sender, String arguments) {
		boolean isConsole = !(sender instanceof Player);
		boolean isHelper = !isConsole && sender.hasPermission("group.helper");
		boolean isMod = !isConsole && sender.hasPermission("group.denizen");
		ChatColor cmd = ChatColor.AQUA;
		if (isConsole) { // TODO console-friendly stuff
			sender.sendMessage(ChatColor.DARK_RED + "No commands programmed yet! :D");
		} else { // ingame commands
			ChatUser user = ChatUserManager.getUserManager().getUser(sender.getName());
			if (arguments == null) {
				return false;
			}
			String[] args = arguments.split(" ");
			if (args.length < 1) {
				return false;
			}
			
			if (args[0].equalsIgnoreCase("c")) { // setChannel
				if (args.length == 1) {
					sender.sendMessage(ChatColor.YELLOW	+ "Set current channel:\n" + cmd + "/sc c <$channelname>");
					return true;
				}
				if(ChatModule.getChatModule().getChannelManager().getChannel(args[1]).getType().equals(ChannelType.REGION))	{
					sender.sendMessage(ChatColor.RED + "You cannot join a region channel!");
					return true;
				}
				try {
					user.setCurrent(ChatModule.getChatModule().getChannelManager()
							.getChannel(args[1]));
				} catch (NullPointerException e) {
					sender.sendMessage(ChatColor.RED + "Channel " + ChatColor.GOLD + args[1]
							+ ChatColor.RED + " does not exist!");
				}
				return true;
				
			} else if (args[0].equalsIgnoreCase("l")) { // addListening
				if (args.length == 1) {
					sender.sendMessage(ChatColor.YELLOW
							+ "Listen to a channel:\n" + cmd + "/sc l <$channelname>");
					return true;
				}
				if(ChatModule.getChatModule().getChannelManager().getChannel(args[1]).getType().equals(ChannelType.REGION))	{
					sender.sendMessage(ChatColor.RED + "You cannot listen to a region channel!");
					return true;
				}
				try {
					user.addListening(ChatModule.getChatModule().getChannelManager()
							.getChannel(args[1]));
				} catch (NullPointerException e) {
					sender.sendMessage(ChatColor.RED + "Channel " + ChatColor.GOLD + args[1]
							+ ChatColor.RED + " does not exist!");
				}
				return true;
				
			} else if (args[0].equalsIgnoreCase("leave")) { // removeListening
				if (args.length == 1) {
					sender.sendMessage(ChatColor.YELLOW
							+ "Stop listening to a channel:\n" + cmd + "/sc leave <$channelname>");
					return true;
				}
				if(ChatModule.getChatModule().getChannelManager().getChannel(args[1]).getType().equals(ChannelType.REGION))	{
					sender.sendMessage(ChatColor.RED + "You cannot leave a region channel!");
					return true;
				}
				try {
					user.removeListening(args[1]);
					//sender.sendMessage(ChatMsgs.onChannelLeave(user, ChatModule.getChatModule()
					//		.getChannelManager().getChannel(args[1])));
				} catch (NullPointerException e) {
					sender.sendMessage(ChatColor.RED + "Channel " + ChatColor.GOLD + args[1]
							+ ChatColor.RED + " does not exist!");
				}
				return true;
				
			} else if (args[0].equalsIgnoreCase("list")) { // listListening
				String clist = ChatColor.YELLOW + "Currently pestering: ";
				for (String s : user.getListening()) {
					clist += s + " ";
				}
				sender.sendMessage(clist);
				return true;
				
			} else if (args[0].equalsIgnoreCase("listall")) { // listAll
				String clist = ChatColor.YELLOW + "All channels: ";
				Map<String, Channel> channels = ChannelManager.getChannelList();
				for (Channel c : channels.values()) {
					String next;
					if (user.isListening(c)) {
						next = ChatColor.YELLOW + c.getName() + " ";
						clist += next;
					} else if (c.getAccess().equals(AccessLevel.PUBLIC)) {
						next = ChatColor.GREEN + c.getName() + " ";
						clist += next;
					} else if (c.getAccess().equals(AccessLevel.PRIVATE)) {
						next = ChatColor.RED + c.getName() + " ";
						clist += next;
					}
				}
				sender.sendMessage(clist);
				return true;
				
			} else if (args[0].equalsIgnoreCase("new")) { // newChannel
				if (args.length != 4) {
					sender.sendMessage(ChatColor.YELLOW
							+ "Create a new channel:\n" + cmd + "/sc new <$channelname> <$access> <$type>\n"
							+ ChatColor.YELLOW + "Access must be either PUBLIC or PRIVATE\n"
							+ "Type must be NORMAL, NICK, or RP");
					return true;
				}
				if (ChannelType.getType(args[3]) == null || ChannelType.getType(args[3]) == ChannelType.REGION) {
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
				
			} else if(args[0].equalsIgnoreCase("nick"))	{
				Channel c = user.getCurrent();
				if(args.length == 1 || args.length > 3)	{
					sender.sendMessage(ChatColor.YELLOW + "Set your nick in a Nick or RP Channel\n" + cmd + "/sc nick <set/remove> <nick>");
					return true;
				}
				else if(c instanceof NickChannel || c instanceof RPChannel)	{				
					if(args[1].equalsIgnoreCase("set") && args.length == 3)	{
						c.setNick(user, args[2]);
						return true;
					}
					else if(args[1].equalsIgnoreCase("remove"))	{
						c.removeNick(user);
						return true;
					}		
				}
				else	{
					sender.sendMessage(ChatColor.YELLOW + "This channel does not support nicks!");
					return true;
				}
				
			} else if (args[0].equalsIgnoreCase("channel")) {
				// ChannelOwner/Mod commands
				Channel c = user.getCurrent();
				if(args[1].equalsIgnoreCase("info"))	{
					sender.sendMessage(c.toString());
					return true;
				}
				if (c.isMod(user) || isHelper) {
					if (args.length == 1) {
						this.sendChannelHelp(user, c);
						return true;
						
					} else if (args.length >= 2 && args[1].equalsIgnoreCase("getlisteners")) {
						String listenerList = ChatColor.YELLOW + "Channel members: ";
						for (String s : c.getListening()) {
							ChatUser u = ChatUserManager.getUserManager().getUser(s);
							if (u.getCurrent().equals(c)) {
								listenerList += ChatColor.GREEN + u.getPlayerName() + " ";
							} else {
								listenerList += ChatColor.YELLOW + u.getPlayerName() + " ";
							}
						}
						sender.sendMessage(listenerList);
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
					
					if (c.isOwner(user) || isMod) {
						if (args.length >= 4 && args[1].equalsIgnoreCase("mod")) {
							if (args[2].equalsIgnoreCase("add")) {
								c.addMod(user, args[3]);
								return true;
								
							} else if (args[2].equalsIgnoreCase("remove")) {
								c.removeMod(user, args[3]);
								return true;
								
							} else {
								this.sendChannelHelp(user, c);
								return true;
							}
							
						} else if (args.length >= 3 && args[1].equalsIgnoreCase("unban")) {
							ChatModule.getChatModule().getChannelManager().getChannel(c.getName())
									.unbanUser(args[2], user);
							return true;
							
						} else if (args.length >= 2 && args[1].equalsIgnoreCase("disband")) {
							c.disband(user);
							return true;
						}
					} else {
						sender.sendMessage(ChatColor.BLACK
								+ "There are mysteries into which it behooves one not to delve too deeply...");
						return true;
					}
				}
			} else if (args[0].equalsIgnoreCase("global")) {
				if (isMod || sender.isOp()) {
					if (args.length == 1) {
						this.sendModHelp(user);
						return true;
					}
					ChatUser victim = ChatUserManager.getUserManager().getUser(args[2]);
					if (args.length == 4 && args[1].equalsIgnoreCase("setnick")) {
						victim.setGlobalNick(args[3]);
						for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
							u.sendMessage(ChatColor.GREEN + victim.getPlayerName()
									+ ChatColor.YELLOW + " shall henceforth be know as: "
									+ ChatColor.GREEN + args[3]);
						}
						Sblogger.infoNoLogName(ChatColor.GREEN + victim.getPlayerName()
								+ ChatColor.YELLOW + " shall henceforth be know as: "
								+ ChatColor.GREEN + args[3]);
						return true;
					} else if (args.length >= 2) {
						if (args[1].equalsIgnoreCase("mute")) {
							victim.setMute(true);
							for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
								u.sendMessage(ChatColor.RED + victim.getPlayerName()
										+ " has been muted");
							}
							Sblogger.infoNoLogName(ChatColor.RED + victim.getPlayerName()
									+ " has been muted");
							return true;
						} else if (args[1].equalsIgnoreCase("unmute")) {
							victim.setMute(false);
							for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
								u.sendMessage(ChatColor.GREEN + victim.getPlayerName()
										+ " has been unmuted");
							}
							Sblogger.infoNoLogName(ChatColor.RED + victim.getPlayerName()
									+ " has been muted");
							return true;
						} else if (args[1].equalsIgnoreCase("rmnick")) {
							for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
								u.sendMessage(ChatColor.GREEN + victim.getPlayerName()
										+ ChatColor.YELLOW + " shall no longer be know as: "
										+ ChatColor.GREEN + victim.getGlobalNick());
							}
							Sblogger.infoNoLogName(ChatColor.GREEN + victim.getPlayerName()
									+ ChatColor.YELLOW + " shall no longer be know as: "
									+ ChatColor.GREEN + victim.getGlobalNick());
							victim.setGlobalNick(victim.getPlayerName());
							return true;
						}
					}
				}
			} else {
				this.sendDefaultHelp(sender);
			}

			// Global powers
			// setcurrent //sc c <channelname>
			// addlistening //sc l <channelname>
			// removelistening //sc leave <channelname>
			// getlistening //sc list
			// newchannel //sc new <name, sAccess, lAccess>
			// listallchannels //sc listall
			// whois(limited) //sc whois

			// Channelowner powers //prereq: Channel must be current
			// mod //sc channel mod add <user>
			// demod //sc channel mod remove <user>
			// unban //sc channel unban <user>
			// disband //sc channel disband

			// Channelmod powers
			// kick //sc channel kick <player>
			// ban //sc channel ban <player>
			// setalias //sc channel alias set <alias>
			// rmalias //sc channel alias remove
			// getListeners //sc channel listeners

			// Mod powers
			// Mute //sc global mute <player>
			// unmute //sc global unmute <player>
			// setnick //sc global setnick <player> <nick>
			// rmnick //sc global rmnick <player>
		}

		return true;
	}

	private void sendDefaultHelp(CommandSender sender) {
		sender.sendMessage(ChatMsgs.helpDefault());
	}

	private void sendChannelHelp(ChatUser user, Channel c) {
		if (c.isMod(user) || user.getPlayer().hasPermission("group.helper")) {
			user.sendMessage(ChatMsgs.helpChannelMod());
			if (c.isOwner(user) || user.getPlayer().hasPermission("group.denizen")) {
				user.sendMessage(ChatMsgs.helpChannelOwner());
			}
		}
	}

	private void sendModHelp(ChatUser user) {
		user.sendMessage(ChatMsgs.helpGlobalMod());
	}
}
