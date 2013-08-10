/**
 * 
 */
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
			for (int i = 0; i < text.length(); i++) {
				for (int j = 0; j < ColorDef.RAINBOW.length; j++) {
					if (i >= text.length())
						break;
					lelOut = lelOut + ChatColor.valueOf(ColorDef.RAINBOW[j])
							+ ChatColor.MAGIC + text.charAt(i);
					i++;
				}
			}
			Sblogger.info("LEL", lelOut);
			for(Player p: Bukkit.getServer().getOnlinePlayers()) {
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
			for (int i = 0; i < text.length(); i++) {
				for (int j = 0; j < ColorDef.RAINBOW.length; j++) {
					if (i >= text.length())
						break;
					leOut = leOut + ChatColor.valueOf(ColorDef.RAINBOW[j]) + text.charAt(i);
					i++;
				}
			}
			Sblogger.info("LE", leOut);
			for(Player p: Bukkit.getServer().getOnlinePlayers()) {
				p.sendMessage(leOut);
			}
		} else {
			sender.sendMessage(ChatColor.BLACK + "Aren't you fancy.");
		}
		return true;
	}
	

	@SblockCommand(consoleFriendly = true)
	public boolean whois(CommandSender sender, String target) {
		if (!(sender instanceof Player) || sender.hasPermission("group.denizen")) {
			SblockUser u = UserManager.getUserManager().getUser(target);
			u.toString();
			return true;
		} else {
			sender.sendMessage(ChatColor.BLACK +
					"There are mysteries into which it behooves one not to delve too deeply...");
			return true;
		}
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean o(CommandSender sender, String text) {
		if (!(sender instanceof Player) ||
				sender.hasPermission("group.horrorterror") || sender.isOp()) {
			Sblogger.infoNoLogName(ChatColor.WHITE + "[o] " + text);
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				p.sendMessage(ChatColor.BOLD + "[o] " + text);
			}
			return true;
		} else {
			sender.sendMessage(ChatColor.BOLD + "[o] "
					+"You try to be the white text guy, but fail to be the white text guy."
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
			for (SblockUser u : UserManager.getUserManager().getUserlist()) {
				u.sendMessage(ChatColor.DARK_RED + victim.getPlayerName() +
						"has been superbanned for " + reason);
			}
			victim.getPlayer().setBanned(true);
			victim.getPlayer().kickPlayer(reason);
			new ChatStorage().setBan(target, reason);
			return true;
		} else {
			sender.sendMessage(ChatColor.BLACK +
					"There are mysteries into which it behooves one not to delve too deeply...");
			return true;
		}
	}

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean sc(CommandSender sender, String arguments) {
		boolean isConsole = !(sender instanceof Player);
		boolean isHelper = !isConsole && sender.hasPermission("group.helper");
		boolean isMod = !isConsole && sender.hasPermission("group.denizen");
		if (isConsole) { // TODO console-friendly stuff
			sender.sendMessage(ChatColor.DARK_RED + "No commands programmed yet! :D");
		} else { // ingame commands
			SblockUser user = UserManager.getUserManager().getUser(sender.getName());
			if (arguments == null) {
				return false;
			}
			String[] args = arguments.split(" ");
			if (args.length < 1) {
				return false;
			}
			if (args[0].equalsIgnoreCase("c")) { // setChannel
				if (args.length == 1) {
					sender.sendMessage(ChatColor.YELLOW
							+ "Set current channel:\n\t/sc c <$channelname>");
					return true;
				}
				try {
					user.setCurrent(ChatModule.getInstance().getChannelManager()
							.getChannel(args[1]));
				} catch (NullPointerException e) {
					sender.sendMessage(ChatColor.RED + "Channel "
							+ ChatColor.GOLD + args[1] + ChatColor.RED
							+ " does not exist!");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("l")) { // addListening
				if (args.length == 1) {
					sender.sendMessage(ChatColor.YELLOW
							+ "Listen to a channel:\n\t/sc l <$channelname>");
					return true;
				}
				try {
					user.addListening(ChatModule.getInstance()
							.getChannelManager().getChannel(args[1]));
				} catch (NullPointerException e) {
					sender.sendMessage(ChatColor.RED + "Channel "
							+ ChatColor.GOLD + args[1] + ChatColor.RED
							+ " does not exist!");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("leave")) { // removeListening
				if (args.length == 1) {
					sender.sendMessage(ChatColor.YELLOW
							+ "Stop listening to a channel:\n\t/sc leave <$channelname>");
					return true;
				}
				try {
					user.removeListening(args[1]);
				} catch (NullPointerException e) {
					sender.sendMessage(ChatColor.RED + "Channel "
							+ ChatColor.GOLD + args[1] + ChatColor.RED
							+ " does not exist!");
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
				Map<String, Channel> channels = ChannelManager
						.getChannelList();
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
							+ "Create a new channel:\n\t/sc new <$channelname> <$sAccess> <$lAccess>\n\t"
							+ "sendAccess and listenAccess must be either PUBLIC or PRIVATE");
					return true;
				}
				if (ChannelType.getType(args[3]) == null) {
					user.sendMessage(ChatMsgs.errorInvalidType(args[3]));
				} else if (AccessLevel.getAccess(args[2]) == null) {
					user.sendMessage(ChatMsgs.errorInvalidAccess(args[2]));
				} else {
					ChatModule.getInstance().getChannelManager().createNewChannel(args[1],
							AccessLevel.getAccess(args[2]),
							user.getPlayerName(), ChannelType.getType(args[3]));
					return true;
				}
			} else if (args[0].equalsIgnoreCase("channel")) { // ChannelOwner/Mod commands
				Channel c = user.getCurrent();
				if (c.isMod(user) || isHelper) {
					if (args.length == 1) {
						this.sendChannelHelp(user, c);
					}
					if (args.length >= 2 && args[1].equalsIgnoreCase("getlisteners")) {
						String listenerList = ChatColor.YELLOW
								+ "Channel members: ";
						for (String s : c.getListening()) {
							SblockUser u = UserManager
									.getUserManager().getUser(s);
							if (u.getCurrent().equals(c)) {
								listenerList += ChatColor.GREEN
										+ u.getPlayerName() + " ";
							} else {
								listenerList += ChatColor.YELLOW
										+ u.getPlayerName() + " ";
							}
						}
						sender.sendMessage(listenerList);
						return true;
					}
					if (args.length >= 3) {
						if (args[1].equalsIgnoreCase("kick")) {
							c.kickUser(
									UserManager.getUserManager().getUser(
											args[2]), user);
							return true;
						}
						if (args[1].equalsIgnoreCase("ban")) {
							c.banUser(args[2], user);
							return true;
						}
					}
					if (c.isOwner(user) || isMod) {
						if (args.length >= 4 && args[1].equalsIgnoreCase("mod")) {
							// TODO offline user support, important
							if (args[2].equalsIgnoreCase("add")) {
								c.addMod(args[3], user);
								return true;
							} else if (args[2].equalsIgnoreCase("remove")) {
								c.removeMod(args[3], user);
								return true;
							} else {
								this.sendChannelHelp(user, c);
								return true;
							}
						}
						if (args.length >= 3 && args[1].equalsIgnoreCase("unban")) {
							ChatModule.getInstance().getChannelManager()
									.getChannel(c.getName())
									.unbanUser(args[2], user);
							return true;
						}
						if (args.length >= 2 && args[0].equalsIgnoreCase("disband")) {
							sender.sendMessage(ChatColor.YELLOW
									+ "Command coming soon!"); // TODO
							return true;
						}
					} else {
						sender.sendMessage(ChatColor.BLACK +
								"There are mysteries into which it behooves one not to delve too deeply...");
						return true;
					}
				}
			} else if (args[0].equalsIgnoreCase("global")) {
				if (isMod || sender.isOp()) {
					if (args.length < 3) {
						return false; // TODO error message
					}
					SblockUser victim = UserManager.getUserManager()
							.getUser(args[2]);
					if (args.length == 4 && args[1].equalsIgnoreCase("setnick")) {
						victim.setNick(args[3]);
						return true;
					}
					if (args.length >= 2) {
						if (args[1].equalsIgnoreCase("mute")) {
							victim.setMute(true);
							return true;
						}
						if (args[1].equalsIgnoreCase("unmute")) {
							victim.setMute(false);
							return true;
						}
						if (args[1].equalsIgnoreCase("rmnick")) {
							victim.setNick(victim.getPlayerName());
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

	// TODO -> ChatMsgs
	private void sendDefaultHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.YELLOW + "/sc subcommands:\n"
				+ ChatColor.GREEN + "c <channel>: "
				+ ChatColor.YELLOW + "Talking will send messages to <channel>.\n"
				+ ChatColor.GREEN + "l <channel>: "
				+ ChatColor.YELLOW + "Listen to <channel>.\n"
				+ ChatColor.GREEN + "leave <channel>: "
				+ ChatColor.YELLOW + "Stop listening to <channel>.\n"
				+ ChatColor.GREEN + "list: "
				+ ChatColor.YELLOW + "List all channels you are listening to.\n"
				+ ChatColor.GREEN + "listall: "
				+ ChatColor.YELLOW + "List all channels.\n"
				+ ChatColor.GREEN + "new <name> <access> <type>: "
				+ ChatColor.YELLOW + "Create a new channel.\n"
				+ ChatColor.GREEN + "channel: "
				+ ChatColor.YELLOW + "Channel moderation commands.");
	}
	
	private void sendChannelHelp(SblockUser user, Channel c) {
		String helpMod = ChatColor.YELLOW
				+ "Channel Mod commands:\n"
				+ ChatColor.GREEN + "channel kick <$user>: "
				+ ChatColor.YELLOW + "Kick a user from the channel\n"
				+ ChatColor.GREEN + "channel ban <$user>: "
				+ ChatColor.YELLOW + "Ban a user from the channel\n"
				+ ChatColor.GREEN + "channel setalias <$alias>: "
				+ ChatColor.YELLOW + "Set an alias for the channel\n"
				+ ChatColor.GREEN + "channel rmalias: "
				+ ChatColor.YELLOW + "Remove the channel alias\n"
				+ ChatColor.GREEN + "channel getListeners: "
				+ ChatColor.YELLOW + "List all users currently listening to this channel";
		String helpOwner = ChatColor.YELLOW
				+ "Channel Owner commands:\n"
				+ ChatColor.GREEN + "/sc channel mod <add/remove> <$user>: "
				+ ChatColor.YELLOW + "Add or remove a channel mod\n"
				+ ChatColor.GREEN + "/sc channel <ban/unban> <$user>"
				+ ChatColor.YELLOW + "(Un)bans a user from the channel\n"
				+ ChatColor.GREEN + "/sc disband"
				+ ChatColor.YELLOW + "Disband coming soon!";
		if (c.isMod(user) || user.getPlayer().hasPermission("group.helper")) {
			user.sendMessage(helpMod);
			if (c.isOwner(user) || user.getPlayer().hasPermission("group.denizen")) {
				user.sendMessage(helpOwner);
			}
		}
	}
}
